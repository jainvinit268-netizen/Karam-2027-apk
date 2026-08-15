from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


def replace_once(path, old, new, marker):
    p = ROOT / path
    s = p.read_text(encoding="utf-8")
    if marker in s:
        return False
    if old not in s:
        raise SystemExit(f"Patch marker not found in {path}: {marker}")
    p.write_text(s.replace(old, new, 1), encoding="utf-8")
    return True

# 1) ViewModel: download a direct PDF URL, then feed the existing persistent pipeline.
vm = ROOT / "app/src/main/java/com/example/ui/viewmodel/JeeViewModel.kt"
s = vm.read_text(encoding="utf-8")
if "fun convertPdfFromUrl(" not in s:
    s = s.replace(
        "import kotlinx.coroutines.CancellationException\n",
        "import kotlinx.coroutines.CancellationException\nimport kotlinx.coroutines.Dispatchers\n",
        1,
    )
    marker = "    fun convertFilesToCbt(\n"
    fn = '''    /** Imports a directly accessible PDF URL through the existing persistent PDF->CBT pipeline. */
    fun convertPdfFromUrl(
        testTitle: String,
        pdfUrl: String,
        answerKeyUri: android.net.Uri?,
        fallbackAnswerText: String,
        durationMinutes: Int = 180
    ) {
        activeQuestionPdfUri = null
        conversionJob?.cancel()
        conversionTickerJob?.cancel()
        conversionJob = viewModelScope.launch(Dispatchers.IO) {
            _conversionState.value = ConversionUiState(
                isProcessing = true,
                currentStep = ProcessingStep.ANALYZING_PDF,
                progressPercent = 2,
                progressMessage = "Downloading PDF from source link..."
            )
            try {
                val normalized = pdfUrl.trim()
                require(normalized.startsWith("https://") || normalized.startsWith("http://")) {
                    "Please paste a direct HTTP/HTTPS PDF link."
                }
                val connection = (java.net.URL(normalized).openConnection() as java.net.HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 20000
                    readTimeout = 60000
                    instanceFollowRedirects = true
                    setRequestProperty("User-Agent", "KARAM-2027/1.0")
                }
                connection.connect()
                val code = connection.responseCode
                require(code in 200..299) { "Source link returned HTTP $code." }
                val file = java.io.File(getApplication<Application>().cacheDir, "linked_source_${System.currentTimeMillis()}.pdf")
                connection.inputStream.use { input ->
                    java.io.FileOutputStream(file).use { output -> input.copyTo(output) }
                }
                connection.disconnect()
                require(file.length() > 100 && file.inputStream().use { it.readNBytes(4).contentEquals(byteArrayOf(0x25, 0x50, 0x44, 0x46)) }) {
                    "The source URL did not return a valid PDF."
                }
                _conversionState.value = _conversionState.value.copy(
                    progressPercent = 5,
                    progressMessage = "PDF downloaded. Starting extraction..."
                )
                convertFilesToCbt(
                    testTitle = testTitle.ifBlank { "Linked JEE Paper" },
                    questionPdfUri = android.net.Uri.fromFile(file),
                    answerKeyUri = answerKeyUri,
                    fallbackQuestionText = "",
                    fallbackAnswerText = fallbackAnswerText,
                    durationMinutes = durationMinutes,
                    pdfFileName = "Linked_JEE_Paper.pdf"
                )
            } catch (e: Exception) {
                _conversionState.value = ConversionUiState(
                    isProcessing = false,
                    errorMessage = "Source-link import failed: ${e.localizedMessage ?: "invalid PDF link"}"
                )
            }
        }
    }

'''
    if marker not in s:
        raise SystemExit("ViewModel insertion marker not found")
    s = s.replace(marker, fn + marker, 1)
    vm.write_text(s, encoding="utf-8")

# 2) PDF helper: support file:// URIs produced by the downloader.
helper = ROOT / "app/src/main/java/com/example/data/ai/PdfDocumentHelper.kt"
s = helper.read_text(encoding="utf-8")
if 'uri.scheme == "file"' not in s:
    s = s.replace("import java.io.FileOutputStream\n", "import java.io.FileOutputStream\nimport java.io.FileInputStream\n", 1)
    old = "            context.contentResolver.openInputStream(uri)?.use { stream ->\n                val bytes = stream.readBytes()"
    new = "            (if (uri.scheme == \"file\") FileInputStream(File(uri.path ?: return@withContext ExtractedDocument(fileName, \"\", 0, emptyList(), PdfSourceType.SCANNED_IMAGE))) else context.contentResolver.openInputStream(uri))?.use { stream ->\n                val bytes = stream.readBytes()"
    if old not in s:
        raise SystemExit("PDF helper direct-stream marker not found")
    s = s.replace(old, new, 1)
    old2 = "            context.contentResolver.openInputStream(uri)?.use { input ->\n                FileOutputStream(tempFile).use { output -> input.copyTo(output) }\n            }"
    new2 = "            (if (uri.scheme == \"file\") FileInputStream(File(uri.path ?: return@withContext ExtractedDocument(fileName, \"\", 0, emptyList(), PdfSourceType.SCANNED_IMAGE))) else context.contentResolver.openInputStream(uri))?.use { input ->\n                FileOutputStream(tempFile).use { output -> input.copyTo(output) }\n            }"
    if old2 not in s:
        raise SystemExit("PDF helper temp-stream marker not found")
    s = s.replace(old2, new2, 1)
    helper.write_text(s, encoding="utf-8")

# 3) Cropper: support the same file:// URI.
cropper = ROOT / "app/src/main/java/com/example/data/ai/PdfVisualCropper.kt"
s = cropper.read_text(encoding="utf-8")
if 'pdfUri.scheme == "file"' not in s:
    s = s.replace("import java.io.FileOutputStream\n", "import java.io.FileOutputStream\nimport java.io.FileInputStream\n", 1)
    old = "            context.contentResolver.openInputStream(pdfUri)?.use { input ->\n                FileOutputStream(tempPdf).use { output -> input.copyTo(output) }\n            } ?: return@withContext questions"
    new = "            (if (pdfUri.scheme == \"file\") FileInputStream(File(pdfUri.path ?: return@withContext questions)) else context.contentResolver.openInputStream(pdfUri))?.use { input ->\n                FileOutputStream(tempPdf).use { output -> input.copyTo(output) }\n            } ?: return@withContext questions"
    if old not in s:
        raise SystemExit("PDF cropper stream marker not found")
    s = s.replace(old, new, 1)
    cropper.write_text(s, encoding="utf-8")

# 4) UI: add an optional direct-link field and route Generate through it when supplied.
ui = ROOT / "app/src/main/java/com/example/ui/components/PdfToCbtSection.kt"
s = ui.read_text(encoding="utf-8")
if "var sourceLink by remember" not in s:
    s = s.replace(
        "    var selectedAnswerKeyFileName by remember { mutableStateOf<String?>(null) }\n",
        "    var selectedAnswerKeyFileName by remember { mutableStateOf<String?>(null) }\n    var sourceLink by remember { mutableStateOf(\"\") }\n",
        1,
    )
    marker = "        // STEP 1: QUESTION PAPER\n"
    card = '''        // OPTIONAL DIRECT PDF SOURCE LINK
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, JeeCyan.copy(alpha = 0.35f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("DIRECT PDF LINK (OPTIONAL)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                Text(
                    "Paste a direct PDF URL. The CBT is generated through the same pipeline and saved in Test Library.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = sourceLink,
                    onValueChange = { sourceLink = it },
                    label = { Text("Direct PDF URL") },
                    placeholder = { Text("https://example.com/paper.pdf") },
                    modifier = Modifier.fillMaxWidth().testTag("input_source_link"),
                    singleLine = true,
                    trailingIcon = {
                        if (sourceLink.isNotBlank()) {
                            IconButton(onClick = { sourceLink = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear link")
                            }
                        }
                    }
                )
            }
        }

'''
    if marker not in s:
        raise SystemExit("UI insertion marker not found")
    s = s.replace(marker, card + marker, 1)
    s = s.replace(
        "        val hasInputs = selectedPdfUri != null || selectedPdfFileName != null || questionPaperText.isNotBlank()",
        "        val hasInputs = sourceLink.isNotBlank() || selectedPdfUri != null || selectedPdfFileName != null || questionPaperText.isNotBlank()",
        1,
    )
    old_call = '''                viewModel.convertFilesToCbt(
                    testTitle = resolvedTitle,
                    questionPdfUri = selectedPdfUri,
                    answerKeyUri = selectedAnswerKeyUri,
                    fallbackQuestionText = questionPaperText,
                    fallbackAnswerText = answerKeyText,
                    durationMinutes = finalDuration,
                    pdfFileName = selectedPdfFileName ?: "Uploaded_JEE_Paper.pdf"
                )'''
    new_call = '''                if (sourceLink.isNotBlank()) {
                    viewModel.convertPdfFromUrl(
                        testTitle = resolvedTitle,
                        pdfUrl = sourceLink,
                        answerKeyUri = selectedAnswerKeyUri,
                        fallbackAnswerText = answerKeyText,
                        durationMinutes = finalDuration
                    )
                } else {
                    viewModel.convertFilesToCbt(
                        testTitle = resolvedTitle,
                        questionPdfUri = selectedPdfUri,
                        answerKeyUri = selectedAnswerKeyUri,
                        fallbackQuestionText = questionPaperText,
                        fallbackAnswerText = answerKeyText,
                        durationMinutes = finalDuration,
                        pdfFileName = selectedPdfFileName ?: "Uploaded_JEE_Paper.pdf"
                    )
                }'''
    if old_call not in s:
        raise SystemExit("UI generate-call marker not found")
    s = s.replace(old_call, new_call, 1)
    ui.write_text(s, encoding="utf-8")

print("Direct PDF link patch applied successfully.")

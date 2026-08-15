# JSON import implementation note

The generic importer is deliberately separate from Gemini. Any external generator can output the documented JSON schema. KARAM reads the file locally, validates it, saves it into the local Test Library, and opens it in the normal CBT flow.

The source upload UI should keep two independent source slots: `Question Paper` and `Official Answer Key`. These are for PDF/answer-key extraction workflows only. A ready JSON test uses the `Import JSON Test` path and does not require either source file.

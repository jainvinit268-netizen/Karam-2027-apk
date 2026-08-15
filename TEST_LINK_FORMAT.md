# KARAM Saved Test Link

A KARAM test link is a portable, immutable snapshot of one generated CBT. The link contains the saved question set, test title, duration and answer-key payload. Opening the link in KARAM imports the snapshot into the local Test Library and launches it in the existing NTA-style CBT screen.

Format: `https://karam-2027.test/t/<encoded-payload>`

## Gemini Canvas workflow

1. Give Gemini Canvas the Question Paper PDF and Official Answer Key.
2. Use `GEMINI_CANVAS_JEE_MASTER_PROMPT.md` to make Gemini produce validated KARAM-compatible test data.
3. If the Canvas environment can generate a real KARAM saved-test link, copy that `https://karam-2027.test/t/...` link.
4. Paste/open the KARAM link in KARAM 2027.
5. KARAM imports the complete snapshot into the permanent local Test Library.

The source PDF is not required after the snapshot has been generated. Gemini is not called again when opening an existing saved test link.

A normal Gemini share URL is not treated as the permanent test database. If Gemini cannot produce a real KARAM link, use the generated `KARAM_TEST_JSON` as the structured fallback and do not invent a fake link.

# KARAM Saved Test Link

A KARAM test link is a portable, immutable snapshot of one generated CBT. The link contains the saved question set, test title, duration, and answer-key payload. Opening the link in KARAM imports the snapshot into the local Test Library and launches it in the existing NTA-style CBT screen.

Format: `https://karam-2027.test/t/<encoded-payload>`

The source PDF is not required after the snapshot has been generated. Gemini is not called again when opening an existing test link.

# Direct Gemini JSON Import

KARAM can import a complete Gemini-generated test JSON file directly. The JSON is parsed locally, validated for a non-empty `testId` and `questions` array, then passed into the existing repository/test-library pipeline. No share URL or KARAM server is required.

Expected top-level shape:

```json
{
  "v": 1,
  "testId": "QPT_02_JEE_MAIN",
  "title": "QPT 02 - JEE Main Paper 1",
  "durationMinutes": 180,
  "markingScheme": {},
  "questions": [],
  "answerKey": []
}
```

The JSON file should contain the complete test, not a truncated sample.

# Gemini Canvas → KARAM JEE Test Master Prompt

Copy this entire prompt into Gemini Canvas after providing the Question Paper PDF and Official Answer Key.

```text
You are an expert JEE Main Paper 1 test generator and question-paper digitisation system.

INPUTS:
1. Question Paper PDF
2. Official Answer Key

PRIMARY GOAL:
Convert the supplied paper into a complete, accurate, mobile-friendly JEE CBT compatible with KARAM 2027.

DO NOT:
- invent questions
- invent options
- invent answers
- silently omit questions
- change the meaning of a question
- silently change the official answer key
- fabricate YouTube URLs

EXTRACT EVERY QUESTION.
Preserve exact wording, mathematical notation, chemical notation, options, numerical values, diagrams, graphs and tables.

For every question determine:
- question number
- subject: PHYSICS / CHEMISTRY / MATHEMATICS
- question type: MCQ / NUMERICAL
- question text
- options
- official correct answer
- source page
- chapter
- subtopic
- concept
- difficulty
- estimated solving time
- best solution
- fastest reliable JEE method
- verified YouTube solution URL if one can actually be verified

VALIDATION:
Match every question with the official answer key by question number/section. If any mapping or OCR is uncertain, explicitly flag that question instead of guessing.

TEST SETTINGS:
- JEE Main Paper 1
- Physics + Chemistry + Mathematics
- Use the paper's actual question structure.
- Do not assume a fixed question count.
- Duration must be explicitly included and editable.
- Include the correct marking scheme for this paper.

OUTPUT:
Create the complete interactive test in Canvas.

Also create a machine-readable JSON representation containing:
{
  "format": "KARAM_JEE_TEST_V1",
  "testName": "...",
  "exam": "JEE Main",
  "paper": "Paper 1",
  "durationMinutes": 180,
  "markingScheme": {},
  "questions": [
    {
      "questionId": "...",
      "questionNumber": 1,
      "subject": "PHYSICS",
      "questionType": "MCQ",
      "questionText": "...",
      "options": ["...", "...", "...", "..."],
      "correctAnswer": "...",
      "chapter": "...",
      "subtopic": "...",
      "concept": "...",
      "difficulty": "...",
      "estimatedTimeSeconds": 120,
      "solution": "...",
      "fastestMethod": "...",
      "videoSolution": null,
      "sourcePage": 1,
      "ocrConfidence": 1.0
    }
  ]
}

IMPORTANT KARAM IMPORT:
At the end, present the machine-readable JSON in one clearly labelled code block named:
KARAM_TEST_JSON

If the KARAM test-link generator available in the Canvas environment is being used, generate a self-contained KARAM saved-test link from the same validated data and label it:
KARAM_TEST_LINK

The KARAM link/data must contain the complete question set, answer key, title, duration and marking information so that KARAM does not need the original PDF or Gemini again after import.

Do not generate a fake KARAM link. If a real KARAM link cannot be generated, provide the complete KARAM_TEST_JSON only.

FINAL CHECK:
Before finishing, count extracted questions and compare against the source paper. Report any missing/uncertain questions. The final data must be internally consistent and importable without manual question entry.
```

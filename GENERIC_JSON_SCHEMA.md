# Generic KARAM JSON schema

`questions` is mandatory. Each question requires `questionId`, `questionNumber`, `subject`, `questionType`, `questionText`, and `correctAnswer`. MCQ questions require `options`. Additional fields such as solutions, difficulty, chapter, subtopic, concept, estimatedTime, sourcePage, diagrams, and videoSolution are accepted and preserved by the KARAM test model when supported.

Supported top-level fields:

- `v`
- `testId`
- `title`
- `exam`
- `durationMinutes`
- `markingScheme`
- `questions`
- `answerKey`

Provider-specific fields are allowed and ignored when not needed. The importer is not coupled to Gemini.

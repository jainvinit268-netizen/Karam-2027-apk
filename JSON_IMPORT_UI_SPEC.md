# JSON import UI

The app should expose `Import JSON Test` directly from the Test Library/home conversion area.

Flow:

1. Tap Import JSON Test.
2. Android file picker accepts `.json`.
3. Validate and parse locally.
4. Save the test into the existing local Test Library.
5. Open it in the normal CBT screen.
6. Show a validation error if required fields are missing.

Separate source upload controls:

- `Question Paper` -> PDF picker
- `Official Answer Key` -> PDF/JSON picker

These two files are independent and must not be forced into the JSON import path.

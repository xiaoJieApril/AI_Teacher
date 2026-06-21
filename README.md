# AI Teacher Learning App

An Android-only learning app prototype for a strict but patient AI private teacher. The teacher covers Japanese, English, drawing, and fitness, then assigns work, creates personalized schedules, grades homework, and pushes the student back into focused study.

## What is included

- Kotlin + Jetpack Compose Android app.
- AI Teacher system prompt with a strict JSON response contract.
- Gemini API repository with text and image homework support.
- Room storage for tasks, chat messages, graded homework, and focus sessions.
- Daily task assignment with title, subject, suggested minutes, and completion standard.
- Daily and weekly AI-generated schedules with Today/Week views.
- Homework grading with score, strengths, problems, corrections, and next action.
- Text input, speech input, photo capture, and image picker submission.
- Focus timer UI with Android screen pinning support.

## AI provider

The default implementation uses Gemini because this app needs multimodal feedback for drawings, handwritten notes, Japanese, and English. The current client default is:

```kotlin
gemini-3.5-flash
```

If that model is unavailable for your API account, change the default in `GeminiTeacherClient.kt` to a Gemini Flash model that is enabled for your key.

## Run

Open this folder in Android Studio and sync Gradle.

Create `local.properties` with:

```properties
GEMINI_API_KEY=your_api_key_here
SUPABASE_URL=https://your-project-ref.supabase.co
SUPABASE_ANON_KEY=your_supabase_anon_key
```

Then run the `app` configuration.

For Supabase, run `docs/supabase-schema.sql` in the SQL editor first. This prototype uses anonymous single-device sync and keeps Room as the UI source of truth.

## Notes

Android apps cannot truly lock the device. This prototype uses a focus timer and starts Android screen pinning when available. A stronger blocker can be added later with a foreground service plus overlay permission, but that requires careful permission UX.

# Cue

**Cue** is an offline Android study app that combines note-taking with spaced-repetition flashcards. Instead of keeping your notes in one app and your flashcards in another, Cue lets you write a note and turn its key ideas into question-and-answer cards right inside the note. When you are ready to study, the app builds a review session from the cards that are due and uses those answers to track your progress over time.

Everything is stored locally on the device. There are no accounts, no server, and no internet connection required.

---

## Table of contents

- [What the app does](#what-the-app-does)
- [How it works](#how-it-works)
  - [1. Subjects, notes, and cards](#1-subjects-notes-and-cards)
  - [2. Review sessions](#2-review-sessions)
  - [3. Spaced repetition](#3-spaced-repetition)
  - [4. Progress and settings](#4-progress-and-settings)
- [Screens](#screens)
- [Tech stack and architecture](#tech-stack-and-architecture)
- [Project structure](#project-structure)
- [Getting started](#getting-started)
- [Running the tests](#running-the-tests)
- [Data and privacy](#data-and-privacy)
- [AI usage statement](#ai-usage-statement)
- [Author](#author)

---

## What the app does

Cue helps a student follow a simple study loop:

1. **Organize** your subjects (for example, Biology or Calculus).
2. **Write notes** for a subject, with a title, body text, and comma-separated tags.
3. **Create flashcards** from a note, each with a question and an answer.
4. **Review** the cards that are due, one at a time, and mark each answer as *Got it* or *Missed*.
5. **See progress**: how many cards you reviewed today, your accuracy, your study streak, and how many cards are in each review stage.

The app is built for phones (it supports Android 8.0 / API 25 and above) and uses Material 3 with a clean, rounded, single-accent-color design, a floating bottom navigation bar, light/dark themes, and animated screen and card transitions.

---

## How it works

### 1. Subjects, notes, and cards

The data is organized in a hierarchy:

```
Subject  →  Note  →  Card  →  ReviewLog
```

- A **subject** has a name and a color, used to visually identify it.
- A **note** belongs to one subject. It has a title, body, tags, and timestamps.
- A **card** belongs to one note. It stores a question, an answer, a review "box" (1–3), and the date the card is next due.
- A **review log** record is written every time a card is answered. These records power the statistics on the Progress screen.

Deleting a parent removes its children (foreign-key cascade): deleting a subject deletes its notes, cards, and review history. Deleting a note deletes its cards. The app asks for confirmation before destructive actions and offers **Undo** through a snackbar for deleted subjects, notes, and cards.

Notes can be searched by title or body text, filtered by tag, and sorted either by last update or alphabetically by title. Tags are entered as comma-separated text and are cleaned up (trimmed, lowercased, and deduplicated) when the note is saved.

### 2. Review sessions

Review can be started from the bottom navigation (all due cards) or from a subject's Notes screen (only that subject's due cards).

1. The app queries cards whose `next_review_date` is already past due, ordered by the oldest due date first.
2. Each card shows its question. Tapping the card flips it with a 3D animation to reveal the answer.
3. The student taps **Got it** or **Missed**.
4. That answer updates the card's box and next review date, writes a review-log entry, and advances to the next card.
5. When the queue is finished, a summary shows the session's score and percentage. If nothing is due, the app shows an "all caught up" state instead.

A live progress bar and counter (`current / total`) show where you are in the session.

### 3. Spaced repetition

Cue uses a simple three-box (Leitner-style) scheduler in `ReviewBoxes`:

| Box | Stage | Review interval |
| --- | --- | --- |
| 1 | New | 1 day |
| 2 | Learning | 3 days |
| 3 | Mastered | 7 days |

Rules:

- A **correct** answer moves the card up one box (capped at box 3), and its next review is scheduled using the new box's interval.
- An **incorrect** answer sends the card back to box 1, so it comes back sooner.
- A card is "due" when its next review date is less than or equal to the current time.

This logic lives in `data/domain/review/ReviewScheduler.kt` as plain Kotlin functions, which keeps it easy to unit test without Android.

### 4. Progress and settings

The **Progress** screen reads the review log and card table to show:

- cards reviewed since midnight today,
- lifetime accuracy (correct answers ÷ all answers),
- the current study streak (consecutive days with at least one review, counting today or yesterday),
- a distribution of cards across boxes 1, 2, and 3.

The **Settings** screen stores preferences with DataStore Preferences:

- **Theme** — light, dark, or follow the system,
- **Daily goal** — a saved target from 5 to 100 cards (in steps of 5),
- **Sort by title** — the default note ordering,
- **Clear all data** — removes all subjects, notes, and cards after confirmation.

Preferences persist across app restarts. Cue currently uses the daily goal as a saved preference; a review session still includes every card that is due.

---

## Screens

| Screen | What you can do there |
| --- | --- |
| **Subjects** | Add, rename (with color), search, and delete subjects; see the total due-card count; start a review of all cards. |
| **Notes** | Search, filter by tag, open, add, and delete notes for a subject; review only that subject's cards. |
| **Note editor** | Edit title, body, and tags; add, edit, and delete flashcards; get a warning for unsaved changes. |
| **Review** | Flip through due cards, mark *Got it* / *Missed*, follow the progress bar, and see a session summary. |
| **Progress** | View today's reviews, accuracy, streak, and how cards are distributed across review boxes. |
| **Settings** | Change theme, daily goal, and note sort order, or clear all app data. |

Navigation is handled by Navigation Compose. The four main destinations live in a floating bottom bar; the Notes and Note Editor screens are detail screens reached by tapping a subject or note.

---

## Tech stack and architecture

The app follows an MVVM-style structure using the recommended Android architecture components:

- **Kotlin** and **Jetpack Compose** (Material 3) for the UI
- **Navigation Compose** for routing between screens
- **ViewModel + StateFlow / Flow** for UI state and reactive data
- **Room 3** (with KSP code generation) for local persistence
- **DataStore Preferences** for settings
- **Kotlin Coroutines** for database and asynchronous work
- **JUnit** for local unit tests, plus instrumented tests for the database
- **Gradle version catalog** (`gradle/libs.versions.toml`) for dependency versions

Data flows in one direction:

```
Room DAO / DataStore  →  Repository  →  ViewModel (StateFlow)  →  Compose screen
                                    ↑                         |
                                    └────── user actions ─────┘
```

Key build settings: namespace/application ID `ph.edu.comteq.cue`, `minSdk 25`, `targetSdk` and `compileSdk` 37, version 1.0.

---

## Project structure

```
cue/
├── app/
│   └── src/
│       ├── main/
│       │   ├── java/ph/edu/comteq/cue/
│       │   │   ├── MainActivity.kt              # App entry point, theme, navigation, bottom bar
│       │   │   ├── data/
│       │   │   │   ├── datastore/                # Settings (theme, goal, sort order)
│       │   │   │   ├── domain/review/            # Box intervals and scheduling logic
│       │   │   │   ├── local/                    # Room database, entities, DAOs
│       │   │   │   └── repository/               # Data access for ViewModels
│       │   │   └── ui/
│       │   │       ├── components/               # Shared headers, search field, loading state
│       │   │       ├── notes/                    # Notes list and note editor
│       │   │       ├── progress/                 # Progress screen
│       │   │       ├── review/                   # Review screen
│       │   │       ├── settings/                 # Settings screen and dialogs
│       │   │       ├── subjects/                 # Subjects screen and dialogs
│       │   │       └── theme/                    # Colors, typography, shapes, Material theme
│       │   └── res/                              # Manifest, launcher icon, theme resources
│       ├── test/                                 # Local unit tests (review logic, stats)
│       └── androidTest/                          # Instrumented Room database tests
├── gradle/                                       # Wrapper and version catalog
├── build.gradle.kts / settings.gradle.kts
└── README.md
```

---

## Getting started

### Prerequisites

- Android Studio (latest stable) with the Android SDK installed
- JDK 17
- An emulator or a physical Android device running API 25 or higher
- Git

> `local.properties` is intentionally not committed. Android Studio generates it with your local SDK path when you open the project.

### Open and run

1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd cue
   ```
2. Open the `cue` folder in Android Studio and let Gradle sync download the dependencies.
3. Select an emulator or connected device, then press **Run** (or use `./gradlew installDebug`).

Useful command-line builds from the project root:

```bash
./gradlew assembleDebug          # Build the debug APK
./gradlew installDebug           # Install it on a connected device/emulator
./gradlew test                   # Run local unit tests
```

The debug APK is produced at `app/build/outputs/apk/debug/`.

### First steps in the app

1. Tap **+** on the Subjects screen and add a subject.
2. Open the subject, tap **+**, and write a note. Add a few comma-separated tags if you like.
3. In the note editor, tap **Add flashcard** and enter a question and answer.
4. Tap **Save** to keep the note.
5. Tap **Review** (new cards are due immediately), flip each card, and answer it.
6. Open **Progress** to see your streak, accuracy, and card distribution.

---

## Running the tests

- **Local unit tests** (`app/src/test`) cover the review scheduler (box promotion, reset to box 1, due-date filtering, session ordering) and the progress calculations (streak and accuracy).
  ```bash
  ./gradlew testDebugUnitTest
  ```
- **Instrumented tests** (`app/src/androidTest`) exercise the Room database with an in-memory/on-device database and require a connected device or emulator.
  ```bash
  ./gradlew connectedAndroidTest
  ```

---

## Data and privacy

- Cue is fully offline: it declares no internet permission and makes no network calls. There is no AI functionality inside the app.
- Notes, cards, and review history are saved in a local Room database named `cue.db`.
- Preferences are saved in DataStore under `cue_settings`.
- Data stays on the device unless the user backs up or transfers the device. **Settings → Clear all data** permanently removes app data after confirmation.

---

## AI usage statement

AI tools were used during planning, learning, and drafting parts of the code and design. The full disclosure, including what was AI-assisted and what was done independently, is in [`Cue - AI Usage Statement.docx`](./Cue%20-%20AI%20Usage%20Statement.docx) at the repository root.

---

## Author

Turno, Kyle Tristan D. — 4th Year BSIT, COMTEQ Computer and Business College.

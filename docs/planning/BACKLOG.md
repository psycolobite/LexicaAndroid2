# Backlog Lexica Android

## High Priority (Bugs & Blockers)
- [ ] **Fix Mini-Games Crash**: The application crashes when launching mini-games. Suspected issue in Navigation graph or arguments.
- [ ] **Fix Wiktionnaire API**: The scraper returns 0 results. Needs robust HTML parsing and better error handling.

## Sprint 5: Content & Features
- [ ] **Import Full Dictionary (7000 words)**:
    -   Retrieve the JSON from the Python project.
    -   Optimize `DataImporter` for large files (check memory usage).
- [ ] **Data Sync (Firebase)**:
    -   Authentication (Email/Google).
    -   Save progression (Cloud Firestore).
    -   *Constraint*: Handle "Merge Conflict" between local and cloud data (User choice logic).

## Sprint 6: Gamification
- [ ] **Experience System (XP)**:
    -   Gain XP per review/game.
    -   Levels/Badges.
- [ ] **Mini-Games V2**:
    -   Spelling Game (Audio -> Write).
    -   Review/Fix current games (Crash fix).

## Sprint 7: User Experience
- [ ] **Search & Explore**:
    -   Search bar for the user.
    -   "Suggested Content" based on user level.
- [ ] **Tutoriel**: First launch onboarding.


## Future Improvements
- [ ] **UI Refinement**: Polish the Jetpack Compose UI to match modern standards.
- [ ] **Statistics**: Advanced learning stats.


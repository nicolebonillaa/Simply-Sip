# Simply Sip

A mobile app for CSUN students to discover and filter drinks near campus based on dietary restrictions, allergies, calories, sugar content, and nutrition preferences.

## Tech Stack
- Frontend: React Native, Expo, React Navigation, react-native-maps
- Backend: Java (Spring Boot), MySQL
- API: REST with JWT authentication

## Team
- Nicole Uribe — Team Lead / Scrum Master, Frontend Lead
- Angelo Rodriguez — Database Administrator, Backend Lead
- Cosette Espino — Graphic Designer, Backend
- Amin Zoghlami — Frontend, Backend
- Faith Hill — Frontend
- Danielle Pacheco — Backend

## Getting Started
1. Clone the repo 
2. Run `npm install`
3. iOS: `bundle install`, then `npx pod-install`, then `npx expo run:ios`
4. Android: requires Android Studio with the Android SDK installed — then `npx expo run:android`

## Day-to-Day Development

Once you've done the setup above, you don't need to repeat all of it every time. Typical workflow:

1. Pull latest changes: `git pull`
2. If `package.json` changed: `npm install`
3. If native dependencies changed (new libraries added, iOS files modified): `npx pod-install` (iOS only)
4. Start the app:
   - `npx expo start` — fastest, for previewing JS/UI changes (press `i` for iOS Simulator, `a` for Android emulator)
   - `npx expo run:ios` / `npx expo run:android` — use this instead if you added a new native dependency, since Expo Go / a plain start won't pick up native changes

You only need to re-run `pod-install` or a full `run:ios`/`run:android` build when native code changes (new packages, Podfile edits, native config). For everyday JS/UI work, `npx expo start` and Fast Refresh handle it — no rebuild needed.

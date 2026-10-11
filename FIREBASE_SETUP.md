# Firebase setup (Part 3)

1. Create a project at https://console.firebase.google.com/
2. Add an Android app with package name `com.goldfin.goldfinbudgeting` (must match `applicationId` in `app/build.gradle.kts`)
3. Download `google-services.json` into **`app/google-services.json`**
   - This file is **gitignored** — do not commit it (GitHub will flag the API key)
   - Teammates each download their own copy from Firebase, or share the file privately
4. Enable **Authentication → Email/Password**
5. Create a **Cloud Firestore** database (test mode is fine for demos)
6. Create accounts in the app with a real email — the old Room login `1` / `1` is not valid Firebase email

See `app/google-services.json.example` for the expected file shape (no real keys).

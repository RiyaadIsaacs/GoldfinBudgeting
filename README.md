# Goldfin Budgeting

**Module:** OPSC7311 (Open Source Coding)  
**Assessment:** Portfolio of Evidence (PoE)  
  
**Group members:**  

- Riyaad Isaacs - ST10453030
- Liam Fullard - ST10438878  
- Ndzalama Khosa - ST10438552
- Lazjessicah van der Zee - ST10435229  



**Repository:** [https://github.com/[organisation-or-username]/GoldfinBudgeting](https://github.com/[organisation-or-username]/GoldfinBudgeting) 

---
### Setup Instructions

#### Run Android Studio Emulator:
1. Clone or download the project zipfile from GitHub.
2. Open the project in Android Studio and wait for the project to finish loading.
3. Run the app on the emulator by clicking the play icon at the top right of the screen.
4. Wait for the Emulator to finish loading and for the App to open automatically or click on the GoldFin app icon to open the app.
5. Create a new account with a new email and password or sign in with an existing account.
6. Explore the app's Home, Expenses, Envelopes, Profile, Badges, and AI Assistant pages as well as the Edit Expenses, Edit Envelopes, and Monthly Expenes Graph features.
7. Logout when your finished with the app.

#### Install and Run Android APK File:
1. Send the .apk file to your Android device.
2. Open your phone's Files or Downloads app and tap the .apk file.
3. If prompted by a security popup, go to settings and turn on "Allow from this source".
4. Tap Install. If Play Protect prompts a warning, tap "Install anyway".
5. Ensure Wi-Fi is connected or data is turned on so that the app can connect to Firebase in order for the login to work.
6. Tap Open to launch the app from your home screen.

---
### How to connect Ollama (AI Assistant Feature (optional))

1. Open powershell windows.
2. Type in: ollama pull llama3.
3. In the same powershell window, type in: ollama serve (if it gives an error that means Ollam is still running in the background. Quit ollama and try again).
4. Then run the emulator in Android Studio. Wait for it to finish loading.
5. When the emulator is done loading, open a second powershell window.
6. In the second powershell window, copy and paste the following:

      & "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" reverse tcp:11434 tcp:11434
      
7. Keep both powershell windows open when using the emulator.

Note: Ollama is optional and is not required to run the application. The budgeting app and all its features function fully without an Ollama connection.

---
### Unique App Features

1. Optional Daily Challenges: Gamification feature where users get daily saving challenges that give them XP and help them level up towards claiming achievement badges.
2. First-Time User Challenges: Gamification feature where new users have to interact with all the main app features in order to earn XP and level up towards claiming achievement badges.
3. AI App Assistant: An innovative feature where an AI assistant helps new users by answering questions regarding the app's features and where certain features/functions are located and how to navigate to that section.

---

### Video Presentation Links

Part 2 Google Drive link: https://drive.google.com/file/d/1Zvu7crRPt1ofTaOTOw9cpOp_B9Ua7-f9/view?usp=sharing

Part 3 Google Drive link: https://drive.google.com/file/d/12ngoBOIPucl-IH6apoFo8RLw8IHTPdSc/view?usp=sharing

---

## Project Report
(Word count: 1286)

### Introduction 
The purpose of the following section is to outline the purpose of our personal budgeting app, GoldFin, highlighting the key design considerations that shaped it, s well as how GitHub and GitHub Actions were utilised during the development of the application.

### Purpose of the App
The purpose of Goldfin is to provide users with a structured and simple way to track their spendings allocate income and develop better financial awareness and financial habits the app addresses the common difficulty of managing everyday money with clear visibility of where funds go or how close a person is to their own limits the application does this by combining the envelope budgeting approach with color-coded progress bars and graphs so that the user can see their financial status at a glance next.

Goldfin is aimed at individuals partners and families with the primary target audience being young adults who want a simple user-friendly system for daily financial management users will be able to create an account login securely, set monthly minimum and maximum spending goals for different categories called envelopes, and make expense entries with an amount, date optional start and end times, description, category or envelope it belongs to, and an optional photo or receipt that can be attached to the entry. Users can also view spending history, filter it by date or category, view total and category spending reports, search for expenses, edit or delete entries, and monitor progress toward budget limits.

Furthermore, collaborative use is also supported through multi-account linking so partners or family members can share a budgeting plan. The app also features gamification elements involving XP, levels, badges, and optional daily saving challenges, as well as a local AI chatbot to assist users in navigating and becoming familiar with the app's features, all to make the app more engaging and helpful.

### Design Considerations
The design decisions for GoldFin were guided by the findings from the research conducted during Part 1 where the existing Android applications – Monarch Money,  Goodbudget and Vault22 – were analyzed so that the prototype would present the relevant functionalities and intuitive interface patterns.

The envelope system was chosen as it promotes proactive allocation of income into specific categories a method that the research showed can support mindful spending and awareness. Each envelope has its own minimum and maximum goals set by the user so the user can see whether a category is approaching its limit before the month ends.

Visual progress trackers, color-coded indicators, and a total overview on the home screen were also included as the compared applications showed that users understand their financial position more quickly when key figures appear in colour and on designated screens rather than cluttered into one screen or scattered across multiple. The colour also shows the user at a glance whether their spending is still within their set limits or has reached their max limit.

Additionally, Profile, Home, and Expenses are located on the bottom bar because recording and reviewing spending are the main tasks the user will utilize the most, with Profile being easily accessible if the user wants to change sign in or profile information.

The side menu, accessed through the hamburger icon, holds envelopes, accounts, badges, and the AI assistant which are used when sitting up or checking something rather than logging a purchase. Additionally, the app ensures to keep related actions together such as category limits on the envelope screen, expense entry and receipt attachment on the edit expenses, badges and challenges on the badger screen, and questions about how to use the app on the AI assistant screen. This reduces the number of steps for a common task and keeps each screen from becoming crowded or too scattered, allowing for easy navigation of the app.

Furthermore gamification was included as the app is designed to engage users and help them develop good financial habits through checking in on one's financial state and challenging oneself to spend money sparingly and not only store records. XP, levels, badges and the optional daily saving challenges give users a small sign of progress and sense of achievement and are situated on their own screen so that Home, Envelopes, and Expenses stay focused on the budget.

Another unique feature the app presents is its AI assistant which is there to help users find, navigate, and operate features in the app. It makes the app completely beginner friendly which aligns with the intended young adult target audience who want to start managing their finances. The application is designed for app navigation only so does not give financial advice or analyze transactions. It also runs locally through Ollama rather than through an external AI service so questions about using the app are not sent to a third party chatbot, promoting privacy and security which are essential for budgeting apps.

Furthermore, as for storing user entries, the user's entries are stored in Firebase with Firebase Authentication supporting secure login and Cloud Firestore holding the envelope's expenses and user profile information. Firestore also keeps a local copy of recent data on the device so that the entries can be opened during a short loss of connection and synced again when the device is back online.

Overall, these design choices shaped by the research conducted during Part 1 keep spending limits easy to see, frequent tasks close at hand, and app navigation intuitive.

### GitHub & GitHub Actions Utilisation
During development, GitHub was used to keep a shared history of the project. Each change was committed with a clear title and a short note, so the other team members could see what had been changed and why. GitHub Actions was also then utilised to run on its own whenever code was pushed or a pull request was opened and ran the expense tests and built the application so that the checks did not have to be started manually.

For the tests with GitHub Actions, it analyzed the expense rules rather than the Android screens and are located in ExpenseLogic which is a plain Kotlin object independent from the Android emulator and/or database. The screens call this object when they filter expenses, add up totals, handle dates, and then check goals. As a result, the same roles could be tested on an ordinary computer without starting an emulator or manually testing the database.

The unit tests check the main behaviors of ExpenseLogic. These include filtering by month and category, searching names and descriptions, selecting a date range, calculating totals, checking goals and spending status, working at progress bar percentages, and turning an expense into the form the app saves.

The above tests were then started from .github/workflows/build.yml where the workflow downloads the repository, installs the Java Development Kit (JDK 17) and the Android platform-tools package, runs the unit tests, and then finally builds the app for testing. This build then creates a debug Android Package, aka an APK, a released APK, and a released app bundle before uploading the files to the workflow run on GitHub. The first run originally failed because the workflow tried to install an old SDK (Software Development Kit) package that no longer existed. This was easily fixed by installing only platform-tools so that the app could be compiled and tests could successfully run.

Overall using the tools GitHub and GitHub Actions offered gave the team a shared record of each change and an automatic check that the expense rules still passed before new work was merged, changed, or added, keeping the project easier to follow, manage, and reduced the changes of a broken build going unnoticed.

### App Images
#### App Icon, Login Screen, and Home Screen

<img width="80%" alt="App Icon, Login Screen and Home Screen" src="https://github.com/user-attachments/assets/6fa87529-a95e-4ba0-8d80-63805407997b" />

Figure 1: Canva, 2026. App Icon, Login Screen and Home Screen. (Canva, 2026).

---
#### Envelopes and Edit Envelopes Screens

<img width="80%" alt="Envelope Screens" src="https://github.com/user-attachments/assets/147e370c-5781-4950-914f-cf9bc6ba1967" />

Figure 2: Canva, 2026. Envelope Screens. (Canva, 2026).

---
#### Expenses and Expenses Edit Screens. Plus Achievement Notification and Visual Graph of Monthly Expenses

<img width="80%" alt="GoldFin Expenses Edit, Achievement   Graph " src="https://github.com/user-attachments/assets/5f871e45-0ee4-44b7-8ebe-3d4b39d25f07" />

Figure 3: Canva, 2026. GoldFin Expenses Edit, Achievement & Graph. (Canva, 2026).

---
#### Gamification Features

<img width="80%" alt="Gamification features" src="https://github.com/user-attachments/assets/745c0e66-a86a-4a3a-808f-75e2f69f4039" />

Figure 4: Canva, 2026. Gamification features. (Canva, 2026).

---
#### AI Assistant Feature

<img width="80%" alt="AI Assistant" src="https://github.com/user-attachments/assets/da1abcd7-707b-40de-979e-f63e9374cb6a" />

Figure 5: Canva, 2026. AI Assistant. (Canva, 2026).

---

### Conclusion
In conclusion, this report outlined the purpose of Goldfin, the main design choices made to support that purpose, and the practical use of GitHub for version control alongside the GitHub Actions for testing and building during the development of the app.



---

### References (utilised for development)

Alt-Codes, [s.a.]. Star Symbols Alt Codes. [online] Available at: <https://www.alt-codes.net/star_alt_code.php> [Accessed 02 September 2026].

Android Developers, [s.a.]. Connect UI components to NavController using NavigationUI. [online] Available at: <https://developer.android.com/guide/navigation/integrations/ui> [Accessed 03 September 2026].

Android Developers, [s.a.]. Create dynamic lists with RecyclerView. [online] Available at: <https://developer.android.com/develop/ui/views/layout/recyclerview> [Accessed 04 September 2026].

Android Developers, [s.a.]. Display content edge-to-edge in views. [online] Available at: <https://developer.android.com/develop/ui/views/layout/edge-to-edge> [Accessed 06 September 2026].

Android Developers, [s.a.]. Save data in a local database using Room. [online] Available at: <https://developer.android.com/training/data-storage/room> [Accessed 12 September 2026].

Android Knowledge, 2023. Navigation Drawer Menu in Android Studio using Kotlin. [video online] Available at: <https://www.youtube.com/watch?v=sSL6a_ivRVk> [Accessed 03 September 2026].

Android Knowledge, 2023. Save Data in Firebase Realtime Database in Android Studio | Admin-Client Module Kotlin | CRUD - 1. [video online] Available at: <https://www.youtube.com/watch?v=gLfmQiFdZXU> [Accessed 12 September 2026].

Android Studio, [s.a.]. Adding Firebase to Android Application. [online] Available at: <https://developer.android.com/studio/gemini/use-a-local-model>[Accessed 12 October 2026].

Asoyan, A., 2025. How to Run Local LLM (AI) in Android Studio. [online] Available at: <https://artemasoyan.medium.com/how-to-run-local-llm-ai-in-android-studio-6bb142ee64ec>[Accessed 10 October 2026].

CodeCraft, 2026. (283) Creating Gradient Drawables in Android Studio | Android UI Design Tutorial. [video online] Available at: <https://youtu.be/-J9zNq7QA1w?si=VNgMjlutJ7JovCYK> [Accessed 03 September 2026].

Codetutor, 2017. RecyclerView - Part 2, Introduction to BaseAdapter. [video online] Available at: <https://youtu.be/2W8XkpmjcMo?si=0TUQ3lBRuIIZYMLl> [Accessed 05 September 2026].

CodingSTUFF, 2024. The Complete Beginner Guide for Room in Android 2024 | Local Database Tutorial for Android - Part 1. [video online] Available at: <https://youtu.be/r_UfOz3yaLg?si=cd8swb2EiXxvQ34T> [Accessed 12 September 2026].

Dev Leonardo, 2023. Understanding GitHub Actions - Automated Testing. [video online] Available at: <https://youtu.be/WW6ZUw9IExA?si=4kJ06pVRzHR_6xRP> [Accessed 14 September 2026].

DevOps Topics, 2026. Github Actions Tutorial |how to automate unit testing using Github Actions| Run Automated Unit Tests. [video online] Available at: <https://youtu.be/_it4ascr23U?si=53J4XUZotN_HjnXi> [Accessed 13 September 2026].

DJ Malone, 2019. Using Kotlin's listOf and creating a Data Class (Android Studio 2019 Edition, Part 15). [video online] Available at: <https://youtu.be/oFli6rYejUQ?si=Py0-Am6AnSHQOIrl> [Accessed 05 September 2026].

Firebase Documentation, [s.a.]. Add Firebase to your Android project. [online] Available at: <https://firebase.google.com/docs/android/setup>[Accessed 12 October 2026].

Firebase Documentation, [s.a.]. Connect to Firebase. [online] Available at: <https://developer.android.com/studio/write/firebase>[Accessed 12 October 2026].

Firebase, 2023. Getting started with Firebase on Android. [video online] Available at: <https://youtu.be/jbHfJpoOzkI?si=dDqKS1dRAWyxZ8mR> [Accessed 12 October 2026].

Geeks for Geeks, 2025. Adding Firebase to Android Application. [online] Available at: <https://www.geeksforgeeks.org/android/adding-firebase-to-android-app/>[Accessed 12 October 2026].

Geeks for Geeks, 2025. Basic CI Workflow For Android using GitHub Actions. [online] Available at: <https://www.geeksforgeeks.org/android/basic-ci-workflow-for-android-using-github-actions/> [Accessed 14 September 2026].

GitHub Docs, 2026. Building and testing Java with Gradle. [online] Available at: <https://docs.github.com/en/actions/tutorials/build-and-test-code/java-with-gradle> [Accessed 13 September 2026].

Lackner, P., 2023. The FULL Beginner Guide for Room in Android | Local Database Tutorial for Android. [video online] Available at: <https://youtu.be/bOd3wO0uFr8?si=kEoLiaAA_2E3DySE> [Accessed 12 September 2026].

Mashkour, M., 2023. View Binding in RECYCLERVIEW ADAPTER in Android Studio | Kotlin. [video online] Available at: <https://youtu.be/N7rFkULNMpY?si=ZJ3gIgLWp8HvQ-IX> [Accessed 04 September 2026].

Master Coding, 2020. Adapters & Model Class | Android Tutorial #27. [video online] Available at: <https://youtu.be/AG-5Zt8b_6k?si=84ulOaApUlxsiwoy> [Accessed 05 September 2026].

Material Design, [s.a.]. Chips. [online] Available at: <https://m2.material.io/components/chips#input-chips> [Accessed 02 September 2026].

Mit, J., 2024. How to Set Up CI CD for Android Using GitHub Actions. [video online] Available at: <https://youtu.be/Fh4UyfqeB4Y?si=eNAMlRAaEBZnAKqR> [Accessed 14 September 2026].

Sandoval, A., 2026. Android Studio + Gemma4: Run Gemma4 Locally with Ollama. [online] Available at: <https://andresand.medium.com/android-studio-gemma4-run-gemma4-locally-with-ollama-30ef29d1908f>[Accessed 10 October 2026].

TechnoFunctionalLearning, 2026. How to Connect Offline AI Ollama Models with Android Studio. [video online] Available at: <https://youtu.be/-gWvQ28T9SI?si=FplRKk1Y8LwniX2I> [Accessed 09 October 2026].

The Independent Institute of Education, 2026. Activity 7: Introduction to ViewModel Student Notes App [OPSC7311]. nt. [online via internal VLE] The Independent Institute of Education. Available at: <https://mystudies.iie.edu.za/content/enforced/86376-OPSC7311_2026_FT_BCGD0701_EMGPSD_Term2_GR01/Guided%20Activity%207%20Introduction%20to%20viewModel%20.pdf> [Accessed Date 06 September 2026].

The Independent Institute of Education, 2026. Guided Activity 4: Working with Activities and Intents [OPSC7311]. nt. [online via internal VLE] The Independent Institute of Education. Available at: <https://mystudies.iie.edu.za/content/enforced/86376-OPSC7311_2026_FT_BCGD0701_EMGPSD_Term2_GR01/Guided%20Activity%204%20Creating%20a%20Second%20Activity%20and%20Navigating%20Between%20Screens%20v020.pdf> [Accessed Date 03 September 2026].

The Independent Institute of Education, 2026. Guided Activity 5 Student Notes App: Data Classes and Lists [OPSC7311]. nt. [online via internal VLE] The Independent Institute of Education. Available at: <https://mystudies.iie.edu.za/content/enforced/86376-OPSC7311_2026_FT_BCGD0701_EMGPSD_Term2_GR01/Guided%20Activity%205%20Data%20Classes%20and%20Lists%20v014.pdf> [Accessed Date 05 September 2026].

The Independent Institute of Education, 2026. Guided Activity 6: RecyclerView Student Notes App [OPSC7311]. nt. [online via internal VLE] The Independent Institute of Education. Available at: <https://mystudies.iie.edu.za/content/enforced/86376-OPSC7311_2026_FT_BCGD0701_EMGPSD_Term2_GR01/Guided%20Activity%206%20RecyclerView%20v010.pdf> [Accessed Date 05 September 2026].

The Zone, 2021. Android x Kotlin Beginner Tutorial [2021] #0 - Project Introduction. [video online] Available at: <https://youtu.be/GP4p3d7_b2k?si=gHtGcvlFWx7VadhV> [Accessed 03 September 2026].

The Zone, 2021. Android x Kotlin Beginner Tutorial [2021] #1 - Project Setup. [video online] Available at: <https://youtu.be/Q4uud_0hppw?si=ATqJqZMHMnUJXyC-> [Accessed 03 September 2026].

The Zone, 2021. Android x Kotlin Beginner Tutorial [2021] #2 - Build The Main UI. [video online] Available at: <https://youtu.be/U3xMvBYM55g?si=VHOkHM1xoFgpO85W> [Accessed 03 September 2026].

The Zone, 2021. Android x Kotlin Beginner Tutorial [2021] #3 - Setup the RecyclerView & Adapter. [video online] Available at: <https://youtu.be/atw2uYyX1ps?si=mR8xOAn3QtKz_pIA> [Accessed 06 September 2026].
The Zone, 2021. Android x Kotlin Beginner Tutorial [2021] #4 - Add A New Transaction Activity. [video online] Available at: <https://youtu.be/FphM4sDRZ9k?si=762Rk4TvYb7iZusP> [Accessed 06 September 2026].

The Zone, 2021. Android x Kotlin Beginner Tutorial [2021] #5 - Android Room Library for Database Management. [video online] Available at: <https://youtu.be/vdOh_Z6DP4w?si=ywLyyMWPug2Z8B_6> [Accessed 06 September 2026].

The Zone, 2021. Android x Kotlin Beginner Tutorial [2021] #6 - Swipe To Delete Within RecyclerView & ItemTouchHelper. [video online] Available at: <https://youtu.be/d87LYtLBSKA?si=NuTyqbLCXU-lb6df> [Accessed 07 September 2026].

The Zone, 2021. Android x Kotlin Beginner Tutorial [2021] #7 - Detailed View & Transactions Update. [video online] Available at: <https://youtu.be/t0hQl1G2Yg0?si=SZDQtymJvXso7dBR> [Accessed 07 September 2026].

Toptal Designers, [s.a.]. Trigram for Heaven. [online] Available at: <https://www.toptal.com/designers/htmlarrows/symbols/trigram-for-heaven/> [Accessed 02 September 2026].


---

### Referencing App Images (in report section)

Canva, 2026. AI Assistant. [electronic print] Available at: <https://canva.link/8kyiqy6q6g52ddh>[Accessed 10 October 2026].

Canva, 2026. App Icon, Login Screen and Home Screen. [electronic print] Available at: <https://canva.link/uxy1ec2facekvj0>[Accessed 10 October 2026].

Canva, 2026. Envelope Screens. [electronic print] Available at: <https://canva.link/bisgcsrum4rsw19>[Accessed 10 October 2026].

Canva, 2026. Gamification features. [electronic print] Available at: <https://canva.link/c35h4d75head67w>[Accessed 10 October 2026].

Canva, 2026. GoldFin Expenses Edit, Achievement & Graph. [electronic print] Available at: <https://canva.link/ilt36zkq6hl9ukq>[Accessed 10 October 2026].








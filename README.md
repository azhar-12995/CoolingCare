# Cooling Care — AC & Refrigeration Services (Android + iOS)

Customer app **and** shop admin app for Cooling Care, Pasrur. Built with **Compose Multiplatform** (one Kotlin UI for Android and iOS)
and **Firebase** (GitLive Kotlin SDK) from the Figma design: https://www.figma.com/design/ZiWzmk6xXI3pvnGTdUHjCH
(English + Urdu RTL screens incl. log-in and Admin Dashboard, prototype, handoff page with the Firebase architecture).

## How it works
- **Log in / sign up with the mobile number** (Firebase Phone Auth, 6-digit SMS code; Pakistani numbers only). New accounts add
  name + address once (screen 06); it is saved in `users/{uid}` and pre-fills every booking.
- **Place Order** saves the order in Firestore with a sequential number (CC-1001, CC-1002 … from a transaction) and uploads up to
  3 photos to Storage. A Cloud Function pushes it to every admin phone straight away.
- **Admins** are the phone numbers in Firestore `config/roles.adminPhones`. The same app opens on the Admin Dashboard for them:
  orders by status, Confirm / Propose another time / Technician left / Completed / Cancel (with reason), services manager
  (add, edit, open / close — closed services disappear from the customer app), business numbers and hours.
- Every admin change is pushed to the customer in their language; the customer's status screen updates live.
  Customers can cancel or ask for another time until the technician leaves (pushed to admins), and rate completed visits.
- Local reminders still run on the phone: 7 PM the day before, 1 h before the slot, maintenance 90 days after a completed visit.
- WhatsApp is kept for support chats and invoice requests.
- English and Urdu, switchable any time (Settings › Language). Urdu uses right-to-left layout and Noto Nastaliq Urdu.

## Firebase (project `cooling-care-pasrur`)
```
firebase/
  firebase.json, .firebaserc        emulators: auth 9099 · firestore 8085 · storage 9199 · functions 5001 · UI 4000
  firestore.rules                   who may read / write what (see Handoff page in Figma)
  storage.rules                     order photos (customer + admins) · service photos (public)
  firestore.indexes.json
  functions/                        onOrderCreated / onOrderUpdated → FCM pushes (EN / UR texts in messages.js)
  seed/seed.js                      services (from tools/catalog.json), business info, roles, order counter
  tests/rules-check.js              43 allow / deny checks against the emulator
```
**Status:** Firestore rules + indexes are deployed; Phone Auth is on with an SMS allow-list for Pakistan; test numbers
`+92 300 0000001 → 123456` (admin) and `+92 300 0000002 → 654321` send no real SMS; the debug SHA-1 / SHA-256 are registered.

**Still needed (owner):**
1. **Upgrade to the Blaze plan** — Cloud Functions (pushes) and the Storage bucket (photos) need it:
   https://console.firebase.google.com/project/cooling-care-pasrur/usage/details
   Then create the default Storage bucket (Console › Storage › Get started, region asia-south1) and deploy:
   ```
   cd firebase && firebase deploy --only storage,functions
   ```
2. **Admin numbers:** Firebase console › Firestore › `config/roles` › `adminPhones` (format `+923001234567`),
   or `node firebase/seed/seed.js --project cooling-care-pasrur --admin +923001234567`.
3. **Shop phone / WhatsApp / hours:** log in as admin › Settings › Business (stored in `config/business`, no app update needed).
4. **Android release:** add the release key and Play App Signing SHA-1 / SHA-256 to the Firebase Android app (Project settings).
5. **iOS:** upload an APNs auth key in Firebase › Project settings › Cloud Messaging.

## Local testing with the Firebase Emulator Suite
Needs Java 21+ (Android Studio's `jbr` works) and Node 22+.
```
cd firebase
set JAVA_TOOL_OPTIONS=-Djdk.net.unixdomain.tmpdir=%USERPROFILE%\.gradle   # Windows: avoids "Unable to establish loopback connection"
firebase emulators:start --project cooling-care-pasrur
node seed/seed.js --emulator          # services, business info, admin +923000000001, counter
node tests/rules-check.js             # 43 rule checks
```
App against the emulators (Android emulator reaches the PC at 10.0.2.2; debug builds allow plain HTTP only to it):
```
./gradlew :composeApp:installDebug -Pcc.useEmulators=true
```
SMS codes are not sent by the Auth emulator — read them from
`http://127.0.0.1:9099/emulator/v1/projects/cooling-care-pasrur/verificationCodes` or the emulator log.

## Project layout
```
composeApp/src/commonMain/kotlin/com/coolingcare/app
  App.kt                 navigation (type-safe routes), log-in / admin routing, notification deep links
  data/                  Cloud (Firebase: auth, profile, services, orders, admin actions, images), Repository (prefs +
                         cached profile / orders), Draft, Graph, Catalog.kt (generated offline defaults)
  domain/                Dates, Rules (phone formats, WhatsApp texts, validation), Reminders (planner + inbox)
  feature/               start · auth · home · services · book · status · bookings · settings · admin
  i18n/Strings.kt        every string in English + Urdu
  ui/                    theme (tokens from Figma), components (incl. Storage pictures, toasts), icons (generated Lucide)
composeApp/src/androidMain   Phone Auth (Activity), FCM service + channels, photo picker, AlarmManager reminders
composeApp/src/androidDebug  network config that lets debug builds reach the local emulators
composeApp/src/iosMain       Phone Auth (FIRPhoneAuthProvider), PHPicker / camera, UNUserNotificationCenter
iosApp/                      Xcode host: AppDelegate (Firebase, APNs, FCM token), Firebase SPM packages, push entitlement
firebase/                    rules, functions, seed, rule tests (above)
tools/                       gen.py (fonts, images, icons, catalog, Android icons), figma/ (design builder scripts)
store/                       listing text, privacy policy, icon, feature graphic, screenshots
```

## Build — Android
Requirements: JDK 17, Android SDK 36.
```
./gradlew :composeApp:testDebugUnitTest      # domain tests (16)
./gradlew :composeApp:assembleDebug          # debug APK (live Firebase)
./gradlew :composeApp:bundleRelease          # Play Store bundle (needs signing, below)
```
**Release signing (one time):** create an upload key and keep it safe — losing it means you cannot update the app.
```
keytool -genkeypair -v -keystore coolingcare-upload.jks -alias upload -keyalg RSA -keysize 2048 -validity 10000
```
Then create `keystore.properties` in the project root (git-ignored):
```
storeFile=coolingcare-upload.jks
storePassword=…
keyAlias=upload
keyPassword=…
```
Upload `composeApp/build/outputs/bundle/release/composeApp-release.aab` to Play Console (enable Play App Signing) and add
its SHA fingerprints to Firebase (step 4 above) — phone log-in fails on release builds without them.

## Build — iOS (needs a Mac with Xcode 16+)
1. Set `TEAM_ID` in `iosApp/Configuration/Config.xcconfig`; enable the Push Notifications capability for the App ID.
2. Open `iosApp/iosApp.xcodeproj`. Xcode resolves the Firebase packages (firebase-ios-sdk 12.x: FirebaseAuth, Firestore,
   Storage, Messaging) and calls Gradle to build the Kotlin framework. If the linker reports Firebase symbol mismatches,
   pin firebase-ios-sdk to the version GitLive 2.7.0 was built with (see the GitLive release notes).
3. Run on a device for push / silent phone verification (the simulator uses the reCAPTCHA fallback).
4. Archive → upload to App Store Connect.
The iOS sources were written on Windows and have not been compiled yet; expect small fixes on the first Mac build.

## Regenerating assets
- `python tools/gen.py` — fonts, WebP images, icon vectors, catalog (offline defaults + picture keys), Android icons.
- `python tools/app_icon.py` — iOS 1024 icon and Play 512 icon.
- Images come from the client board upscaled ×4 with Real-ESRGAN (`tools/images/upscale.py`), cropped by `tools/images/build.py`.

## Fonts & licences
Poppins, Inter, Noto Nastaliq Urdu — SIL Open Font License. Icons — Lucide (ISC). WhatsApp glyph drawn for this app.

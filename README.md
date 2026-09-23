# enzvuck icon

> **customise your icons.**

**enzvuck icon** is an Android application icon editor, icon pack generator, and theme exporter built with Kotlin and Jetpack Compose.

---

## What is enzvuck icon?

Android does not generally permit third-party applications to directly replace another application's launcher icon through public system APIs. Many previous utilities attempted to circumvent this by creating home screen shortcuts disguised as icons, producing badge overlays, delayed launches, and messy home screens.

**enzvuck icon does NOT create home screen shortcuts.**

Instead, **enzvuck icon** is a genuine icon asset and icon pack workshop:
1. Detects launchable applications on your device using standard package APIs.
2. Provides a full graphic editor (Crop, Transform, Shape, Background, Border, Shadow, Adjustments, Filters).
3. Saves clean, pristine custom icons (zero watermarks, no "E" badge overlay).
4. Packages icons into standard launcher-compatible formats:
   - **Portable Theme ZIP** (`theme.json`, `appfilter.xml`, `icons/*.webp`)
   - **Icons ZIP** (`enzvuck-icons/` + `metadata.json`)
   - **Standalone Icon Pack Android Project Package**
   - Individual clean PNG / WEBP exports.
5. Imports and parses portable theme archives, validating metadata and mapping against installed apps.
6. Simulates home screen layouts in a dedicated interactive preview.

---

## Key Features

- **Offline-First & Private:** No accounts, no backend, no cloud sync, no tracking. Your icons remain strictly on your device.
- **Installed App Discovery:** Reads only necessary metadata (label, package name, launcher activity, original icon) without invasive permissions.
- **Real-Time App Search:** Search instantly by app name or package identifier.
- **Comprehensive Icon Editor:**
  - **Crop:** 1:1 default, free crop, zoom (0.5x to 3x), pan X/Y, rotation.
  - **Transform:** Scale, position offsets, rotation, flip horizontal/vertical.
  - **Shape:** Original, Square, Rounded Square, Squircle (superellipse), Circle, Hexagon, Custom Radius slider.
  - **Background:** Transparent, original, white, black, solid colors, linear gradient, radial gradient.
  - **Border:** Toggleable, thickness, custom color, opacity.
  - **Shadow:** Toggleable, opacity, blur, offset X/Y, spread.
  - **Adjustments:** Brightness, contrast, saturation, exposure, opacity, grayscale, color inversion.
  - **Preset Filters:** Original, Mono, Dark, Light, Contrast, Soft, Invert.
- **Critical Clean Icon Rule:** Output icons NEVER include watermarks, badges, or brand logos.
- **Theme ZIP & AppFilter:** Generates industry-standard `appfilter.xml` with real `ComponentInfo{packageName/activity}` component mappings.
- **Theme Importer:** Import portable `theme.json` packages and match them against installed applications.
- **Simulated Home Screen:** Visual mock layout displaying clock, wallpaper, app grid, and dock without creating real shortcuts.

---

## Architecture & Code Structure

The project follows modern Android architecture (MVVM, Clean Architecture, Kotlin Coroutines & Flow, Room):

```
enzvuck-icon/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/enzvuck/icon/
│   │   │   │   ├── data/
│   │   │   │   │   ├── dao/         # Room DAOs (CustomIconDao, IconPackDao)
│   │   │   │   │   ├── database/    # EnzvuckDatabase
│   │   │   │   │   ├── entity/      # Room Entities
│   │   │   │   │   └── repository/  # IconRepository
│   │   │   │   ├── domain/model/    # Domain models (AppInfo, EditorConfig, CustomIcon, IconPack)
│   │   │   │   ├── editor/          # Pure ImageProcessor graphics engine
│   │   │   │   ├── export/          # AppFilterGenerator, ResourceNameNormalizer, ThemePackager
│   │   │   │   ├── icons/           # InstalledAppsManager
│   │   │   │   ├── storage/         # StorageManager (internal filesDir/cacheDir)
│   │   │   │   ├── ui/
│   │   │   │   │   ├── screens/     # Apps, Editor, Detail, MyIcons, Packs, Preview, Settings
│   │   │   │   │   ├── theme/       # Soft Neobrutalism theme & typography
│   │   │   │   │   └── viewmodel/   # MainViewModel & state holders
│   │   │   │   └── MainActivity.kt
│   │   │   └── res/
│   │   └── test/                    # Unit tests & verification suites
│   └── build.gradle.kts
├── gradle/
├── .github/workflows/build.yml
├── settings.gradle.kts
└── README.md
```

---

## How to Build Locally

### Prerequisites
- JDK 17
- Android SDK (API 34+ recommended)

### Build Command
```bash
chmod +x ./gradlew
./gradlew assembleDebug
```

The debug APK will be generated at:
```
app/build/outputs/apk/debug/app-debug.apk
```

---

## How to Build Using GitHub Actions

The repository includes `.github/workflows/build.yml` configured to build automatically on every push, pull request, and manual `workflow_dispatch` trigger without requiring any secrets.

### Downloading the Generated APK Artifact
1. Go to your repository on GitHub.
2. Click the **Actions** tab.
3. Select the latest run of **Build enzvuck icon**.
4. Scroll down to the **Artifacts** section at the bottom of the page.
5. Click **enzvuck-icon-debug-apk** to download the ZIP containing the compiled debug APK.

---

## Launcher Compatibility & Icon Pack Limitations

### Standard Icon Packs
- Custom theme packages and exported `appfilter.xml` files conform to standard third-party launcher conventions (compatible with Nova Launcher, Lawnchair, Smart Launcher, Niagara, Action Launcher, ADW, etc.).
- When using launcher theme managers, import the portable theme ZIP or use the exported Icon Pack project.

### Why No Shortcut Creation?
- Traditional "icon changers" create pinned home screen shortcuts via Android `ShortcutManager`. Shortcuts are not real application icons: they suffer from badge overlays, cannot replace app drawer icons, and break when apps update.
- **enzvuck icon** stays true to authentic icon editing and standard theme package generation.

---

## License & Developer

- **Developer:** enzvuck
- **App Name:** enzvuck icon
- **Tagline:** customise your icons.

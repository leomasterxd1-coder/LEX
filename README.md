# LEX

LEX is an independent Jetpack Compose mobile print and scan app, produced by **Leo Lopes**. The interface follows the layout of the original app, with the source icons ported for visual fidelity and a new red/white theme. No original source code, proprietary SDKs or branding are used.

> LEX is an independent project. It is not affiliated with, endorsed by or sponsored by Lexmark. Lexmark is a trademark of its respective owner.

## Features

- **White background with a vivid red Material 3 theme** (light theme forced).
- **Onboarding**: welcome, feature overview, privacy consent and demo device.
- **Home (Fontes)**: 3-column source grid, selected-device bar and bottom navigation, using the original source icons.
- **Print**: PDF, images and text files sent **directly to the printer IP** via IPP (port 631) with a raw port 9100 fallback. Copies, color, sides, media size and orientation are sent as IPP job attributes. No Android print dialog.
- **Scan**: camera capture, Android photo picker, multi-page scan, simulated scanner page, PDF export to `Downloads/LEX`, and print.
- **Copy**: capture or pick a page, then print it as a copy.
- **Web print**: built-in WebView with URL history and direct page printing via the printer IP.
- **Devices**: NSD/mDNS discovery (`_ipp`, `_ipps`, `_printer`, `_pdl-datastream`, `_scanner`), manual add, demo device, default selection, device details, simulated supplies and help.
- **Queue**: print, scan, copy and web jobs with release, delete and clear actions.
- **More**: original-style GERENCIAR/GERAL sections, settings, import, data collection, open-source notices, what's new and about.
- **Persistence**: devices, jobs, history and settings stored locally with `SharedPreferences` (no account required).
- **Notifications**: optional completion notification for jobs.

## Architecture

- Single `ComponentActivity` + Jetpack Compose.
- Navigation Compose with typed routes and bottom navigation.
- `LexViewModel` (`AndroidViewModel`) drives UI state through `StateFlow`.
- `AppRepository` persists JSON state in `SharedPreferences`.
- Platform integration is isolated in:
  - `PrintController` / `PdfUtils` for PDF generation and printing.
  - `ScanUtils` for camera capture URIs through `FileProvider`.
  - `DiscoveryManager` for NSD/mDNS device discovery.
- Material 3 components, custom red color scheme, no dynamic color so the red identity is always preserved.

## Build locally

Requirements: JDK 17, Android SDK 36, an arm64-v8a compatible environment or CI.

```bash
./gradlew assembleRelease
```

The unsigned/debug-signed output is `app/build/outputs/apk/release/app-release.apk`.

For a signed release, provide:

```bash
export LEX_KEYSTORE_PATH=/absolute/path/to/lex-release.jks
export LEX_KEYSTORE_PASSWORD='...'
export LEX_KEY_ALIAS=lex
export LEX_KEY_PASSWORD='...'
./gradlew assembleRelease
```

Without these variables Gradle falls back to the debug signing key. The release build is filtered to **arm64-v8a** only.

## GitHub Actions release

The workflow `.github/workflows/android.yml` runs on tags matching `v*` and on manual dispatch:

1. Sets up JDK 17, Android SDK 36 and Gradle.
2. Decodes the release keystore from repository secrets.
3. Builds `assembleRelease`.
4. Names the APK `LEX-<tag>-arm64-v8a.apk` and generates `SHA256SUMS.txt`.
5. Uploads the workflow artifact.
6. Creates a GitHub Release with the APK attached when the ref is a tag.

Required repository secrets:

| Secret | Description |
| --- | --- |
| `KEYSTORE_BASE64` | Base64 of the release keystore (`.jks`) |
| `LEX_KEYSTORE_PASSWORD` | Keystore password |
| `LEX_KEY_ALIAS` | Key alias (`lex`) |
| `LEX_KEY_PASSWORD` | Key password |

To publish a release:

```bash
git tag v1.0.0
git push origin v1.0.0
```

## Limitations

- LEX does not include Lexmark's proprietary cloud, secure-release or device SDKs. Discovery uses NSD/mDNS and printing uses standard IPP/raw network protocols. The printer must be reachable on the same network and support PDF/IPP (or raw port 9100). Cloud/secure print flows are represented as local demo flows.
- The simulated scanner and demo printer are local fixtures for exploring the UI.
- The app is intentionally free of owner branding, proprietary assets and trackers.

## Author

Produced by **Leo Lopes**.

## License

The original LEX source in this repository is provided as an independent clean-room implementation. Third-party libraries retain their respective licenses; see the Open source screen in the app.

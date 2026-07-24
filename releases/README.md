# Releases

FoneClaw APKs are distributed through **GitHub Releases**. Each release includes the APK binary, release notes, and version metadata.

## Latest Release

**v0.0.9** — Package prepared July 24, 2026

[Download APK](https://github.com/FoneClaw-AI/foneclaw-android/releases/tag/v0.0.9) | [Release Notes](v0.0.9.md)

## All Versions

| Version | Date | Status |
|---------|------|--------|
| [v0.0.9](v0.0.9.md) | 2026-07-24 | Package Prepared |
| [v0.0.8](v0.0.8.md) | 2026-07-22 | GitHub Pre-release |
| [v0.0.7](v0.0.7.md) | 2026-07-17 | GitHub Pre-release |
| [v0.0.6](v0.0.6.md) | 2026-07-09 | Package Prepared |
| [v0.0.5](v0.0.5.md) | TBD | Release Notes Draft |
| [v0.0.4](v0.0.4.md) | TBD | Release Notes Draft |
| [v0.0.3](v0.0.3.md) | TBD | In Development |
| [v0.0.2](v0.0.2.md) | 2026-06-17 | Released |

## OTA Update System

FoneClaw includes a built-in OTA (Over-the-Air) update checker. When a new version is available, the app displays an update notification with release notes and a download link.

**OTA Endpoint:** `GET https://public-files-foneclaw-ai.oss-us-west-1.aliyuncs.com/FoneClaw/Version/version.json`

**Response format:**
```json
{
  "version": "0.0.9",
  "versionCode": 9,
  "minVersionCode": 1,
  "forceUpdate": false,
  "releaseNotes": "Improved notification summaries, permission recovery, Home Markdown output, device status workflows, SMS handling, and model/request handling.",
  "apkPath": "/FoneClaw/Version/V0.0.9/app-onlineTts-release.apk",
  "apkSize": 79630910,
  "apkMd5": "13975d9b8accb38ed940e8f1ea07fdca"
}
```

## Installation

1. Download the APK from the [Releases page](https://github.com/FoneClaw-AI/foneclaw-android/releases)
2. Enable "Install from unknown sources" in Android Settings if prompted
3. Open the APK file to install
4. Launch FoneClaw and follow the setup wizard
5. Enable the Accessibility Service when prompted (required for device automation)

**Requirements:** Android 9 (API 28) or above

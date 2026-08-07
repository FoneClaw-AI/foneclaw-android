# Releases

FoneClaw APKs are distributed through **GitHub Releases**. Each release includes the APK binary, release notes, and version metadata.

## Latest Release

**v0.1.2-fix** — GitHub Pre-release, August 7, 2026

[Download APK](https://github.com/FoneClaw-AI/foneclaw-android/releases/tag/v0.1.2-fix) | [Release Notes](v0.1.2-fix.md)

## All Versions

| Version | Date | Status |
|---------|------|--------|
| [v0.1.2-fix](v0.1.2-fix.md) | 2026-08-07 | GitHub Pre-release |
| [v0.1.2](v0.1.2.md) | 2026-08-07 | GitHub Pre-release |
| [v0.1.1](v0.1.1.md) | 2026-08-05 | GitHub Pre-release |
| [v0.1.0](v0.1.0.md) | 2026-07-31 | GitHub Pre-release |
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

**OTA Endpoint:** `GET https://public-files.foneclaw.ai/FoneClaw/Version/version.json`

**Response format:**
```json
{
  "version": "0.1.2-fix",
  "versionCode": 13,
  "minVersionCode": 1,
  "forceUpdate": false,
  "releaseNotes": "Improved floating-assistant reliability and added the first safe Plugin/Skill capability-routing foundation.",
  "apkPath": "/FoneClaw/Version/V0.1.2-fix/app-onlineTts-release.apk",
  "apkSize": 80177470,
  "apkMd5": "6a3c1bad9d98b5a267d2515e9e60a614"
}
```

## Installation

1. Download the APK from the [Releases page](https://github.com/FoneClaw-AI/foneclaw-android/releases)
2. Enable "Install from unknown sources" in Android Settings if prompted
3. Open the APK file to install
4. Launch FoneClaw and follow the setup wizard
5. Enable the Accessibility Service when prompted (required for device automation)

**Requirements:** Android 9 (API 28) or above

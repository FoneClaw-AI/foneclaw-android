# Releases

FoneClaw APKs are distributed through **GitHub Releases**. Each release includes the APK binary, release notes, and version metadata.

## Latest Release

**v0.0.2** — Released June 17, 2026

[Download APK](../../releases/tag/v0.0.2) | [Release Notes](v0.0.2.md)

## All Versions

| Version | Date | Status |
|---------|------|--------|
| [v0.0.2](v0.0.2.md) | 2026-06-17 | Released |
| [v0.0.3](v0.0.3.md) | TBD | In Development |

## OTA Update System

FoneClaw includes a built-in OTA (Over-the-Air) update checker. When a new version is available, the app displays an update notification with release notes and a download link.

**OTA Endpoint:** `GET https://public-files-foneclaw-ai.oss-us-west-1.aliyuncs.com/FoneClaw/Version/version.json`

**Response format:**
```json
{
  "version": "0.0.2",
  "versionCode": 2,
  "minVersionCode": 1,
  "forceUpdate": false,
  "releaseNotes": "Voice input; Intelligent suggestion chips; ...",
  "apkPath": "/FoneClaw/v0.0.2/app-release.apk",
  "apkSize": 16027426,
  "apkMd5": "20ee317c3f8d27960027ae6b761866d4"
}
```

## Installation

1. Download the APK from the [Releases page](../../releases)
2. Enable "Install from unknown sources" in Android Settings if prompted
3. Open the APK file to install
4. Launch FoneClaw and follow the setup wizard
5. Enable the Accessibility Service when prompted (required for device automation)

**Requirements:** Android 9 (API 28) or above

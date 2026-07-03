# Published Plugins

This directory is the local static plugin repo output for FoneClaw Android.

Generated files:

- `repo.json`: repository metadata consumed by the host repo parser.
- `index.min.json`: plugin package index.
- `apks/`: published plugin APK artifacts.

The `source` workspace writes here when running:

```powershell
.\gradlew.bat -p docs/07-release/foneclaw-android/plugin/source publishPlugin -Pplugin=device:file-manager
```

Current package:

```text
apks/ai.android.claw.plugin.device.filemanager-v0.0.2-2.apk
```

The file-manager plugin is an internal device plugin. Version `0.0.2` declares
`MANAGE_EXTERNAL_STORAGE` and exposes file CRUD, search, batch rename, delete,
and HTTPS download tools after the user grants Android All files access.

Host app requirement: FoneClaw Android `0.0.6` or later. Earlier app versions
cannot discover or execute plugin APK tools.

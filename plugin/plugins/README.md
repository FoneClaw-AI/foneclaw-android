# Published Plugins

This directory is the local static plugin repo output for FoneClaw Android.

Generated files:

- `repo.json`: repository metadata consumed by the host repo parser.
- `index.min.json`: plugin package index. Schema 2 entries include APK hash,
  expected signer, capabilities, tool summaries, and permission summaries so the
  host can match missing capabilities before downloading a plugin.
- `apks/`: published plugin APK artifacts.

The `source` workspace writes here when running:

```powershell
.\gradlew.bat -p publish/foneclaw-android/plugin/source publishPlugin -Pplugin=device:file-manager
```

Current packages:

```text
name=foneclaw:file-manager
displayName=File Manager Plugin
apks/ai.android.claw.plugin.device.filemanager-v0.0.3-3.apk

name=foneclaw:youtube-downloader
displayName=YouTube Downloader Plugin
apks/ai.android.claw.plugin.media.youtubedownloader-v0.0.1-1.apk
```

The file-manager plugin is an internal device plugin. Version `0.0.3` declares
`MANAGE_EXTERNAL_STORAGE` and exposes file CRUD, search, batch rename, delete,
and HTTPS download tools after the user grants Android All files access.

The YouTube downloader plugin is an arm64-only local media plugin. Version
`0.0.1` reports only actual available resolutions, downloads exact selected
video quality without silent downgrade, and publishes completed video or audio
through Android MediaStore.

## Host Repository URL

The host app reads the official plugin repository from
`BuildConfig.PLUGIN_REPOSITORY_URL`.

Default:

```text
https://raw.githubusercontent.com/FoneClaw-AI/foneclaw-android/refs/heads/main/plugin/plugins/repo.json
```

Build-time overrides:

- Gradle property: `-PpluginRepoUrl=https://example.com/plugins/repo.json`
- Environment variable: `FONECLAW_PLUGIN_REPO_URL=https://example.com/plugins/repo.json`
- `local.properties`: `PLUGIN_REPOSITORY_URL=https://example.com/plugins/repo.json`

`repo.json` can keep `index` as a repo-relative path such as
`index.min.json`; the host resolves it relative to the configured repo URL.

Host app requirement: FoneClaw Android `0.0.6` or later. Earlier app versions
cannot discover or execute plugin APK tools.

# Plugin Source

This is the unified source workspace for FoneClaw Android plugin APKs.

## Structure

```text
source/
  api/                 shared AIDL and Bundle key constants
  src/
    device/
      file-manager/          file operations plugin
    media/
      youtube-downloader/    local YouTube media download plugin
  templates/
    simple-plugin/    copyable starter template, not included by default
```

Publishable plugins live under `src/<category>/<plugin-name>`.

Current publishable plugin:

- `device/file-manager` (`foneclaw:file-manager`): internal file manager
  plugin. Version `0.0.3` requests
  Android All files access (`MANAGE_EXTERNAL_STORAGE`) and provides file CRUD,
  search, batch rename, delete, and HTTPS download tools.
- `media/youtube-downloader` (`foneclaw:youtube-downloader`): arm64-only local
  downloader for public YouTube videos and Shorts. It provides metadata,
  enqueue, status, and cancel tools and publishes completed files through
  Android MediaStore.

## Naming

Plugin canonical names use `foneclaw:<plugin-name>`.

- `pluginId` in `build.gradle.kts` must match the module name, for example
  `foneclaw:file-manager` for `src/device/file-manager`.
- `extensionId` in `foneclaw_extension.json` must use the same value.
- `displayName` remains the user-facing label, for example
  `File Manager Plugin`.
- Tool names keep the `plugin_` prefix, for example
  `plugin_file_manager_list`.

## Signing

Plugin modules are signed with the public build keystore committed in this
workspace:

```text
androidclaw-plugin.jks
alias: androidclaw-plugin
storePassword/keyPassword: androidclaw-plugin123
SHA-256: E3:31:65:67:CC:A6:49:2D:B9:42:00:80:C9:4D:6E:A4:C3:31:88:67:79:70:DA:58:CE:71:7B:BF:3C:6F:3A:84
```

This key is intentionally public so GitHub and contributors can produce
reproducible plugin APK builds. It must not be treated as a production trust
root. FoneClaw host builds should trust this key only in local or test
configuration by adding its SHA-256 fingerprint to
`PLUGIN_TRUSTED_FINGERPRINTS`.

## Build One Plugin

From the FoneClaw project root:

```powershell
$env:ANDROID_HOME='C:\Users\gongpm.IMYFONE\AppData\Local\Android\Sdk'
$env:ANDROID_SDK_ROOT=$env:ANDROID_HOME
.\gradlew.bat -p publish/foneclaw-android/plugin/source publishPlugin -Pplugin=device:file-manager
```

The task builds the selected plugin, copies the APK into
`../plugins/apks`, and updates `../plugins/repo.json` plus
`../plugins/index.min.json`.

## Build All Plugins

```powershell
.\gradlew.bat -p publish/foneclaw-android/plugin/source publishAllPlugins
```

`publishAllPlugins` scans `src/<category>/<plugin-name>` and does not include
anything under `templates/`.

## Create A New Plugin

1. Copy `templates/simple-plugin` to `src/<category>/<new-plugin>`.
2. Update package name, plugin ID (`foneclaw:<new-plugin>`), display name,
   version, and category.
3. Replace `foneclaw_extension.json` tool metadata.
4. Replace `SKILL.md`.
5. Implement tool handling in the service.
6. Publish with `publishPlugin -Pplugin=<category>:<new-plugin>`.

# Plugin Source

This is the unified source workspace for FoneClaw Android plugin APKs.

## Structure

```text
source/
  api/                 shared AIDL and Bundle key constants
  src/
    device/
      file-manager/   first real plugin module
  templates/
    simple-plugin/    copyable starter template, not included by default
```

Publishable plugins live under `src/<category>/<plugin-name>`.

Current publishable plugin:

- `device/file-manager`: internal file manager plugin. Version `0.0.2` requests
  Android All files access (`MANAGE_EXTERNAL_STORAGE`) and provides file CRUD,
  search, batch rename, delete, and HTTPS download tools.

## Build One Plugin

From the FoneClaw project root:

```powershell
$env:ANDROID_HOME='C:\Users\gongpm.IMYFONE\AppData\Local\Android\Sdk'
$env:ANDROID_SDK_ROOT=$env:ANDROID_HOME
.\gradlew.bat -p docs/07-release/foneclaw-android/plugin/source publishPlugin -Pplugin=device:file-manager
```

The task builds the selected plugin, copies the APK into
`../plugins/apks`, and updates `../plugins/repo.json` plus
`../plugins/index.min.json`.

## Build All Plugins

```powershell
.\gradlew.bat -p docs/07-release/foneclaw-android/plugin/source publishAllPlugins
```

`publishAllPlugins` scans `src/<category>/<plugin-name>` and does not include
anything under `templates/`.

## Create A New Plugin

1. Copy `templates/simple-plugin` to `src/<category>/<new-plugin>`.
2. Update package name, plugin ID, display name, version, and category.
3. Replace `foneclaw_extension.json` tool metadata.
4. Replace `SKILL.md`.
5. Implement tool handling in the service.
6. Publish with `publishPlugin -Pplugin=<category>:<new-plugin>`.

# Simple Plugin Template

Copy this directory to `src/<category>/<new-plugin>` when creating a new
FoneClaw plugin.

After copying:

1. Update `build.gradle.kts` with the new plugin ID, package name, display
   name, version name, version code, and category.
2. Update `src/main/AndroidManifest.xml` if the service class name changes.
3. Update `src/main/res/raw/foneclaw_extension.json` with real tool metadata.
4. Update `src/main/assets/SKILL.md` with real workflow guidance.
5. Implement tool branches in `SimplePluginService`.
6. Publish with:

```powershell
.\gradlew.bat -p docs/07-release/foneclaw-android/plugin/source publishPlugin -Pplugin=<category>:<new-plugin>
```

This template is not included by default in Gradle and is not published by
`publishAllPlugins`.


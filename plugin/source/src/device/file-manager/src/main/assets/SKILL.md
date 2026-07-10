---
name: file-manager-plugin
description: Manage user files with the official file manager plugin.
version: 0.0.3
---

# File Manager Plugin

## Scope

This plugin manages files under the Android external storage root after the user
grants all files access to the plugin APK. It can list, search, read text,
create files and folders, write text files, rename, batch rename, delete, and
download HTTPS files.

## Safety Rules

1. Call `plugin_file_manager_status` before file operations when permission
   state is unknown.
2. If all files access is missing, call
   `plugin_file_manager_open_all_files_access_settings` and ask the user to
   enable the permission in Android settings.
3. Use relative paths under the external storage root. Do not pass absolute
   paths.
4. For batch rename, always call `plugin_file_manager_batch_rename_preview`
   first. Only call `plugin_file_manager_batch_rename_apply` with the returned
   `previewToken` after the user approves the preview.
5. For delete, overwrite, download, and batch rename apply, clearly summarize
   the target path and likely impact before requesting approval.
6. The plugin rejects path traversal and refuses to delete the storage root.

## Recommended Workflows

1. To list files, call `plugin_file_manager_list`.
2. To search files, call `plugin_file_manager_search`.
3. To read text, call `plugin_file_manager_read_text` with a safe `maxBytes`.
4. To create or update text files, call `plugin_file_manager_create_file` or
   `plugin_file_manager_write_text`.
5. To rename a single file, call `plugin_file_manager_rename`.
6. To batch rename, preview first, show the mapping, then apply with the token.
7. To delete files or folders, call `plugin_file_manager_delete`.
8. To download a file, call `plugin_file_manager_download` with an HTTPS URL
   and a relative destination path.

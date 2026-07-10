package ai.android.claw.plugin.device.filemanager

import ai.android.claw.extension.IFoneClawExtensionService
import ai.android.claw.plugin.PluginBundleKeys
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.IBinder
import java.io.File
import org.json.JSONObject

class FileManagerPluginService : Service() {

    private val binder = object : IFoneClawExtensionService.Stub() {
        override fun executeTool(request: Bundle?): Bundle {
            val toolName = request?.getString(PluginBundleKeys.TOOL_NAME).orEmpty()
            val argsJson = request?.getString(PluginBundleKeys.ARGS_JSON).orEmpty()
                .ifBlank { "{}" }

            return when (toolName) {
                "plugin_file_manager_status" -> fileManagerStatus()
                "plugin_file_manager_open_all_files_access_settings" -> openAllFilesAccessSettings()
                else -> executeFileTool(toolName, argsJson)
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    private fun executeFileTool(toolName: String, argsJson: String): Bundle {
        if (!hasAllFilesAccess()) {
            return userActionRequired(
                code = "all_files_access_required",
                message = "All files access is required. Call " +
                    "plugin_file_manager_open_all_files_access_settings first.",
            )
        }

        val args = argsJson.toJsonObject()
        val operations = FileManagerOperations(managedRoot())
        val result = when (toolName) {
            "plugin_file_manager_list" -> operations.listDirectory(
                path = args.optString("path", ""),
                limit = args.optInt("limit", DEFAULT_LIMIT),
            )

            "plugin_file_manager_search" -> operations.search(
                directory = args.optString("directory", ""),
                query = args.optString("query", ""),
                limit = args.optInt("limit", DEFAULT_LIMIT),
            )

            "plugin_file_manager_read_text" -> operations.readText(
                path = args.optString("path", ""),
                maxBytes = args.optLong("maxBytes", DEFAULT_MAX_READ_BYTES),
            )

            "plugin_file_manager_create_file" -> operations.writeText(
                path = args.optString("path", ""),
                content = args.optString("content", ""),
                overwrite = false,
                createParents = args.optBoolean("createParents", true),
            )

            "plugin_file_manager_write_text" -> operations.writeText(
                path = args.optString("path", ""),
                content = args.optString("content", ""),
                overwrite = args.optBoolean("overwrite", false),
                createParents = args.optBoolean("createParents", true),
            )

            "plugin_file_manager_create_folder" -> operations.createFolder(
                path = args.optString("path", ""),
            )

            "plugin_file_manager_rename" -> operations.rename(
                path = args.optString("path", ""),
                newName = args.optString("newName", ""),
                overwrite = args.optBoolean("overwrite", false),
            )

            "plugin_file_manager_batch_rename_preview" -> operations.batchRenamePreview(
                directory = args.optString("directory", ""),
                namePattern = args.optString("namePattern", ""),
                startIndex = args.optInt("startIndex", 1),
                padding = args.optInt("padding", 2),
                includeDirectories = args.optBoolean("includeDirectories", false),
                limit = args.optInt("limit", DEFAULT_LIMIT),
            )

            "plugin_file_manager_batch_rename_apply" -> operations.batchRenameApply(
                directory = args.optString("directory", ""),
                namePattern = args.optString("namePattern", ""),
                startIndex = args.optInt("startIndex", 1),
                padding = args.optInt("padding", 2),
                includeDirectories = args.optBoolean("includeDirectories", false),
                limit = args.optInt("limit", DEFAULT_LIMIT),
                previewToken = args.optString("previewToken", ""),
            )

            "plugin_file_manager_delete" -> operations.delete(
                path = args.optString("path", ""),
                recursive = args.optBoolean("recursive", false),
            )

            "plugin_file_manager_download" -> operations.download(
                url = args.optString("url", ""),
                path = args.optString("path", ""),
                overwrite = args.optBoolean("overwrite", false),
                maxBytes = args.optLong("maxBytes", DEFAULT_MAX_DOWNLOAD_BYTES),
            )

            else -> FileOperationResult(
                ok = false,
                text = "Unknown file manager plugin tool: $toolName",
                code = "unknown_tool",
            )
        }

        return result.toBundle()
    }

    private fun fileManagerStatus(): Bundle {
        val root = managedRoot()
        val text = buildString {
            appendLine("All files access: ${if (hasAllFilesAccess()) "granted" else "missing"}")
            appendLine("Managed root: ${root.path}")
            appendLine("Android SDK: ${Build.VERSION.SDK_INT}")
        }.trimEnd()

        return successResponse(text)
    }

    private fun openAllFilesAccessSettings(): Bundle {
        val opened = openFileManagerAllFilesAccessSettings(newTask = true)

        return if (opened) {
            userActionRequired(
                code = "enable_all_files_access",
                message = "Opened File Manager Plugin permissions. Enable all files access.",
            )
        } else {
            errorResponse(
                status = "RETRYABLE_ERROR",
                code = "settings_unavailable",
                message = "Unable to open all files access settings.",
            )
        }
    }

    private fun hasAllFilesAccess(): Boolean {
        return hasFileManagerAllFilesAccess()
    }

    private fun managedRoot(): File {
        return Environment.getExternalStorageDirectory().canonicalFile
    }

    private fun FileOperationResult.toBundle(): Bundle {
        return if (ok) {
            successResponse(text)
        } else {
            errorResponse(
                status = "INVALID_ARGUMENT",
                code = code ?: "file_operation_failed",
                message = text,
            )
        }
    }

    private fun successResponse(text: String): Bundle {
        return Bundle().apply {
            putString(PluginBundleKeys.STATUS, "OK")
            putString(PluginBundleKeys.TEXT, text)
        }
    }

    private fun userActionRequired(code: String, message: String): Bundle {
        return errorResponse(
            status = "USER_ACTION_REQUIRED",
            code = code,
            message = message,
        )
    }

    private fun errorResponse(
        status: String,
        code: String,
        message: String,
    ): Bundle {
        return Bundle().apply {
            putString(PluginBundleKeys.STATUS, status)
            putString(PluginBundleKeys.TEXT, "")
            putString(PluginBundleKeys.ERROR_CODE, code)
            putString(PluginBundleKeys.ERROR_MESSAGE, message)
        }
    }

    private fun String.toJsonObject(): JSONObject {
        return runCatching { JSONObject(ifBlank { "{}" }) }.getOrDefault(JSONObject())
    }

    private companion object {
        const val DEFAULT_LIMIT = 50
        const val DEFAULT_MAX_READ_BYTES = 1_048_576L
        const val DEFAULT_MAX_DOWNLOAD_BYTES = 104_857_600L
    }
}

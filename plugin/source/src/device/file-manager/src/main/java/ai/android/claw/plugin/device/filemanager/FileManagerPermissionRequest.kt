package ai.android.claw.plugin.device.filemanager

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment

data class FileManagerPermissionSettingsTarget(
    val action: String,
    val dataUri: String?,
)

object FileManagerPermissionRequest {
    const val ACTION_MANAGE_APP_ALL_FILES_ACCESS =
        "android.settings.MANAGE_APP_ALL_FILES_ACCESS_PERMISSION"
    const val ACTION_MANAGE_ALL_FILES_ACCESS =
        "android.settings.MANAGE_ALL_FILES_ACCESS_PERMISSION"
    const val ACTION_APPLICATION_DETAILS_SETTINGS =
        "android.settings.APPLICATION_DETAILS_SETTINGS"

    fun settingsTarget(packageName: String, sdkInt: Int): FileManagerPermissionSettingsTarget {
        val dataUri = "package:$packageName"
        return if (sdkInt >= Build.VERSION_CODES.R) {
            FileManagerPermissionSettingsTarget(
                action = ACTION_MANAGE_APP_ALL_FILES_ACCESS,
                dataUri = dataUri,
            )
        } else {
            FileManagerPermissionSettingsTarget(
                action = ACTION_APPLICATION_DETAILS_SETTINGS,
                dataUri = dataUri,
            )
        }
    }

    fun allFilesAccessListTarget(): FileManagerPermissionSettingsTarget {
        return FileManagerPermissionSettingsTarget(
            action = ACTION_MANAGE_ALL_FILES_ACCESS,
            dataUri = null,
        )
    }

    fun settingsTargets(packageName: String, sdkInt: Int): List<FileManagerPermissionSettingsTarget> {
        val appDetailsTarget = FileManagerPermissionSettingsTarget(
            action = ACTION_APPLICATION_DETAILS_SETTINGS,
            dataUri = "package:$packageName",
        )
        if (sdkInt < Build.VERSION_CODES.R) {
            return listOf(appDetailsTarget)
        }

        return listOf(
            settingsTarget(packageName = packageName, sdkInt = sdkInt),
            allFilesAccessListTarget(),
            appDetailsTarget,
        )
    }
}

fun hasFileManagerAllFilesAccess(): Boolean {
    return Build.VERSION.SDK_INT < Build.VERSION_CODES.R || Environment.isExternalStorageManager()
}

fun Context.openFileManagerAllFilesAccessSettings(newTask: Boolean): Boolean {
    return FileManagerPermissionRequest.settingsTargets(
        packageName = packageName,
        sdkInt = Build.VERSION.SDK_INT,
    ).any { target ->
        runCatching { startActivity(target.toIntent(newTask)) }.isSuccess
    }
}

private fun FileManagerPermissionSettingsTarget.toIntent(newTask: Boolean): Intent {
    return Intent(action, dataUri?.let(Uri::parse)).apply {
        if (newTask) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
}

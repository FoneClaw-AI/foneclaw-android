package ai.android.claw.plugin.device.filemanager

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FileManagerPermissionRequestTest {

    @Test
    fun androidRAndAboveTargetsAppAllFilesAccessSettings() {
        val target = FileManagerPermissionRequest.settingsTarget(
            packageName = "ai.android.claw.plugin.device.filemanager",
            sdkInt = 30,
        )

        assertEquals(
            FileManagerPermissionRequest.ACTION_MANAGE_APP_ALL_FILES_ACCESS,
            target.action,
        )
        assertEquals(
            "package:ai.android.claw.plugin.device.filemanager",
            target.dataUri,
        )
    }

    @Test
    fun preAndroidRTargetsApplicationDetailsSettings() {
        val target = FileManagerPermissionRequest.settingsTarget(
            packageName = "ai.android.claw.plugin.device.filemanager",
            sdkInt = 29,
        )

        assertEquals(
            FileManagerPermissionRequest.ACTION_APPLICATION_DETAILS_SETTINGS,
            target.action,
        )
        assertEquals(
            "package:ai.android.claw.plugin.device.filemanager",
            target.dataUri,
        )
    }

    @Test
    fun allFilesAccessListFallbackHasNoPackageUri() {
        val target = FileManagerPermissionRequest.allFilesAccessListTarget()

        assertEquals(
            FileManagerPermissionRequest.ACTION_MANAGE_ALL_FILES_ACCESS,
            target.action,
        )
        assertNull(target.dataUri)
    }

    @Test
    fun androidRAndAboveSettingsTargetsIncludeSystemFallbacks() {
        val targets = FileManagerPermissionRequest.settingsTargets(
            packageName = "ai.android.claw.plugin.device.filemanager",
            sdkInt = 30,
        )

        assertEquals(
            FileManagerPermissionRequest.ACTION_MANAGE_APP_ALL_FILES_ACCESS,
            targets[0].action,
        )
        assertEquals("package:ai.android.claw.plugin.device.filemanager", targets[0].dataUri)
        assertEquals(
            FileManagerPermissionRequest.ACTION_MANAGE_ALL_FILES_ACCESS,
            targets[1].action,
        )
        assertNull(targets[1].dataUri)
        assertEquals(
            FileManagerPermissionRequest.ACTION_APPLICATION_DETAILS_SETTINGS,
            targets[2].action,
        )
        assertEquals("package:ai.android.claw.plugin.device.filemanager", targets[2].dataUri)
    }
}

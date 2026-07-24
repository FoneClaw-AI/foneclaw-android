package ai.android.claw.plugin.device.filemanager

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class FileManagerOperationsTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun batchRenamePreviewReturnsTokenWithoutMutatingFiles() {
        val root = temporaryFolder.newFolder("root")
        root.resolve("IMG_0001.jpg").writeText("one")
        root.resolve("IMG_0002.jpg").writeText("two")
        val operations = FileManagerOperations(root)

        val result = operations.batchRenamePreview(
            directory = "",
            namePattern = "vacation_{index}{ext}",
            startIndex = 1,
            padding = 2,
            includeDirectories = false,
            limit = 20,
        )

        assertTrue(result.ok)
        assertTrue(result.text.contains("previewToken="))
        assertTrue(result.text.contains("IMG_0001.jpg -> vacation_01.jpg"))
        assertTrue(result.text.contains("IMG_0002.jpg -> vacation_02.jpg"))
        assertTrue(root.resolve("IMG_0001.jpg").exists())
        assertFalse(root.resolve("vacation_01.jpg").exists())
    }

    @Test
    fun deleteRejectsPathOutsideRoot() {
        val root = temporaryFolder.newFolder("root")
        val outside = temporaryFolder.newFile("outside.txt")
        outside.writeText("keep")
        val operations = FileManagerOperations(root)

        val result = operations.delete("../outside.txt", recursive = false)

        assertFalse(result.ok)
        assertEquals("path_outside_root", result.code)
        assertTrue(outside.exists())
    }

    @Test
    fun downloadRejectsDirectoryTargetBeforeOpeningConnection() {
        val root = temporaryFolder.newFolder("root")
        val operations = FileManagerOperations(
            root = root,
            openConnection = {
                error("download should reject a directory target before opening a connection")
            },
        )

        val result = operations.download(
            url = "https://example.com/file.txt",
            path = "",
            overwrite = true,
            maxBytes = 1024,
        )

        assertFalse(result.ok)
        assertEquals("target_is_directory", result.code)
        assertTrue(root.exists())
    }
}

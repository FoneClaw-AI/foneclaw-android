package ai.android.claw.plugin.media.youtubedownloader

import android.content.ContentValues
import android.content.ContentUris
import android.content.Context
import android.os.Environment
import android.provider.MediaStore
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class YoutubePublishedOutputValidatorTest {
    @Test
    fun mediaStoreOutputMustStillExistToBeReusable() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val resolver = context.contentResolver
        val uri = requireNotNull(
            resolver.insert(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, "youtube-output-test.txt")
                    put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
                    put(
                        MediaStore.MediaColumns.RELATIVE_PATH,
                        "${Environment.DIRECTORY_DOWNLOADS}/FoneClawTest",
                    )
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
            )
        )
        try {
            resolver.openOutputStream(uri, "w")?.use { output -> output.write(1) }
            resolver.update(
                uri,
                ContentValues().apply { put(MediaStore.MediaColumns.IS_PENDING, 0) },
                null,
                null,
            )
            val validator = YoutubePublishedOutputValidator(context)
            val mediaStoreId = ContentUris.parseId(uri)

            assertEquals(mediaStoreId, validator.parseMediaStoreId(uri.toString()))
            assertTrue(validator.isAvailable(uri.toString(), mediaStoreId))
            assertFalse(validator.isAvailable(uri.toString(), mediaStoreId + 1L))
            resolver.delete(uri, null, null)
            assertFalse(validator.isAvailable(uri.toString(), mediaStoreId))
            assertFalse(validator.isAvailable(null, null))
        } finally {
            resolver.delete(uri, null, null)
        }
    }
}

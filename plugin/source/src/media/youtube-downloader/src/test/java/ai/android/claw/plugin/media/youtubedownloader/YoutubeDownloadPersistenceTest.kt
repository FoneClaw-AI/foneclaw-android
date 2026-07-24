package ai.android.claw.plugin.media.youtubedownloader

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class YoutubeDownloadPersistenceTest {
    @Test
    fun `room entity preserves the complete download job`() {
        val job = YoutubeDownloadJob(
            id = "job-1",
            canonicalUrl = "https://www.youtube.com/watch?v=jNQXAC9IVRw",
            videoId = "jNQXAC9IVRw",
            title = "Me at the zoo",
            source = "share",
            format = YoutubeDownloadFormat.VIDEO_240P,
            formatSelectionVersion = CURRENT_FORMAT_SELECTION_VERSION,
            status = YoutubeDownloadStatus.DOWNLOADING,
            progress = 42,
            downloadedBytes = 123_456L,
            totalBytes = 629_172L,
            etaSeconds = 8L,
            outputUri = null,
            mediaStoreId = 123L,
            errorCode = null,
            createdAtMillis = 1_000L,
            updatedAtMillis = 2_000L,
        )

        assertEquals(job, job.toEntity().toModel())
    }

    @Test
    fun `invalid persisted format or status is rejected`() {
        val entity = YoutubeDownloadJob(
            id = "job-2",
            canonicalUrl = "https://www.youtube.com/watch?v=jNQXAC9IVRw",
            videoId = "jNQXAC9IVRw",
            title = "Me at the zoo",
            source = "home_link",
            format = YoutubeDownloadFormat.AUDIO_M4A,
            formatSelectionVersion = CURRENT_FORMAT_SELECTION_VERSION,
            status = YoutubeDownloadStatus.QUEUED,
            progress = 0,
            downloadedBytes = 0L,
            totalBytes = null,
            etaSeconds = null,
            outputUri = null,
            mediaStoreId = null,
            errorCode = null,
            createdAtMillis = 1_000L,
            updatedAtMillis = 1_000L,
        ).toEntity()

        assertNull(entity.copy(format = "video_999p").toModel())
        assertNull(entity.copy(status = "UNKNOWN_STATUS").toModel())
    }

    @Test
    fun `runtime code does not use shared preferences for download records`() {
        val sourceDirectory = File(
            "src/main/java/ai/android/claw/plugin/media/youtubedownloader"
        )
        val runtimeSource = sourceDirectory.walkTopDown()
            .filter { file -> file.isFile && file.extension == "kt" }
            .joinToString("\n", transform = File::readText)
        val jobStoreSource = File(sourceDirectory, "YoutubeJobStore.kt").readText()

        assertFalse(runtimeSource.contains("SharedPreferences"))
        assertFalse(runtimeSource.contains("getSharedPreferences"))
        assertFalse(runtimeSource.contains("deleteSharedPreferences"))
        assertFalse(jobStoreSource.contains("JSONArray"))
        assertFalse(runtimeSource.contains("MAX_STORED_JOBS"))
        assertFalse(jobStoreSource.contains("trimToLimit"))
        assertTrue(jobStoreSource.contains("runInTransaction"))
        assertTrue(jobStoreSource.contains("YoutubeDownloadDatabase"))
    }
}

package ai.android.claw.plugin.media.youtubedownloader

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import java.io.File

class YoutubeMediaPublisher(context: Context) {
    private val appContext = context.applicationContext

    fun publish(job: YoutubeDownloadJob, sourceFile: File): Uri {
        val collection = if (job.format.isVideo) {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }
        val topDirectory = if (job.format.isVideo) {
            Environment.DIRECTORY_MOVIES
        } else {
            Environment.DIRECTORY_MUSIC
        }
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, job.outputFileName(sourceFile.extension))
            put(MediaStore.MediaColumns.MIME_TYPE, job.format.mimeType)
            put(MediaStore.MediaColumns.RELATIVE_PATH, "$topDirectory/$OUTPUT_DIRECTORY")
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }
        val resolver = appContext.contentResolver
        val uri = resolver.insert(collection, values)
            ?: throw YoutubeDownloadException(YoutubeDownloadErrorCode.PUBLISH_FAILED)
        try {
            resolver.openOutputStream(uri, "w")?.use { output ->
                sourceFile.inputStream().buffered().use { input -> input.copyTo(output) }
            } ?: throw YoutubeDownloadException(YoutubeDownloadErrorCode.PUBLISH_FAILED)
            resolver.update(
                uri,
                ContentValues().apply { put(MediaStore.MediaColumns.IS_PENDING, 0) },
                null,
                null,
            )
            return uri
        } catch (error: Throwable) {
            resolver.delete(uri, null, null)
            if (error is YoutubeDownloadException) throw error
            throw YoutubeDownloadException(YoutubeDownloadErrorCode.PUBLISH_FAILED, error)
        }
    }

    private fun YoutubeDownloadJob.outputFileName(actualExtension: String): String {
        val safeTitle = title
            .replace(Regex("[\\/:*?\"<>|\\p{Cntrl}]"), "_")
            .trim()
            .take(MAX_FILE_TITLE_LENGTH)
            .ifBlank { videoId }
        val extension = actualExtension.lowercase().takeIf(String::isNotBlank)
            ?: format.extension
        return "$safeTitle [$videoId].$extension"
    }

    private companion object {
        const val OUTPUT_DIRECTORY = "FoneClaw"
        const val MAX_FILE_TITLE_LENGTH = 120
    }
}

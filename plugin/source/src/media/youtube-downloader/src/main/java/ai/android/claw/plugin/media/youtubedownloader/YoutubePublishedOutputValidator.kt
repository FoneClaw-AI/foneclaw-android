package ai.android.claw.plugin.media.youtubedownloader

import android.content.ContentUris
import android.content.Context
import android.net.Uri

internal class YoutubePublishedOutputValidator(context: Context) {
    private val contentResolver = context.applicationContext.contentResolver

    fun parseMediaStoreId(outputUri: String?): Long? {
        val uri = parseContentUri(outputUri) ?: return null
        return runCatching { ContentUris.parseId(uri) }
            .getOrNull()
            ?.takeIf { id -> id >= 0L }
    }

    fun isAvailable(outputUri: String?, mediaStoreId: Long?): Boolean {
        val uri = parseContentUri(outputUri) ?: return false
        val storedId = mediaStoreId ?: return false
        if (parseMediaStoreId(outputUri) != storedId) return false
        return runCatching {
            contentResolver.openFileDescriptor(uri, "r")?.use { descriptor ->
                descriptor.statSize != 0L
            } == true
        }.getOrDefault(false)
    }

    private fun parseContentUri(outputUri: String?): Uri? {
        return outputUri
            ?.takeIf(String::isNotBlank)
            ?.let(Uri::parse)
            ?.takeIf { value -> value.scheme == "content" }
    }
}

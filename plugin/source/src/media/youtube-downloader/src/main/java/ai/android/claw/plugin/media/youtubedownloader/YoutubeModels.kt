package ai.android.claw.plugin.media.youtubedownloader

import org.json.JSONArray
import org.json.JSONObject

enum class YoutubeDownloadFormat(
    val wireValue: String,
    val extension: String,
    val mimeType: String,
    val isVideo: Boolean,
    val targetHeight: Int? = null,
) {
    VIDEO_144P("video_144p", "mp4", "video/mp4", true, 144),
    VIDEO_240P("video_240p", "mp4", "video/mp4", true, 240),
    VIDEO_360P("video_360p", "mp4", "video/mp4", true, 360),
    VIDEO_480P("video_480p", "mp4", "video/mp4", true, 480),
    VIDEO_720P("video_720p", "mp4", "video/mp4", true, 720),
    VIDEO_1080P("video_1080p", "mp4", "video/mp4", true, 1080),
    AUDIO_M4A("audio_m4a", "m4a", "audio/mp4", false),
    AUDIO_MP3("audio_mp3", "mp3", "audio/mpeg", false),
    ;

    companion object {
        fun fromWireValue(value: String): YoutubeDownloadFormat? {
            return entries.firstOrNull { format -> format.wireValue == value }
        }
    }
}

data class YoutubeFormatChoice(
    val format: YoutubeDownloadFormat,
    val estimatedBytes: Long?,
    val width: Int? = null,
    val height: Int? = null,
    val audioBitrateKbps: Int? = null,
) {
    fun toJson(): JSONObject {
        return JSONObject()
            .put("format", format.wireValue)
            .put("mediaType", if (format.isVideo) "video" else "audio")
            .put("container", format.extension)
            .put("width", width ?: JSONObject.NULL)
            .put("height", height ?: JSONObject.NULL)
            .put("audioBitrateKbps", audioBitrateKbps ?: JSONObject.NULL)
            .put("estimatedBytes", estimatedBytes ?: JSONObject.NULL)
    }
}

enum class YoutubeDownloadStatus {
    QUEUED,
    PREPARING,
    DOWNLOADING,
    MERGING,
    PUBLISHING,
    SUCCEEDED,
    FAILED,
    CANCELED,
    INTERRUPTED,
    ;

    val isTerminal: Boolean
        get() = this == SUCCEEDED || this == FAILED || this == CANCELED

    val isActive: Boolean
        get() = this == PREPARING || this == DOWNLOADING ||
            this == MERGING || this == PUBLISHING
}

data class YoutubeVideoInfo(
    val canonicalUrl: String,
    val videoId: String,
    val title: String,
    val uploader: String?,
    val thumbnailUrl: String?,
    val durationSeconds: Long,
    val availableFormats: List<YoutubeFormatChoice>,
) {
    fun choiceFor(format: YoutubeDownloadFormat): YoutubeFormatChoice? {
        return availableFormats.firstOrNull { choice -> choice.format == format }
    }

    fun toJson(): JSONObject {
        val formats = JSONArray()
        availableFormats.forEach { choice -> formats.put(choice.toJson()) }
        return JSONObject()
            .put("url", canonicalUrl)
            .put("videoId", videoId)
            .put("title", title)
            .put("uploader", uploader ?: JSONObject.NULL)
            .put("thumbnailUrl", thumbnailUrl ?: JSONObject.NULL)
            .put("durationSeconds", durationSeconds)
            .put("formats", formats)
    }
}

data class YoutubeDownloadJob(
    val id: String,
    val canonicalUrl: String,
    val videoId: String,
    val title: String,
    val source: String,
    val format: YoutubeDownloadFormat,
    val formatSelectionVersion: Int,
    val status: YoutubeDownloadStatus,
    val progress: Int,
    val downloadedBytes: Long,
    val totalBytes: Long?,
    val etaSeconds: Long?,
    val outputUri: String?,
    val mediaStoreId: Long?,
    val errorCode: String?,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
) {
    fun toJson(): JSONObject {
        return JSONObject()
            .put("jobId", id)
            .put("url", canonicalUrl)
            .put("videoId", videoId)
            .put("title", title)
            .put("source", source)
            .put("format", format.wireValue)
            .put("formatSelectionVersion", formatSelectionVersion)
            .put("status", status.name)
            .put("progress", progress)
            .put("downloadedBytes", downloadedBytes)
            .put("totalBytes", totalBytes ?: JSONObject.NULL)
            .put("etaSeconds", etaSeconds ?: JSONObject.NULL)
            .put("outputUri", outputUri ?: JSONObject.NULL)
            .put("mediaStoreId", mediaStoreId ?: JSONObject.NULL)
            .put("errorCode", errorCode ?: JSONObject.NULL)
            .put("createdAtMillis", createdAtMillis)
            .put("updatedAtMillis", updatedAtMillis)
    }

    fun toStoredJson(): JSONObject = toJson()

    companion object {
        fun fromStoredJson(json: JSONObject): YoutubeDownloadJob? {
            val format = YoutubeDownloadFormat.fromWireValue(json.optString("format"))
                ?: return null
            val formatSelectionVersion = json.optInt("formatSelectionVersion", 1)
            if (!isSupportedStoredFormatSelection(format, formatSelectionVersion)) {
                return null
            }
            val status = runCatching {
                YoutubeDownloadStatus.valueOf(json.optString("status"))
            }.getOrNull() ?: return null
            val id = json.optString("jobId").takeIf(String::isNotBlank) ?: return null
            val canonicalUrl = json.optString("url").takeIf(String::isNotBlank) ?: return null
            return YoutubeDownloadJob(
                id = id,
                canonicalUrl = canonicalUrl,
                videoId = json.optString("videoId"),
                title = json.optString("title"),
                source = json.optString("source", "agent_tool"),
                format = format,
                formatSelectionVersion = formatSelectionVersion,
                status = status,
                progress = json.optInt("progress").coerceIn(0, 100),
                downloadedBytes = json.optLong("downloadedBytes").coerceAtLeast(0L),
                totalBytes = json.optNullableLong("totalBytes"),
                etaSeconds = json.optNullableLong("etaSeconds"),
                outputUri = json.optNullableString("outputUri"),
                mediaStoreId = json.optNullableLong("mediaStoreId"),
                errorCode = json.optNullableString("errorCode"),
                createdAtMillis = json.optLong("createdAtMillis"),
                updatedAtMillis = json.optLong("updatedAtMillis"),
            )
        }
    }
}

internal const val CURRENT_FORMAT_SELECTION_VERSION = 2

internal fun isSupportedStoredFormatSelection(
    format: YoutubeDownloadFormat,
    selectionVersion: Int,
): Boolean {
    return !format.isVideo || selectionVersion >= CURRENT_FORMAT_SELECTION_VERSION
}

class YoutubeDownloadException(
    val code: String,
    cause: Throwable? = null,
) : Exception(code, cause)

object YoutubeDownloadErrorCode {
    const val INVALID_URL = "YTDL_INVALID_URL"
    const val MULTIPLE_URLS = "YTDL_MULTIPLE_URLS"
    const val UNSUPPORTED_CONTENT = "YTDL_UNSUPPORTED_CONTENT"
    const val VIDEO_UNAVAILABLE = "YTDL_VIDEO_UNAVAILABLE"
    const val AUTH_REQUIRED = "YTDL_AUTH_REQUIRED"
    const val REGION_RESTRICTED = "YTDL_REGION_RESTRICTED"
    const val EXTRACTOR_OUTDATED = "YTDL_EXTRACTOR_OUTDATED"
    const val NETWORK_ERROR = "YTDL_NETWORK_ERROR"
    const val STORAGE_LOW = "YTDL_STORAGE_LOW"
    const val MERGE_FAILED = "YTDL_MERGE_FAILED"
    const val PUBLISH_FAILED = "YTDL_PUBLISH_FAILED"
    const val CANCELED = "YTDL_CANCELED"
    const val INTERRUPTED = "YTDL_INTERRUPTED"
    const val INITIALIZATION_FAILED = "YTDL_INITIALIZATION_FAILED"
    const val QUEUE_FULL = "YTDL_QUEUE_FULL"
    const val JOB_NOT_FOUND = "YTDL_JOB_NOT_FOUND"
    const val OUTPUT_MISSING = "YTDL_OUTPUT_MISSING"
    const val INVALID_CURSOR = "YTDL_INVALID_CURSOR"
    const val STATUS_RESPONSE_TOO_LARGE = "YTDL_STATUS_RESPONSE_TOO_LARGE"
    const val UNKNOWN = "YTDL_UNKNOWN"
}

object YoutubeDownloadLimits {
    const val MAX_DURATION_SECONDS = 2 * 60 * 60L
    const val MAX_FILE_SIZE_BYTES = 1024L * 1024L * 1024L
    const val MIN_FREE_SPACE_BYTES = 256L * 1024L * 1024L
    const val MAX_PENDING_JOBS = 3
}

private fun JSONObject.optNullableLong(key: String): Long? {
    return if (isNull(key) || !has(key)) null else optLong(key)
}

private fun JSONObject.optNullableString(key: String): String? {
    return if (isNull(key) || !has(key)) null else optString(key).takeIf(String::isNotBlank)
}

package ai.android.claw.plugin.media.youtubedownloader

import ai.android.claw.extension.IFoneClawExtensionService
import ai.android.claw.plugin.PluginBundleKeys
import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import org.json.JSONObject

class YoutubeDownloaderPluginService : Service() {
    private val store by lazy { YoutubeJobStore(this) }
    private val engine by lazy { YoutubeDownloadEngine(this) }
    private val outputValidator by lazy { YoutubePublishedOutputValidator(this) }

    private val binder = object : IFoneClawExtensionService.Stub() {
        override fun executeTool(request: Bundle?): Bundle {
            val toolName = request?.getString(PluginBundleKeys.TOOL_NAME).orEmpty()
            val argsJson = request?.getString(PluginBundleKeys.ARGS_JSON).orEmpty()
                .ifBlank { "{}" }
            return runCatching {
                YoutubeDownloadProcessRecovery.ensureRecovered(store)
                val args = JSONObject(argsJson)
                when (toolName) {
                    TOOL_VIDEO_INFO -> videoInfo(args)
                    TOOL_DOWNLOAD -> download(args)
                    TOOL_STATUS -> status(args)
                    TOOL_CANCEL -> cancel(args)
                    else -> errorResponse(
                        status = "INVALID_ARGUMENT",
                        code = "unknown_tool",
                        message = "Unknown YouTube downloader plugin tool: $toolName",
                    )
                }
            }.getOrElse(::exceptionResponse)
        }
    }

    override fun onBind(intent: Intent?): IBinder = binder

    private fun videoInfo(args: JSONObject): Bundle {
        val parsed = YoutubeUrlParser.parseOne(args.requiredString("url"))
        val info = engine.getInfo(parsed.canonicalUrl)
        if (info.durationSeconds !in 1..YoutubeDownloadLimits.MAX_DURATION_SECONDS) {
            throw YoutubeDownloadException(YoutubeDownloadErrorCode.UNSUPPORTED_CONTENT)
        }
        return successResponse(info.toJson().toString())
    }

    private fun download(args: JSONObject): Bundle {
        val parsed = YoutubeUrlParser.parseOne(args.requiredString("url"))
        val format = YoutubeDownloadFormat.fromWireValue(args.requiredString("format"))
            ?: throw YoutubeDownloadException(YoutubeDownloadErrorCode.UNSUPPORTED_CONTENT)
        val source = args.optString("source", "agent_tool")
            .takeIf { value -> value in ALLOWED_SOURCES }
            ?: "agent_tool"
        var result = store.enqueue(parsed, format, source)
        var job = result.job
        if (job.status == YoutubeDownloadStatus.SUCCEEDED &&
            !outputValidator.isAvailable(job.outputUri, job.mediaStoreId)
        ) {
            store.markSucceededOutputMissing(job.id)
            result = store.enqueue(parsed, format, source)
            job = result.job
        }
        var resumed = false
        if (job.status == YoutubeDownloadStatus.INTERRUPTED) {
            store.update(job.id) { current ->
                current.copy(
                    status = YoutubeDownloadStatus.QUEUED,
                    errorCode = null,
                    updatedAtMillis = System.currentTimeMillis(),
                )
            }
            job = store.find(job.id) ?: job
            resumed = true
        }
        if (job.status == YoutubeDownloadStatus.QUEUED) {
            try {
                YoutubeDownloadService.start(this, job.id)
            } catch (error: RuntimeException) {
                store.update(job.id) { current ->
                    if (current.status != YoutubeDownloadStatus.QUEUED) {
                        current
                    } else {
                        current.copy(
                            status = YoutubeDownloadStatus.INTERRUPTED,
                            errorCode = YoutubeDownloadErrorCode.INTERRUPTED,
                            etaSeconds = null,
                            updatedAtMillis = System.currentTimeMillis(),
                        )
                    }
                }
                throw YoutubeDownloadException(
                    YoutubeDownloadErrorCode.INITIALIZATION_FAILED,
                    error,
                )
            }
        }
        val alreadyInProgress = result is YoutubeJobStore.EnqueueResult.Existing &&
            (job.status == YoutubeDownloadStatus.QUEUED || job.status.isActive)
        val response = job.toJson()
            .put("created", result is YoutubeJobStore.EnqueueResult.Created)
            .put("resumed", resumed)
            .put("alreadyInProgress", alreadyInProgress)
        return successResponse(response.toString())
    }

    private fun status(args: JSONObject): Bundle {
        val jobId = args.optString("jobId").trim()
        if (jobId.isNotBlank()) {
            val job = store.find(jobId)
                ?: throw YoutubeDownloadException(YoutubeDownloadErrorCode.JOB_NOT_FOUND)
            return successResponse(job.toJson().toString())
        }

        val limit = args.optInt("limit", DEFAULT_STATUS_LIMIT)
            .coerceIn(1, MAX_STATUS_LIMIT)
        val cursor = args.optString("cursor").trim()
            .takeIf(String::isNotBlank)
            ?.let { value ->
                YoutubeJobPageCodec.decodeCursor(value)
                    ?: throw YoutubeDownloadException(YoutubeDownloadErrorCode.INVALID_CURSOR)
            }
        val candidates = store.recentPage(limit + 1, cursor)
        return successResponse(YoutubeJobPageCodec.encodePage(candidates, limit))
    }

    private fun cancel(args: JSONObject): Bundle {
        val jobId = args.requiredString("jobId")
        val job = store.find(jobId)
            ?: throw YoutubeDownloadException(YoutubeDownloadErrorCode.JOB_NOT_FOUND)
        if (!job.status.isTerminal) {
            store.update(jobId) { current ->
                current.copy(
                    status = YoutubeDownloadStatus.CANCELED,
                    errorCode = YoutubeDownloadErrorCode.CANCELED,
                    etaSeconds = null,
                    updatedAtMillis = System.currentTimeMillis(),
                )
            }
            YoutubeDownloadService.cancel(this, jobId)
        }
        return successResponse(
            (store.find(jobId) ?: job).toJson().toString()
        )
    }

    private fun exceptionResponse(error: Throwable): Bundle {
        val code = (error as? YoutubeDownloadException)?.code
            ?: if (error is IllegalArgumentException) {
                YoutubeDownloadErrorCode.INVALID_URL
            } else {
                YoutubeDownloadErrorCode.UNKNOWN
            }
        val status = when (code) {
            YoutubeDownloadErrorCode.INVALID_URL,
            YoutubeDownloadErrorCode.MULTIPLE_URLS,
            YoutubeDownloadErrorCode.UNSUPPORTED_CONTENT,
            YoutubeDownloadErrorCode.JOB_NOT_FOUND,
            YoutubeDownloadErrorCode.INVALID_CURSOR,
            YoutubeDownloadErrorCode.VIDEO_UNAVAILABLE,
            YoutubeDownloadErrorCode.AUTH_REQUIRED,
            YoutubeDownloadErrorCode.REGION_RESTRICTED,
            -> "INVALID_ARGUMENT"
            YoutubeDownloadErrorCode.STORAGE_LOW -> "USER_ACTION_REQUIRED"
            YoutubeDownloadErrorCode.NETWORK_ERROR,
            YoutubeDownloadErrorCode.EXTRACTOR_OUTDATED,
            YoutubeDownloadErrorCode.INITIALIZATION_FAILED,
            YoutubeDownloadErrorCode.QUEUE_FULL,
            YoutubeDownloadErrorCode.MERGE_FAILED,
            YoutubeDownloadErrorCode.PUBLISH_FAILED,
            -> "RETRYABLE_ERROR"
            else -> "INTERNAL_ERROR"
        }
        return errorResponse(status, code, errorMessage(code))
    }

    private fun errorMessage(code: String): String {
        return when (code) {
            YoutubeDownloadErrorCode.INVALID_URL -> "Use one supported public YouTube URL."
            YoutubeDownloadErrorCode.MULTIPLE_URLS -> "Provide exactly one YouTube URL."
            YoutubeDownloadErrorCode.UNSUPPORTED_CONTENT ->
                "This content or requested format is not supported."
            YoutubeDownloadErrorCode.VIDEO_UNAVAILABLE -> "The video is unavailable."
            YoutubeDownloadErrorCode.AUTH_REQUIRED ->
                "Login, member, private, or age-restricted content is not supported."
            YoutubeDownloadErrorCode.REGION_RESTRICTED ->
                "The video is unavailable in the current region."
            YoutubeDownloadErrorCode.EXTRACTOR_OUTDATED ->
                "The bundled extractor needs a plugin update."
            YoutubeDownloadErrorCode.NETWORK_ERROR -> "The network request failed."
            YoutubeDownloadErrorCode.STORAGE_LOW -> "There is not enough free storage."
            YoutubeDownloadErrorCode.QUEUE_FULL -> "The download queue is full."
            YoutubeDownloadErrorCode.JOB_NOT_FOUND -> "The download job was not found."
            YoutubeDownloadErrorCode.INVALID_CURSOR -> "The download history cursor is invalid."
            YoutubeDownloadErrorCode.OUTPUT_MISSING ->
                "The previously downloaded media file is no longer available."
            YoutubeDownloadErrorCode.STATUS_RESPONSE_TOO_LARGE ->
                "The download history response is too large."
            YoutubeDownloadErrorCode.MERGE_FAILED -> "Media processing failed."
            YoutubeDownloadErrorCode.PUBLISH_FAILED ->
                "The downloaded file could not be saved to the media library."
            YoutubeDownloadErrorCode.CANCELED -> "The download was canceled."
            else -> "The YouTube downloader plugin failed with code $code."
        }
    }

    private fun successResponse(text: String): Bundle {
        return Bundle().apply {
            putString(PluginBundleKeys.STATUS, "OK")
            putString(PluginBundleKeys.TEXT, text)
        }
    }

    private fun errorResponse(status: String, code: String, message: String): Bundle {
        return Bundle().apply {
            putString(PluginBundleKeys.STATUS, status)
            putString(PluginBundleKeys.TEXT, "")
            putString(PluginBundleKeys.ERROR_CODE, code)
            putString(PluginBundleKeys.ERROR_MESSAGE, message)
        }
    }

    private fun JSONObject.requiredString(key: String): String {
        return optString(key).trim().takeIf(String::isNotBlank)
            ?: throw IllegalArgumentException("Missing argument: $key")
    }

    private companion object {
        const val TOOL_VIDEO_INFO = "plugin_youtube_video_info"
        const val TOOL_DOWNLOAD = "plugin_youtube_download"
        const val TOOL_STATUS = "plugin_youtube_download_status"
        const val TOOL_CANCEL = "plugin_youtube_download_cancel"
        const val DEFAULT_STATUS_LIMIT = 5
        const val MAX_STATUS_LIMIT = 20
        val ALLOWED_SOURCES = setOf("share", "home_link", "agent_tool")
    }
}

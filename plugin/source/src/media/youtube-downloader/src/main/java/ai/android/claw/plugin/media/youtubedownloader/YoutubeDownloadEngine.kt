package ai.android.claw.plugin.media.youtubedownloader

import android.content.Context
import com.yausername.ffmpeg.FFmpeg
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLException
import com.yausername.youtubedl_android.YoutubeDLRequest
import com.yausername.youtubedl_android.mapper.VideoFormat
import com.yausername.youtubedl_android.mapper.VideoInfo
import java.io.File
import java.util.Locale

class YoutubeDownloadEngine(context: Context) {
    private val appContext = context.applicationContext

    fun getInfo(canonicalUrl: String): YoutubeVideoInfo {
        ensureInitialized()
        val info = executeSafely { YoutubeDL.getInstance().getInfo(baseRequest(canonicalUrl)) }
        return info.toModel(canonicalUrl)
    }

    fun download(
        job: YoutubeDownloadJob,
        outputDirectory: File,
        onProgress: (status: YoutubeDownloadStatus, percent: Int, etaSeconds: Long?) -> Unit,
    ): File {
        ensureInitialized()
        check(outputDirectory.mkdirs() || outputDirectory.isDirectory)

        val request = baseRequest(job.canonicalUrl)
            .addOption("--newline")
            .addOption("--no-mtime")
            .addOption("--max-filesize", YoutubeDownloadLimits.MAX_FILE_SIZE_BYTES)
            .addOption("--fragment-retries", RETRY_COUNT)
            .addOption("--output", File(outputDirectory, "%(id)s.%(ext)s").absolutePath)
            .applyFormat(job.format)

        executeSafely {
            YoutubeDL.getInstance().execute(request, job.id) { percent, etaSeconds, line ->
                onProgress(
                    line.toDownloadStatus(),
                    percent.toInt().coerceIn(0, 100),
                    etaSeconds.takeIf { eta -> eta >= 0L },
                )
            }
        }

        return outputDirectory.listFiles()
            .orEmpty()
            .asSequence()
            .filter(File::isFile)
            .filterNot { file -> file.name.endsWith(".part") || file.name.endsWith(".ytdl") }
            .filter { file -> file.length() > 0L }
            .sortedWith(
                compareByDescending<File> { file ->
                    file.extension.equals(job.format.extension, ignoreCase = true)
                }.thenByDescending(File::length)
            )
            .firstOrNull()
            ?: throw YoutubeDownloadException(YoutubeDownloadErrorCode.UNKNOWN)
    }

    fun cancel(jobId: String): Boolean {
        return YoutubeDL.getInstance().destroyProcessById(jobId)
    }

    private fun ensureInitialized() {
        if (initialized) return
        synchronized(initializationLock) {
            if (initialized) return
            try {
                YoutubeDL.getInstance().init(appContext)
                FFmpeg.getInstance().init(appContext)
                initialized = true
            } catch (error: Throwable) {
                throw YoutubeDownloadException(
                    YoutubeDownloadErrorCode.INITIALIZATION_FAILED,
                    error,
                )
            }
        }
    }

    private fun baseRequest(url: String): YoutubeDLRequest {
        return YoutubeDLRequest(url)
            .addOption("--ignore-config")
            .addOption("--no-playlist")
            .addOption("--no-warnings")
            .addOption("--extractor-args", "youtube:player_client=android_vr")
            .addOption("--socket-timeout", SOCKET_TIMEOUT_SECONDS)
            .addOption("--retries", RETRY_COUNT)
    }

    private fun YoutubeDLRequest.applyFormat(
        format: YoutubeDownloadFormat,
    ): YoutubeDLRequest {
        if (format.isVideo) {
            return applyVideoFormat(requireNotNull(format.targetHeight))
        }
        return when (format) {
            YoutubeDownloadFormat.AUDIO_M4A -> addOption("--extract-audio")
                .addOption("--format", "bestaudio[ext=m4a]/bestaudio/best")
                .addOption("--audio-format", "m4a")
            YoutubeDownloadFormat.AUDIO_MP3 -> addOption("--extract-audio")
                .addOption("--format", "bestaudio/best")
                .addOption("--audio-format", "mp3")
                .addOption("--audio-quality", "192K")
            else -> error("Unsupported download format: ${format.wireValue}")
        }
    }

    private fun YoutubeDLRequest.applyVideoFormat(height: Int): YoutubeDLRequest {
        return addOption("--format", exactVideoSelector(height))
            .addOption("--merge-output-format", "mp4")
    }

    private inline fun <T> executeSafely(block: () -> T): T {
        return try {
            block()
        } catch (error: YoutubeDL.CanceledException) {
            throw YoutubeDownloadException(YoutubeDownloadErrorCode.CANCELED, error)
        } catch (error: InterruptedException) {
            Thread.currentThread().interrupt()
            throw YoutubeDownloadException(YoutubeDownloadErrorCode.INTERRUPTED, error)
        } catch (error: YoutubeDLException) {
            throw YoutubeDownloadException(error.classifyYoutubeDlError(), error)
        }
    }

    private fun VideoInfo.toModel(canonicalUrl: String): YoutubeVideoInfo {
        val safeId = id?.takeIf(String::isNotBlank)
            ?: throw YoutubeDownloadException(YoutubeDownloadErrorCode.VIDEO_UNAVAILABLE)
        val safeTitle = title?.trim()?.takeIf(String::isNotBlank) ?: safeId
        val availableFormats = formats.orEmpty()
        return YoutubeVideoInfo(
            canonicalUrl = canonicalUrl,
            videoId = safeId,
            title = safeTitle.take(MAX_TITLE_LENGTH),
            uploader = uploader?.trim()?.takeIf(String::isNotBlank)
                ?.take(MAX_UPLOADER_LENGTH),
            thumbnailUrl = thumbnail?.takeIf(String::isNotBlank),
            durationSeconds = duration.toLong().coerceAtLeast(0L),
            availableFormats = YoutubeFormatResolver.resolve(
                candidates = availableFormats.map { format -> format.toCandidate() },
                durationSeconds = duration.toLong().coerceAtLeast(0L),
            ),
        )
    }

    private fun VideoFormat.toCandidate(): YoutubeFormatCandidate {
        return YoutubeFormatCandidate(
            extension = ext.orEmpty(),
            videoCodec = vcodec.orEmpty(),
            audioCodec = acodec.orEmpty(),
            width = width,
            height = height,
            fps = fps,
            totalBitrateKbps = tbr,
            audioBitrateKbps = abr,
            preference = preference,
            knownSize = maxOf(fileSize, fileSizeApproximate),
        )
    }

    private fun String.toDownloadStatus(): YoutubeDownloadStatus {
        val normalized = lowercase(Locale.US)
        return if (
            "[merger]" in normalized ||
            "merging formats" in normalized ||
            "[extractaudio]" in normalized
        ) {
            YoutubeDownloadStatus.MERGING
        } else {
            YoutubeDownloadStatus.DOWNLOADING
        }
    }

    private fun YoutubeDLException.classifyYoutubeDlError(): String {
        val text = message.orEmpty().lowercase(Locale.US)
        return when {
            "private video" in text || "sign in" in text || "login" in text ||
                "age-restricted" in text -> YoutubeDownloadErrorCode.AUTH_REQUIRED
            "not available in your country" in text || "geo" in text -> {
                YoutubeDownloadErrorCode.REGION_RESTRICTED
            }
            "video unavailable" in text || "removed" in text ||
                "does not exist" in text -> YoutubeDownloadErrorCode.VIDEO_UNAVAILABLE
            "no video formats" in text || "signature" in text ||
                "extractor" in text -> YoutubeDownloadErrorCode.EXTRACTOR_OUTDATED
            "requested format is not available" in text -> {
                YoutubeDownloadErrorCode.UNSUPPORTED_CONTENT
            }
            "timed out" in text || "network" in text || "http error" in text ||
                "unable to download" in text -> YoutubeDownloadErrorCode.NETWORK_ERROR
            "ffmpeg" in text || "merge" in text || "nonetype" in text -> {
                YoutubeDownloadErrorCode.MERGE_FAILED
            }
            else -> YoutubeDownloadErrorCode.UNKNOWN
        }
    }

    private companion object {
        val initializationLock = Any()

        @Volatile
        var initialized = false

        const val SOCKET_TIMEOUT_SECONDS = 15
        const val RETRY_COUNT = 3
        const val MAX_TITLE_LENGTH = 500
        const val MAX_UPLOADER_LENGTH = 200
    }
}

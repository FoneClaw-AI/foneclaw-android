package ai.android.claw.plugin.media.youtubedownloader

internal data class YoutubeFormatCandidate(
    val extension: String,
    val videoCodec: String,
    val audioCodec: String,
    val width: Int,
    val height: Int,
    val fps: Int,
    val totalBitrateKbps: Int,
    val audioBitrateKbps: Int,
    val preference: Int,
    val knownSize: Long,
) {
    val hasVideo: Boolean
        get() = videoCodec.isNotBlank() && !videoCodec.equals("none", ignoreCase = true)

    val hasAudio: Boolean
        get() = audioCodec.isNotBlank() && !audioCodec.equals("none", ignoreCase = true)

    val isAudioOnly: Boolean
        get() = !hasVideo && hasAudio

    fun estimatedSize(durationSeconds: Long): Long? {
        if (knownSize > 0L) return knownSize
        return totalBitrateKbps.takeIf { bitrate -> bitrate > 0 }
            ?.let { bitrate -> bitrate.toLong() * 1_000L * durationSeconds / 8L }
            ?.takeIf { size -> size > 0L }
    }
}

internal object YoutubeFormatResolver {
    private const val MP3_BITRATE_KBPS = 192

    fun resolve(
        candidates: List<YoutubeFormatCandidate>,
        durationSeconds: Long,
    ): List<YoutubeFormatChoice> = buildList {
        YoutubeDownloadFormat.entries
            .filter(YoutubeDownloadFormat::isVideo)
            .mapNotNull { format -> resolveVideo(candidates, durationSeconds, format) }
            .forEach(::add)

        val audioOnlySources = candidates.filter(YoutubeFormatCandidate::isAudioOnly)
        val audioSources = audioOnlySources.ifEmpty {
            candidates.filter(YoutubeFormatCandidate::hasAudio)
        }
        val m4aSource = audioSources
            .filter { candidate -> candidate.extension.equals("m4a", ignoreCase = true) }
            .bestCandidate()
            ?: audioSources.bestCandidate()
        if (m4aSource != null) {
            add(
                YoutubeFormatChoice(
                    format = YoutubeDownloadFormat.AUDIO_M4A,
                    estimatedBytes = m4aSource.estimatedSize(durationSeconds),
                    audioBitrateKbps = m4aSource.audioBitrateKbps.takeIf { it > 0 },
                )
            )
            add(
                YoutubeFormatChoice(
                    format = YoutubeDownloadFormat.AUDIO_MP3,
                    estimatedBytes = durationSeconds.takeIf { it > 0L }
                        ?.let { duration ->
                            MP3_BITRATE_KBPS.toLong() * 1_000L * duration / 8L
                        },
                    audioBitrateKbps = MP3_BITRATE_KBPS,
                )
            )
        }
    }

    private fun resolveVideo(
        candidates: List<YoutubeFormatCandidate>,
        durationSeconds: Long,
        format: YoutubeDownloadFormat,
    ): YoutubeFormatChoice? {
        val targetHeight = format.targetHeight ?: return null
        val exactMp4 = candidates.filter { candidate ->
            candidate.extension.equals("mp4", ignoreCase = true) &&
                candidate.hasVideo &&
                candidate.height == targetHeight
        }
        val combined = exactMp4
            .filter(YoutubeFormatCandidate::hasAudio)
            .bestCandidate()
        if (combined != null) {
            return YoutubeFormatChoice(
                format = format,
                estimatedBytes = combined.estimatedSize(durationSeconds),
                width = combined.width.takeIf { it > 0 },
                height = targetHeight,
            )
        }

        val video = exactMp4
            .filterNot(YoutubeFormatCandidate::hasAudio)
            .bestCandidate()
            ?: return null
        val audio = candidates
            .filter { candidate ->
                candidate.isAudioOnly &&
                    candidate.extension.equals("m4a", ignoreCase = true)
            }
            .bestCandidate()
            ?: return null
        val videoSize = video.estimatedSize(durationSeconds)
        val audioSize = audio.estimatedSize(durationSeconds)
        val estimatedBytes = if (videoSize != null && audioSize != null) {
            videoSize + audioSize
        } else {
            null
        }
        return YoutubeFormatChoice(
            format = format,
            estimatedBytes = estimatedBytes,
            width = video.width.takeIf { it > 0 },
            height = targetHeight,
        )
    }

    private fun List<YoutubeFormatCandidate>.bestCandidate(): YoutubeFormatCandidate? {
        return maxWithOrNull(
            compareBy<YoutubeFormatCandidate> { candidate -> candidate.preference }
                .thenBy { candidate -> candidate.totalBitrateKbps }
                .thenBy { candidate -> candidate.audioBitrateKbps }
                .thenBy { candidate -> candidate.fps }
                .thenBy { candidate -> candidate.knownSize }
        )
    }
}

internal fun exactVideoSelector(height: Int): String {
    return "best[height=$height][ext=mp4]/" +
        "bestvideo[height=$height][ext=mp4]+bestaudio[ext=m4a]"
}

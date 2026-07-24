package ai.android.claw.plugin.media.youtubedownloader

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class YoutubeFormatResolverTest {
    @Test
    fun `combined video is not charged for audio twice and unavailable qualities are omitted`() {
        val choices = YoutubeFormatResolver.resolve(
            candidates = listOf(
                candidate(
                    extension = "mp4",
                    videoCodec = "avc1",
                    audioCodec = "mp4a",
                    width = 320,
                    height = 240,
                    knownSize = 629_172L,
                ),
                candidate(
                    extension = "m4a",
                    audioCodec = "mp4a",
                    knownSize = 309_000L,
                    audioBitrateKbps = 130,
                ),
            ),
            durationSeconds = 19L,
        )

        val video = choices.single { choice -> choice.format.isVideo }
        assertEquals(YoutubeDownloadFormat.VIDEO_240P, video.format)
        assertEquals(320, video.width)
        assertEquals(240, video.height)
        assertEquals(629_172L, video.estimatedBytes)
        assertFalse(choices.any { choice -> choice.format == YoutubeDownloadFormat.VIDEO_1080P })

        val m4a = choices.single { choice ->
            choice.format == YoutubeDownloadFormat.AUDIO_M4A
        }
        val mp3 = choices.single { choice ->
            choice.format == YoutubeDownloadFormat.AUDIO_MP3
        }
        assertEquals(309_000L, m4a.estimatedBytes)
        assertEquals(456_000L, mp3.estimatedBytes)
        assertEquals(192, mp3.audioBitrateKbps)
    }

    @Test
    fun `separate video and audio sizes are added for the exact resolution`() {
        val choices = YoutubeFormatResolver.resolve(
            candidates = listOf(
                candidate(
                    extension = "mp4",
                    videoCodec = "avc1",
                    width = 1920,
                    height = 1080,
                    knownSize = 2_000_000L,
                ),
                candidate(
                    extension = "m4a",
                    audioCodec = "mp4a",
                    knownSize = 300_000L,
                ),
            ),
            durationSeconds = 60L,
        )

        val video = choices.single { choice ->
            choice.format == YoutubeDownloadFormat.VIDEO_1080P
        }
        assertEquals(2_300_000L, video.estimatedBytes)
        assertEquals(1920, video.width)
        assertEquals(1080, video.height)
        assertNull(
            choices.firstOrNull { choice ->
                choice.format == YoutubeDownloadFormat.VIDEO_720P
            }
        )
    }

    @Test
    fun `video selector requires the requested height`() {
        val selector = exactVideoSelector(1080)

        assertTrue(selector.contains("height=1080"))
        assertFalse(selector.contains("height<=1080"))
        assertEquals(
            "best[height=1080][ext=mp4]/" +
                "bestvideo[height=1080][ext=mp4]+bestaudio[ext=m4a]",
            selector,
        )
    }

    @Test
    fun `legacy video jobs with inexact quality semantics are ignored`() {
        assertFalse(
            isSupportedStoredFormatSelection(
                format = YoutubeDownloadFormat.VIDEO_1080P,
                selectionVersion = 1,
            )
        )
        assertTrue(
            isSupportedStoredFormatSelection(
                format = YoutubeDownloadFormat.VIDEO_1080P,
                selectionVersion = CURRENT_FORMAT_SELECTION_VERSION,
            )
        )
    }

    private fun candidate(
        extension: String,
        videoCodec: String = "none",
        audioCodec: String = "none",
        width: Int = 0,
        height: Int = 0,
        knownSize: Long = 0L,
        audioBitrateKbps: Int = 0,
    ): YoutubeFormatCandidate {
        return YoutubeFormatCandidate(
            extension = extension,
            videoCodec = videoCodec,
            audioCodec = audioCodec,
            width = width,
            height = height,
            fps = 30,
            totalBitrateKbps = 0,
            audioBitrateKbps = audioBitrateKbps,
            preference = 0,
            knownSize = knownSize,
        )
    }
}

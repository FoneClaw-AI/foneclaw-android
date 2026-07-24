package ai.android.claw.plugin.media.youtubedownloader

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class YoutubeUrlParserTest {
    @Test
    fun parsesSupportedVideoUrls() {
        val expected = "https://www.youtube.com/watch?v=dQw4w9WgXcQ"
        val urls = listOf(
            "https://www.youtube.com/watch?v=dQw4w9WgXcQ&utm_source=test",
            "https://youtube.com/watch?v=dQw4w9WgXcQ",
            "https://m.youtube.com/watch?v=dQw4w9WgXcQ",
            "https://youtu.be/dQw4w9WgXcQ?t=1",
            "https://www.youtube.com/shorts/dQw4w9WgXcQ",
        )

        urls.forEach { url ->
            assertEquals(expected, YoutubeUrlParser.parseOne(url).canonicalUrl)
        }
    }

    @Test
    fun extractsOneUrlFromSharedText() {
        val result = YoutubeUrlParser.parseOne(
            "Watch this: https://youtu.be/dQw4w9WgXcQ?si=test.",
        )

        assertEquals("dQw4w9WgXcQ", result.videoId)
    }

    @Test(expected = YoutubeDownloadException::class)
    fun rejectsMultipleSupportedUrls() {
        YoutubeUrlParser.parseOne(
            "https://youtu.be/dQw4w9WgXcQ https://youtu.be/aqz-KE-bpKQ",
        )
    }

    @Test
    fun rejectsUntrustedHostsAndUnsafeUrls() {
        assertNull(YoutubeUrlParser.parseCandidate("http://youtu.be/dQw4w9WgXcQ"))
        assertNull(YoutubeUrlParser.parseCandidate("https://youtube.com.example/watch?v=dQw4w9WgXcQ"))
        assertNull(YoutubeUrlParser.parseCandidate("https://user@youtu.be/dQw4w9WgXcQ"))
        assertNull(YoutubeUrlParser.parseCandidate("https://youtu.be:444/dQw4w9WgXcQ"))
    }
}

package ai.android.claw.plugin.media.youtubedownloader

import java.net.URI
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

data class ParsedYoutubeUrl(
    val canonicalUrl: String,
    val videoId: String,
)

object YoutubeUrlParser {
    private val urlRegex = Regex("""https://[^\s<>\"']+""", RegexOption.IGNORE_CASE)
    private val videoIdRegex = Regex("^[A-Za-z0-9_-]{6,64}$")
    private val youtubeHosts = setOf("youtube.com", "www.youtube.com", "m.youtube.com")

    fun parseOne(text: String): ParsedYoutubeUrl {
        parseCandidate(text.trim().trimUrlPunctuation())?.let { return it }
        val matches = urlRegex.findAll(text)
            .mapNotNull { match -> parseCandidate(match.value.trimUrlPunctuation()) }
            .distinctBy(ParsedYoutubeUrl::canonicalUrl)
            .toList()
        return when (matches.size) {
            1 -> matches.single()
            0 -> throw YoutubeDownloadException(YoutubeDownloadErrorCode.INVALID_URL)
            else -> throw YoutubeDownloadException(YoutubeDownloadErrorCode.MULTIPLE_URLS)
        }
    }

    fun parseCandidate(candidate: String): ParsedYoutubeUrl? {
        val uri = runCatching { URI(candidate) }.getOrNull() ?: return null
        if (!uri.scheme.equals("https", ignoreCase = true)) return null
        if (uri.userInfo != null) return null
        if (uri.port != -1 && uri.port != HTTPS_PORT) return null

        val host = uri.host?.lowercase() ?: return null
        val videoId = when {
            host == SHORT_HOST -> uri.path?.trim('/')?.substringBefore('/')
            host in youtubeHosts -> videoIdFromYoutubeUri(uri)
            else -> null
        }?.takeIf(videoIdRegex::matches) ?: return null

        return ParsedYoutubeUrl(
            canonicalUrl = "https://www.youtube.com/watch?v=$videoId",
            videoId = videoId,
        )
    }

    private fun videoIdFromYoutubeUri(uri: URI): String? {
        val segments = uri.path.orEmpty()
            .trim('/')
            .split('/')
            .filter(String::isNotBlank)
        return when (segments.firstOrNull()?.lowercase()) {
            "watch" -> uri.rawQuery.queryParameter("v")
            "shorts" -> segments.getOrNull(1)
            else -> null
        }
    }

    private fun String?.queryParameter(name: String): String? {
        if (isNullOrBlank()) return null
        return split('&').firstNotNullOfOrNull { pair ->
            val encodedKey = pair.substringBefore('=', missingDelimiterValue = pair)
            val key = runCatching { encodedKey.urlDecode() }.getOrNull()
            if (key != name) return@firstNotNullOfOrNull null
            runCatching {
                pair.substringAfter('=', missingDelimiterValue = "").urlDecode()
            }.getOrNull()
        }
    }

    private fun String.urlDecode(): String {
        return URLDecoder.decode(this, StandardCharsets.UTF_8.name())
    }

    private fun String.trimUrlPunctuation(): String {
        return trim().trimEnd('.', ',', ';', '!', '?', ')', ']', '}')
    }

    private const val SHORT_HOST = "youtu.be"
    private const val HTTPS_PORT = 443
}

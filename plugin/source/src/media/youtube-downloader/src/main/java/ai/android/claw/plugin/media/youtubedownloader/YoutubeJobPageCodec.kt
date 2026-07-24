package ai.android.claw.plugin.media.youtubedownloader

import java.nio.charset.StandardCharsets
import java.util.Base64
import org.json.JSONArray
import org.json.JSONObject

internal data class YoutubeJobCursor(
    val updatedAtMillis: Long,
    val jobId: String,
)

internal object YoutubeJobPageCodec {
    // Binder writes String payloads as UTF-16; this leaves ample room below its 1 MiB limit.
    const val MAX_STATUS_RESPONSE_CHARS = 256 * 1024

    private const val MAX_CURSOR_LENGTH = 512

    fun decodeCursor(value: String): YoutubeJobCursor? {
        if (value.length !in 1..MAX_CURSOR_LENGTH) return null
        return runCatching {
            val decoded = Base64.getUrlDecoder().decode(value)
            val json = JSONObject(String(decoded, StandardCharsets.UTF_8))
            val updatedAtMillis = json.optLong("updatedAtMillis", -1L)
            val jobId = json.optString("jobId").trim()
            require(updatedAtMillis >= 0L)
            require(jobId.isNotBlank() && jobId.length <= 128)
            YoutubeJobCursor(updatedAtMillis, jobId)
        }.getOrNull()
    }

    fun encodePage(candidates: List<YoutubeDownloadJob>, limit: Int): String {
        require(limit > 0)
        val jobs = JSONArray()
        var lastIncluded: YoutubeDownloadJob? = null

        for ((index, job) in candidates.take(limit).withIndex()) {
            jobs.put(job.toJson())
            val hasMore = candidates.size > index + 1
            val candidate = pageJson(
                jobs = jobs,
                hasMore = hasMore,
                nextCursor = job.takeIf { hasMore }?.let(::encodeCursor),
            ).toString()
            if (candidate.length > MAX_STATUS_RESPONSE_CHARS) {
                jobs.remove(jobs.length() - 1)
                break
            }
            lastIncluded = job
        }

        if (candidates.isNotEmpty() && jobs.length() == 0) {
            throw YoutubeDownloadException(YoutubeDownloadErrorCode.STATUS_RESPONSE_TOO_LARGE)
        }
        val hasMore = candidates.size > jobs.length()
        return pageJson(
            jobs = jobs,
            hasMore = hasMore,
            nextCursor = lastIncluded?.takeIf { hasMore }?.let(::encodeCursor),
        ).toString()
    }

    private fun encodeCursor(job: YoutubeDownloadJob): String {
        val json = JSONObject()
            .put("updatedAtMillis", job.updatedAtMillis)
            .put("jobId", job.id)
            .toString()
        return Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(json.toByteArray(StandardCharsets.UTF_8))
    }

    private fun pageJson(
        jobs: JSONArray,
        hasMore: Boolean,
        nextCursor: String?,
    ): JSONObject {
        return JSONObject()
            .put("jobs", jobs)
            .put("hasMore", hasMore)
            .put("nextCursor", nextCursor ?: JSONObject.NULL)
    }
}

package ai.android.claw.plugin.media.youtubedownloader

import android.content.Context
import java.util.UUID
import java.util.concurrent.Callable

class YoutubeJobStore internal constructor(
    private val database: YoutubeDownloadDatabase,
) {
    constructor(context: Context) : this(YoutubeDownloadDatabase.get(context))

    private val jobs = database.jobs()

    fun enqueue(
        url: ParsedYoutubeUrl,
        format: YoutubeDownloadFormat,
        source: String,
        nowMillis: Long = System.currentTimeMillis(),
    ): EnqueueResult {
        return database.runInTransaction(
            Callable {
                jobs.findInProgressByUrl(
                    canonicalUrl = url.canonicalUrl,
                    inProgressStatuses = IN_PROGRESS_STATUSES,
                )?.toModel()?.let { existing ->
                    return@Callable EnqueueResult.Existing(existing)
                }

                jobs.findReusable(
                    canonicalUrl = url.canonicalUrl,
                    format = format.wireValue,
                    formatSelectionVersion = CURRENT_FORMAT_SELECTION_VERSION,
                    excludedStatuses = REUSE_EXCLUDED_STATUSES,
                )?.toModel()?.let { existing ->
                    return@Callable EnqueueResult.Existing(existing)
                }

                if (jobs.countNonTerminal(TERMINAL_STATUSES) >=
                    YoutubeDownloadLimits.MAX_PENDING_JOBS
                ) {
                    throw YoutubeDownloadException(YoutubeDownloadErrorCode.QUEUE_FULL)
                }

                val job = YoutubeDownloadJob(
                    id = UUID.randomUUID().toString(),
                    canonicalUrl = url.canonicalUrl,
                    videoId = url.videoId,
                    title = url.videoId,
                    source = source,
                    format = format,
                    formatSelectionVersion = CURRENT_FORMAT_SELECTION_VERSION,
                    status = YoutubeDownloadStatus.QUEUED,
                    progress = 0,
                    downloadedBytes = 0L,
                    totalBytes = null,
                    etaSeconds = null,
                    outputUri = null,
                    mediaStoreId = null,
                    errorCode = null,
                    createdAtMillis = nowMillis,
                    updatedAtMillis = nowMillis,
                )
                jobs.insert(job.toEntity())
                EnqueueResult.Created(job)
            }
        )
    }

    fun find(jobId: String): YoutubeDownloadJob? {
        return jobs.find(jobId)?.toModel()
    }

    fun recent(limit: Int): List<YoutubeDownloadJob> {
        return jobs.recent(limit).mapNotNull(YoutubeDownloadJobEntity::toModel)
    }

    internal fun recentPage(limit: Int, cursor: YoutubeJobCursor?): List<YoutubeDownloadJob> {
        return jobs.recentPage(
            beforeUpdatedAtMillis = cursor?.updatedAtMillis,
            beforeJobId = cursor?.jobId,
            limit = limit,
        ).mapNotNull(YoutubeDownloadJobEntity::toModel)
    }

    fun update(jobId: String, transform: (YoutubeDownloadJob) -> YoutubeDownloadJob): Boolean {
        return database.runInTransaction(
            Callable {
                val current = jobs.find(jobId)?.toModel() ?: return@Callable false
                jobs.update(transform(current).toEntity()) > 0
            }
        )
    }

    fun markUnfinishedInterrupted(nowMillis: Long = System.currentTimeMillis()) {
        jobs.markUnfinishedInterrupted(
            unfinishedStatuses = UNFINISHED_STATUSES,
            interruptedStatus = YoutubeDownloadStatus.INTERRUPTED.name,
            interruptedError = YoutubeDownloadErrorCode.INTERRUPTED,
            nowMillis = nowMillis,
        )
    }

    fun markSucceededOutputMissing(
        jobId: String,
        nowMillis: Long = System.currentTimeMillis(),
    ): Boolean {
        return update(jobId) { current ->
            if (current.status != YoutubeDownloadStatus.SUCCEEDED) {
                current
            } else {
                current.copy(
                    status = YoutubeDownloadStatus.FAILED,
                    outputUri = null,
                    mediaStoreId = null,
                    errorCode = YoutubeDownloadErrorCode.OUTPUT_MISSING,
                    updatedAtMillis = nowMillis,
                )
            }
        }
    }

    sealed interface EnqueueResult {
        val job: YoutubeDownloadJob

        data class Created(override val job: YoutubeDownloadJob) : EnqueueResult
        data class Existing(override val job: YoutubeDownloadJob) : EnqueueResult
    }

    private companion object {
        val REUSE_EXCLUDED_STATUSES = listOf(
            YoutubeDownloadStatus.FAILED.name,
            YoutubeDownloadStatus.CANCELED.name,
        )
        val TERMINAL_STATUSES = YoutubeDownloadStatus.entries
            .filter(YoutubeDownloadStatus::isTerminal)
            .map(YoutubeDownloadStatus::name)
        val ACTIVE_STATUSES = YoutubeDownloadStatus.entries
            .filter(YoutubeDownloadStatus::isActive)
            .map(YoutubeDownloadStatus::name)
        val IN_PROGRESS_STATUSES = listOf(YoutubeDownloadStatus.QUEUED.name) + ACTIVE_STATUSES
        val UNFINISHED_STATUSES = IN_PROGRESS_STATUSES
    }
}

internal object YoutubeDownloadProcessRecovery {
    private val lock = Any()

    @Volatile
    private var recovered = false

    fun ensureRecovered(store: YoutubeJobStore) {
        if (recovered) return
        synchronized(lock) {
            if (recovered) return
            store.markUnfinishedInterrupted()
            recovered = true
        }
    }
}

package ai.android.claw.plugin.media.youtubedownloader

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class YoutubeJobStoreRoomTest {
    private lateinit var database: YoutubeDownloadDatabase
    private lateinit var store: YoutubeJobStore

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            YoutubeDownloadDatabase::class.java,
        ).build()
        store = YoutubeJobStore(database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun enqueuePersistsAndReusesTheSameActiveJob() {
        val first = store.enqueue(
            url = parsedUrl("video-one"),
            format = YoutubeDownloadFormat.VIDEO_240P,
            source = "share",
            nowMillis = 1_000L,
        )
        val secondStore = YoutubeJobStore(database)
        val second = secondStore.enqueue(
            url = parsedUrl("video-one"),
            format = YoutubeDownloadFormat.VIDEO_240P,
            source = "share",
            nowMillis = 2_000L,
        )

        assertTrue(first is YoutubeJobStore.EnqueueResult.Created)
        assertTrue(second is YoutubeJobStore.EnqueueResult.Existing)
        assertEquals(first.job.id, second.job.id)
        assertEquals(first.job, secondStore.find(first.job.id))
    }

    @Test
    fun sameUrlWithDifferentFormatReturnsTheInProgressJob() {
        val first = store.enqueue(
            url = parsedUrl("same-url"),
            format = YoutubeDownloadFormat.VIDEO_240P,
            source = "share",
        ).job
        store.update(first.id) { current ->
            current.copy(status = YoutubeDownloadStatus.DOWNLOADING)
        }

        val duplicate = store.enqueue(
            url = parsedUrl("same-url"),
            format = YoutubeDownloadFormat.AUDIO_MP3,
            source = "agent_tool",
        )

        assertTrue(duplicate is YoutubeJobStore.EnqueueResult.Existing)
        assertEquals(first.id, duplicate.job.id)
        assertEquals(YoutubeDownloadFormat.VIDEO_240P, duplicate.job.format)
        assertEquals(YoutubeDownloadStatus.DOWNLOADING, duplicate.job.status)
        assertEquals(1, store.recent(10).size)
    }

    @Test
    fun sameUrlCanUseAnotherFormatAfterThePreviousJobFinishes() {
        val first = store.enqueue(
            url = parsedUrl("same-url-finished"),
            format = YoutubeDownloadFormat.VIDEO_240P,
            source = "share",
        ).job
        store.update(first.id) { current ->
            current.copy(status = YoutubeDownloadStatus.SUCCEEDED)
        }

        val next = store.enqueue(
            url = parsedUrl("same-url-finished"),
            format = YoutubeDownloadFormat.AUDIO_MP3,
            source = "agent_tool",
        )

        assertTrue(next is YoutubeJobStore.EnqueueResult.Created)
        assertNotEquals(first.id, next.job.id)
        assertEquals(YoutubeDownloadFormat.AUDIO_MP3, next.job.format)
    }

    @Test
    fun processRecoveryInterruptsQueuedAndActiveJobsOnly() {
        val queued = store.enqueue(
            url = parsedUrl("video-two"),
            format = YoutubeDownloadFormat.AUDIO_M4A,
            source = "home_link",
            nowMillis = 1_000L,
        ).job
        val active = store.enqueue(
            url = parsedUrl("video-three"),
            format = YoutubeDownloadFormat.AUDIO_M4A,
            source = "home_link",
            nowMillis = 1_000L,
        ).job
        assertTrue(
            store.update(active.id) { current ->
                current.copy(
                    status = YoutubeDownloadStatus.DOWNLOADING,
                    progress = 30,
                    updatedAtMillis = 2_000L,
                )
            }
        )
        val succeeded = store.enqueue(
            url = parsedUrl("video-four"),
            format = YoutubeDownloadFormat.AUDIO_M4A,
            source = "home_link",
            nowMillis = 1_000L,
        ).job
        store.update(succeeded.id) { current ->
            current.copy(
                status = YoutubeDownloadStatus.SUCCEEDED,
                updatedAtMillis = 2_000L,
            )
        }

        store.markUnfinishedInterrupted(nowMillis = 3_000L)

        listOf(queued.id, active.id).forEach { jobId ->
            val recovered = store.find(jobId)
            assertNotNull(recovered)
            assertEquals(YoutubeDownloadStatus.INTERRUPTED, recovered?.status)
            assertEquals(YoutubeDownloadErrorCode.INTERRUPTED, recovered?.errorCode)
            assertEquals(3_000L, recovered?.updatedAtMillis)
        }
        assertEquals(YoutubeDownloadStatus.SUCCEEDED, store.find(succeeded.id)?.status)
        assertEquals(2_000L, store.find(succeeded.id)?.updatedAtMillis)
    }

    @Test
    fun queueRejectsMoreThanThreeNonTerminalJobs() {
        repeat(YoutubeDownloadLimits.MAX_PENDING_JOBS) { index ->
            store.enqueue(
                url = parsedUrl("pending-$index"),
                format = YoutubeDownloadFormat.AUDIO_MP3,
                source = "agent_tool",
            )
        }

        val error = runCatching {
            store.enqueue(
                url = parsedUrl("pending-overflow"),
                format = YoutubeDownloadFormat.AUDIO_MP3,
                source = "agent_tool",
            )
        }.exceptionOrNull()

        assertEquals(YoutubeDownloadErrorCode.QUEUE_FULL, (error as? YoutubeDownloadException)?.code)
    }

    @Test
    fun historyIsNotDiscardedAfterFiftyJobs() {
        repeat(HISTORY_JOB_COUNT) { index ->
            val job = store.enqueue(
                url = parsedUrl("history-$index"),
                format = YoutubeDownloadFormat.AUDIO_M4A,
                source = "agent_tool",
                nowMillis = index.toLong(),
            ).job
            store.update(job.id) { current ->
                current.copy(
                    status = YoutubeDownloadStatus.SUCCEEDED,
                    updatedAtMillis = index.toLong(),
                )
            }
        }

        val history = store.recent(HISTORY_JOB_COUNT)
        assertEquals(HISTORY_JOB_COUNT, history.size)
        assertTrue(history.any { job -> job.videoId == "history-0" })
    }

    @Test
    fun missingSucceededOutputCreatesANewJob() {
        val original = store.enqueue(
            url = parsedUrl("missing-output"),
            format = YoutubeDownloadFormat.VIDEO_240P,
            source = "agent_tool",
        ).job
        store.update(original.id) { current ->
            current.copy(
                status = YoutubeDownloadStatus.SUCCEEDED,
                outputUri = "content://media/external/video/media/1",
                mediaStoreId = 1L,
            )
        }

        assertTrue(store.markSucceededOutputMissing(original.id))
        val replacement = store.enqueue(
            url = parsedUrl("missing-output"),
            format = YoutubeDownloadFormat.VIDEO_240P,
            source = "agent_tool",
        ).job

        assertNotEquals(original.id, replacement.id)
        assertEquals(YoutubeDownloadStatus.QUEUED, replacement.status)
        assertEquals(YoutubeDownloadStatus.FAILED, store.find(original.id)?.status)
        assertEquals(YoutubeDownloadErrorCode.OUTPUT_MISSING, store.find(original.id)?.errorCode)
    }

    @Test
    fun cursorPaginationHandlesEqualTimestampsWithoutDuplicates() {
        repeat(PAGINATION_JOB_COUNT) { index ->
            val job = store.enqueue(
                url = parsedUrl("page-$index"),
                format = YoutubeDownloadFormat.AUDIO_M4A,
                source = "agent_tool",
                nowMillis = SHARED_PAGE_TIMESTAMP,
            ).job
            store.update(job.id) { current ->
                current.copy(
                    status = YoutubeDownloadStatus.SUCCEEDED,
                    updatedAtMillis = SHARED_PAGE_TIMESTAMP,
                )
            }
        }

        val collectedIds = linkedSetOf<String>()
        var cursor: YoutubeJobCursor? = null
        do {
            val candidates = store.recentPage(PAGE_SIZE + 1, cursor)
            val payloadText = YoutubeJobPageCodec.encodePage(candidates, PAGE_SIZE)
            assertTrue(payloadText.length <= YoutubeJobPageCodec.MAX_STATUS_RESPONSE_CHARS)
            assertTrue(payloadText.length * 2 < BINDER_TRANSACTION_LIMIT_BYTES)
            val payload = JSONObject(payloadText)
            val jobs = payload.getJSONArray("jobs")
            repeat(jobs.length()) { index ->
                assertTrue(collectedIds.add(jobs.getJSONObject(index).getString("jobId")))
            }
            cursor = payload.optString("nextCursor")
                .takeIf { value -> payload.getBoolean("hasMore") && value.isNotBlank() }
                ?.let(YoutubeJobPageCodec::decodeCursor)
        } while (cursor != null)

        assertEquals(PAGINATION_JOB_COUNT, collectedIds.size)
        assertFalse(YoutubeJobPageCodec.decodeCursor("not-a-cursor") != null)
    }

    @Test
    fun fileDatabasePersistsAfterCloseAndReopen() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val databaseName = "youtube-reopen-${System.nanoTime()}.db"
        context.deleteDatabase(databaseName)
        var fileDatabase: YoutubeDownloadDatabase? = null
        try {
            fileDatabase = YoutubeDownloadDatabase.open(context, databaseName)
            val original = YoutubeJobStore(fileDatabase).enqueue(
                url = parsedUrl("reopen"),
                format = YoutubeDownloadFormat.AUDIO_MP3,
                source = "agent_tool",
            ).job
            fileDatabase.close()
            fileDatabase = YoutubeDownloadDatabase.open(context, databaseName)

            assertEquals(original, YoutubeJobStore(fileDatabase).find(original.id))
        } finally {
            fileDatabase?.close()
            context.deleteDatabase(databaseName)
        }
    }

    private fun parsedUrl(videoId: String): ParsedYoutubeUrl {
        return YoutubeUrlParser.parseOne("https://www.youtube.com/watch?v=$videoId")
    }

    private companion object {
        const val HISTORY_JOB_COUNT = 51
        const val PAGINATION_JOB_COUNT = 25
        const val PAGE_SIZE = 10
        const val SHARED_PAGE_TIMESTAMP = 5_000L
        const val BINDER_TRANSACTION_LIMIT_BYTES = 1024 * 1024
    }
}

package ai.android.claw.plugin.media.youtubedownloader

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.os.IBinder
import android.os.StatFs
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

class YoutubeDownloadService : Service() {
    private val executor = Executors.newSingleThreadExecutor()
    private val controlExecutor = Executors.newSingleThreadExecutor()
    private val scheduledJobIds = ConcurrentHashMap.newKeySet<String>()
    private val workCount = AtomicInteger(0)
    private lateinit var store: YoutubeJobStore
    private lateinit var engine: YoutubeDownloadEngine
    private lateinit var publisher: YoutubeMediaPublisher
    private lateinit var notificationManager: NotificationManager

    override fun onCreate() {
        super.onCreate()
        store = YoutubeJobStore(this)
        engine = YoutubeDownloadEngine(this)
        publisher = YoutubeMediaPublisher(this)
        notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                getString(R.string.youtube_download_channel_name),
                NotificationManager.IMPORTANCE_LOW,
            )
        )
        executor.execute {
            YoutubeDownloadProcessRecovery.ensureRecovered(store)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val jobId = intent?.getStringExtra(EXTRA_JOB_ID).orEmpty()
        startForeground(NOTIFICATION_ID, buildNotification(null))

        when (intent?.action) {
            ACTION_CANCEL -> scheduleCancellation(jobId)
            ACTION_START -> schedule(jobId)
            else -> finishForegroundIfIdle()
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        executor.shutdownNow()
        controlExecutor.shutdownNow()
        super.onDestroy()
    }

    private fun schedule(jobId: String) {
        if (jobId.isBlank() || !scheduledJobIds.add(jobId)) return
        workCount.incrementAndGet()
        executor.execute {
            try {
                process(jobId)
            } finally {
                scheduledJobIds.remove(jobId)
                workCount.decrementAndGet()
                finishForegroundIfIdle()
            }
        }
    }

    private fun scheduleCancellation(jobId: String) {
        if (jobId.isBlank()) {
            finishForegroundIfIdle()
            return
        }
        workCount.incrementAndGet()
        controlExecutor.execute {
            try {
                cancelJob(jobId)
            } finally {
                workCount.decrementAndGet()
                finishForegroundIfIdle()
            }
        }
    }

    private fun process(jobId: String) {
        var job = store.find(jobId) ?: return
        if (job.status == YoutubeDownloadStatus.CANCELED ||
            job.status == YoutubeDownloadStatus.SUCCEEDED
        ) {
            return
        }

        val stagingDirectory = stagingDirectory(jobId)
        try {
            job = updateJob(jobId) { current ->
                current.copy(
                    status = YoutubeDownloadStatus.PREPARING,
                    errorCode = null,
                    updatedAtMillis = System.currentTimeMillis(),
                )
            }
            notifyJob(job)

            val info = engine.getInfo(job.canonicalUrl)
            if (info.durationSeconds !in 1..YoutubeDownloadLimits.MAX_DURATION_SECONDS) {
                throw YoutubeDownloadException(YoutubeDownloadErrorCode.UNSUPPORTED_CONTENT)
            }
            val selectedFormat = info.choiceFor(job.format)
                ?: throw YoutubeDownloadException(YoutubeDownloadErrorCode.UNSUPPORTED_CONTENT)
            val totalBytes = selectedFormat.estimatedBytes
            ensureStorageAvailable(stagingDirectory, totalBytes)
            checkNotCanceled(jobId)

            job = updateJob(jobId) { current ->
                current.copy(
                    title = info.title,
                    videoId = info.videoId,
                    totalBytes = totalBytes,
                    status = YoutubeDownloadStatus.DOWNLOADING,
                    updatedAtMillis = System.currentTimeMillis(),
                )
            }
            notifyJob(job)

            var lastProgress = -1
            var lastStatus = YoutubeDownloadStatus.DOWNLOADING
            var lastPersistMillis = 0L
            val downloadedFile = engine.download(job, stagingDirectory) {
                    status,
                    percent,
                    etaSeconds,
                ->
                checkNotCanceled(jobId)
                val now = System.currentTimeMillis()
                if (percent == lastProgress && status == lastStatus &&
                    now - lastPersistMillis < PROGRESS_PERSIST_INTERVAL_MILLIS
                ) {
                    return@download
                }
                lastProgress = percent
                lastStatus = status
                lastPersistMillis = now
                job = updateJob(jobId) { current ->
                    current.copy(
                        status = status,
                        progress = percent,
                        downloadedBytes = current.totalBytes
                            ?.let { total -> total * percent / 100L }
                            ?: 0L,
                        etaSeconds = etaSeconds,
                        updatedAtMillis = now,
                    )
                }
                notifyJob(job)
            }
            checkNotCanceled(jobId)

            job = updateJob(jobId) { current ->
                current.copy(
                    status = YoutubeDownloadStatus.PUBLISHING,
                    progress = 100,
                    etaSeconds = null,
                    updatedAtMillis = System.currentTimeMillis(),
                )
            }
            notifyJob(job)
            val outputUri = publisher.publish(job, downloadedFile)
            val mediaStoreId = runCatching { ContentUris.parseId(outputUri) }
                .getOrNull()
                ?: throw YoutubeDownloadException(YoutubeDownloadErrorCode.PUBLISH_FAILED)

            job = updateJob(jobId) { current ->
                current.copy(
                    status = YoutubeDownloadStatus.SUCCEEDED,
                    progress = 100,
                    downloadedBytes = downloadedFile.length(),
                    outputUri = outputUri.toString(),
                    mediaStoreId = mediaStoreId,
                    errorCode = null,
                    updatedAtMillis = System.currentTimeMillis(),
                )
            }
            stagingDirectory.deleteRecursively()
            notifyJob(job)
        } catch (error: Throwable) {
            val current = store.find(jobId) ?: return
            if (current.status == YoutubeDownloadStatus.CANCELED) {
                stagingDirectory.deleteRecursively()
                notifyJob(current)
                return
            }
            val code = (error as? YoutubeDownloadException)?.code
                ?: YoutubeDownloadErrorCode.UNKNOWN
            val failedStatus = if (code == YoutubeDownloadErrorCode.INTERRUPTED) {
                YoutubeDownloadStatus.INTERRUPTED
            } else {
                YoutubeDownloadStatus.FAILED
            }
            job = updateJob(jobId) { latest ->
                latest.copy(
                    status = failedStatus,
                    errorCode = code,
                    etaSeconds = null,
                    updatedAtMillis = System.currentTimeMillis(),
                )
            }
            if (failedStatus == YoutubeDownloadStatus.FAILED) {
                stagingDirectory.deleteRecursively()
            }
            notifyJob(job)
        }
    }

    private fun cancelJob(jobId: String) {
        val updated = store.update(jobId) { job ->
            if (job.status.isTerminal) {
                job
            } else {
                job.copy(
                    status = YoutubeDownloadStatus.CANCELED,
                    errorCode = YoutubeDownloadErrorCode.CANCELED,
                    etaSeconds = null,
                    updatedAtMillis = System.currentTimeMillis(),
                )
            }
        }
        runCatching { engine.cancel(jobId) }
        if (updated) {
            stagingDirectory(jobId).deleteRecursively()
            store.find(jobId)?.let(::notifyJob)
        }
    }

    private fun checkNotCanceled(jobId: String) {
        if (store.find(jobId)?.status == YoutubeDownloadStatus.CANCELED) {
            throw YoutubeDownloadException(YoutubeDownloadErrorCode.CANCELED)
        }
    }

    private fun ensureStorageAvailable(directory: File, expectedBytes: Long?) {
        directory.parentFile?.mkdirs()
        val availableBytes = StatFs(directory.parentFile?.absolutePath ?: filesDir.absolutePath)
            .availableBytes
        val requiredBytes = expectedBytes?.let { size ->
            if (size > YoutubeDownloadLimits.MAX_FILE_SIZE_BYTES) {
                throw YoutubeDownloadException(YoutubeDownloadErrorCode.STORAGE_LOW)
            }
            (size * SPACE_MULTIPLIER).toLong() + YoutubeDownloadLimits.MIN_FREE_SPACE_BYTES
        } ?: YoutubeDownloadLimits.MIN_FREE_SPACE_BYTES
        if (availableBytes < requiredBytes) {
            throw YoutubeDownloadException(YoutubeDownloadErrorCode.STORAGE_LOW)
        }
    }

    private fun updateJob(
        jobId: String,
        transform: (YoutubeDownloadJob) -> YoutubeDownloadJob,
    ): YoutubeDownloadJob {
        if (!store.update(jobId, transform)) {
            throw YoutubeDownloadException(YoutubeDownloadErrorCode.JOB_NOT_FOUND)
        }
        return store.find(jobId)
            ?: throw YoutubeDownloadException(YoutubeDownloadErrorCode.JOB_NOT_FOUND)
    }

    private fun notifyJob(job: YoutubeDownloadJob) {
        notificationManager.notify(NOTIFICATION_ID, buildNotification(job))
    }

    private fun buildNotification(job: YoutubeDownloadJob?): Notification {
        val builder = Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle(job?.title ?: getString(R.string.youtube_download_notification_title))
            .setContentText(job?.status?.notificationText() ?: getString(
                R.string.youtube_download_status_preparing
            ))
            .setOnlyAlertOnce(true)
            .setOngoing(job != null && !job.status.isTerminal)

        if (job != null && !job.status.isTerminal) {
            builder.setProgress(100, job.progress, job.progress <= 0)
            builder.addAction(
                Notification.Action.Builder(
                    null,
                    getString(R.string.youtube_download_action_cancel),
                    cancelPendingIntent(job.id),
                ).build()
            )
        } else {
            builder.setProgress(0, 0, false)
        }

        job?.outputUri?.let { uri ->
            builder.setContentIntent(openPendingIntent(job, Uri.parse(uri)))
                .setAutoCancel(true)
        }
        return builder.build()
    }

    private fun YoutubeDownloadStatus.notificationText(): String {
        val resource = when (this) {
            YoutubeDownloadStatus.QUEUED -> R.string.youtube_download_status_queued
            YoutubeDownloadStatus.PREPARING -> R.string.youtube_download_status_preparing
            YoutubeDownloadStatus.DOWNLOADING -> R.string.youtube_download_status_downloading
            YoutubeDownloadStatus.MERGING -> R.string.youtube_download_status_merging
            YoutubeDownloadStatus.PUBLISHING -> R.string.youtube_download_status_publishing
            YoutubeDownloadStatus.SUCCEEDED -> R.string.youtube_download_status_succeeded
            YoutubeDownloadStatus.FAILED -> R.string.youtube_download_status_failed
            YoutubeDownloadStatus.CANCELED -> R.string.youtube_download_status_canceled
            YoutubeDownloadStatus.INTERRUPTED -> R.string.youtube_download_status_interrupted
        }
        return getString(resource)
    }

    private fun cancelPendingIntent(jobId: String): PendingIntent {
        return PendingIntent.getService(
            this,
            jobId.hashCode(),
            Intent(this, YoutubeDownloadService::class.java)
                .setAction(ACTION_CANCEL)
                .putExtra(EXTRA_JOB_ID, jobId),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun openPendingIntent(job: YoutubeDownloadJob, uri: Uri): PendingIntent {
        val intent = Intent(Intent.ACTION_VIEW)
            .setDataAndType(uri, job.format.mimeType)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        return PendingIntent.getActivity(
            this,
            job.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun stagingDirectory(jobId: String): File {
        val root = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: filesDir
        return File(root, "youtube-staging/$jobId")
    }

    private fun finishForegroundIfIdle() {
        if (workCount.get() > 0) return
        stopForeground(STOP_FOREGROUND_DETACH)
        stopSelf()
    }

    companion object {
        private const val ACTION_START =
            "ai.android.claw.plugin.media.youtubedownloader.action.START"
        private const val ACTION_CANCEL =
            "ai.android.claw.plugin.media.youtubedownloader.action.CANCEL"
        private const val EXTRA_JOB_ID = "jobId"
        private const val CHANNEL_ID = "youtube_downloads"
        private const val NOTIFICATION_ID = 17131
        private const val PROGRESS_PERSIST_INTERVAL_MILLIS = 1_000L
        private const val SPACE_MULTIPLIER = 1.2

        fun start(context: Context, jobId: String) {
            context.startForegroundService(
                Intent(context, YoutubeDownloadService::class.java)
                    .setAction(ACTION_START)
                    .putExtra(EXTRA_JOB_ID, jobId)
            )
        }

        fun cancel(context: Context, jobId: String) {
            context.startForegroundService(
                Intent(context, YoutubeDownloadService::class.java)
                    .setAction(ACTION_CANCEL)
                    .putExtra(EXTRA_JOB_ID, jobId)
            )
        }
    }
}

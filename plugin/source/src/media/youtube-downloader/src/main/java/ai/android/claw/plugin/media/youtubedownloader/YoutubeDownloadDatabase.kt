package ai.android.claw.plugin.media.youtubedownloader

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update

@Entity(
    tableName = "youtube_download_jobs",
    indices = [
        Index(value = ["canonical_url", "format", "format_selection_version"]),
        Index(value = ["status"]),
        Index(value = ["updated_at_millis"]),
        Index(value = ["updated_at_millis", "job_id"]),
    ],
)
data class YoutubeDownloadJobEntity(
    @PrimaryKey
    @ColumnInfo(name = "job_id")
    val id: String,
    @ColumnInfo(name = "canonical_url")
    val canonicalUrl: String,
    @ColumnInfo(name = "video_id")
    val videoId: String,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "source")
    val source: String,
    @ColumnInfo(name = "format")
    val format: String,
    @ColumnInfo(name = "format_selection_version")
    val formatSelectionVersion: Int,
    @ColumnInfo(name = "status")
    val status: String,
    @ColumnInfo(name = "progress")
    val progress: Int,
    @ColumnInfo(name = "downloaded_bytes")
    val downloadedBytes: Long,
    @ColumnInfo(name = "total_bytes")
    val totalBytes: Long?,
    @ColumnInfo(name = "eta_seconds")
    val etaSeconds: Long?,
    @ColumnInfo(name = "output_uri")
    val outputUri: String?,
    @ColumnInfo(name = "media_store_id")
    val mediaStoreId: Long?,
    @ColumnInfo(name = "error_code")
    val errorCode: String?,
    @ColumnInfo(name = "created_at_millis")
    val createdAtMillis: Long,
    @ColumnInfo(name = "updated_at_millis")
    val updatedAtMillis: Long,
)

@Dao
interface YoutubeDownloadJobDao {
    @Query("SELECT * FROM youtube_download_jobs WHERE job_id = :jobId LIMIT 1")
    fun find(jobId: String): YoutubeDownloadJobEntity?

    @Query(
        """
        SELECT * FROM youtube_download_jobs
        WHERE canonical_url = :canonicalUrl
          AND status IN (:inProgressStatuses)
        ORDER BY updated_at_millis DESC
        LIMIT 1
        """
    )
    fun findInProgressByUrl(
        canonicalUrl: String,
        inProgressStatuses: List<String>,
    ): YoutubeDownloadJobEntity?

    @Query(
        """
        SELECT * FROM youtube_download_jobs
        WHERE canonical_url = :canonicalUrl
          AND format = :format
          AND format_selection_version = :formatSelectionVersion
          AND status NOT IN (:excludedStatuses)
        ORDER BY updated_at_millis DESC
        LIMIT 1
        """
    )
    fun findReusable(
        canonicalUrl: String,
        format: String,
        formatSelectionVersion: Int,
        excludedStatuses: List<String>,
    ): YoutubeDownloadJobEntity?

    @Query(
        "SELECT * FROM youtube_download_jobs " +
            "ORDER BY updated_at_millis DESC LIMIT :limit"
    )
    fun recent(limit: Int): List<YoutubeDownloadJobEntity>

    @Query(
        """
        SELECT * FROM youtube_download_jobs
        WHERE :beforeUpdatedAtMillis IS NULL
           OR updated_at_millis < :beforeUpdatedAtMillis
           OR (
               updated_at_millis = :beforeUpdatedAtMillis
               AND job_id < :beforeJobId
           )
        ORDER BY updated_at_millis DESC, job_id DESC
        LIMIT :limit
        """
    )
    fun recentPage(
        beforeUpdatedAtMillis: Long?,
        beforeJobId: String?,
        limit: Int,
    ): List<YoutubeDownloadJobEntity>

    @Query("SELECT COUNT(*) FROM youtube_download_jobs WHERE status NOT IN (:terminalStatuses)")
    fun countNonTerminal(terminalStatuses: List<String>): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(job: YoutubeDownloadJobEntity)

    @Update
    fun update(job: YoutubeDownloadJobEntity): Int

    @Query(
        """
        UPDATE youtube_download_jobs
        SET status = :interruptedStatus,
            error_code = :interruptedError,
            updated_at_millis = :nowMillis
        WHERE status IN (:unfinishedStatuses)
        """
    )
    fun markUnfinishedInterrupted(
        unfinishedStatuses: List<String>,
        interruptedStatus: String,
        interruptedError: String,
        nowMillis: Long,
    ): Int
}

@Database(
    entities = [YoutubeDownloadJobEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class YoutubeDownloadDatabase : RoomDatabase() {
    abstract fun jobs(): YoutubeDownloadJobDao

    companion object {
        private const val DATABASE_NAME = "youtube_download_jobs.db"

        @Volatile
        private var instance: YoutubeDownloadDatabase? = null

        fun get(context: Context): YoutubeDownloadDatabase {
            val appContext = context.applicationContext
            return instance ?: synchronized(this) {
                instance ?: open(appContext, DATABASE_NAME)
                    .also { database -> instance = database }
            }
        }

        internal fun open(context: Context, databaseName: String): YoutubeDownloadDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                YoutubeDownloadDatabase::class.java,
                databaseName,
            ).build()
        }
    }
}

internal fun YoutubeDownloadJob.toEntity(): YoutubeDownloadJobEntity {
    return YoutubeDownloadJobEntity(
        id = id,
        canonicalUrl = canonicalUrl,
        videoId = videoId,
        title = title,
        source = source,
        format = format.wireValue,
        formatSelectionVersion = formatSelectionVersion,
        status = status.name,
        progress = progress,
        downloadedBytes = downloadedBytes,
        totalBytes = totalBytes,
        etaSeconds = etaSeconds,
        outputUri = outputUri,
        mediaStoreId = mediaStoreId,
        errorCode = errorCode,
        createdAtMillis = createdAtMillis,
        updatedAtMillis = updatedAtMillis,
    )
}

internal fun YoutubeDownloadJobEntity.toModel(): YoutubeDownloadJob? {
    val parsedFormat = YoutubeDownloadFormat.fromWireValue(format) ?: return null
    if (!isSupportedStoredFormatSelection(parsedFormat, formatSelectionVersion)) return null
    val parsedStatus = runCatching { YoutubeDownloadStatus.valueOf(status) }.getOrNull()
        ?: return null
    return YoutubeDownloadJob(
        id = id,
        canonicalUrl = canonicalUrl,
        videoId = videoId,
        title = title,
        source = source,
        format = parsedFormat,
        formatSelectionVersion = formatSelectionVersion,
        status = parsedStatus,
        progress = progress.coerceIn(0, 100),
        downloadedBytes = downloadedBytes.coerceAtLeast(0L),
        totalBytes = totalBytes,
        etaSeconds = etaSeconds,
        outputUri = outputUri,
        mediaStoreId = mediaStoreId,
        errorCode = errorCode,
        createdAtMillis = createdAtMillis,
        updatedAtMillis = updatedAtMillis,
    )
}

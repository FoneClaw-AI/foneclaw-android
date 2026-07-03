package ai.android.claw.plugin.device.filemanager

import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

data class FileOperationResult(
    val ok: Boolean,
    val text: String,
    val code: String? = null,
)

class FileManagerOperations(
    root: File,
    private val openConnection: (URL) -> HttpURLConnection = { url ->
        url.openConnection() as HttpURLConnection
    },
) {
    private val root: File = root.canonicalFile

    fun listDirectory(path: String, limit: Int): FileOperationResult {
        val target = resolveInsideRoot(path) ?: return pathOutsideRoot()
        if (!target.exists() || !target.isDirectory) {
            return error("directory_not_found", "Directory not found: ${displayPath(target)}")
        }

        val entries = target.listFiles()
            .orEmpty()
            .sortedWith(compareBy<File> { !it.isDirectory }.thenBy { it.name.lowercase() })
        val safeLimit = limit.coerceIn(1, MAX_LIMIT)
        val text = buildString {
            appendLine("Files in ${displayPath(target)}:")
            if (entries.isEmpty()) {
                appendLine("- No files found.")
            } else {
                entries.take(safeLimit).forEach { file ->
                    val kind = if (file.isDirectory) "dir" else "file"
                    val sizeSuffix = if (file.isFile) ", ${file.length()} bytes" else ""
                    appendLine("- [$kind] ${file.name}$sizeSuffix")
                }
                if (entries.size > safeLimit) {
                    appendLine("- ${entries.size - safeLimit} more entries not shown.")
                }
            }
        }.trimEnd()

        return success(text)
    }

    fun search(directory: String, query: String, limit: Int): FileOperationResult {
        val target = resolveInsideRoot(directory) ?: return pathOutsideRoot()
        if (!target.exists() || !target.isDirectory) {
            return error("directory_not_found", "Directory not found: ${displayPath(target)}")
        }

        val trimmedQuery = query.trim()
        if (trimmedQuery.isBlank()) {
            return error("missing_query", "query is required.")
        }

        val safeLimit = limit.coerceIn(1, MAX_LIMIT)
        val matches = target.walkTopDown()
            .filter { file -> file.name.contains(trimmedQuery, ignoreCase = true) }
            .take(safeLimit)
            .toList()

        val text = buildString {
            appendLine("Search results for \"$trimmedQuery\" in ${displayPath(target)}:")
            if (matches.isEmpty()) {
                appendLine("- No files found.")
            } else {
                matches.forEach { file ->
                    val kind = if (file.isDirectory) "dir" else "file"
                    appendLine("- [$kind] ${displayPath(file)}")
                }
            }
        }.trimEnd()

        return success(text)
    }

    fun readText(path: String, maxBytes: Long): FileOperationResult {
        val target = resolveInsideRoot(path) ?: return pathOutsideRoot()
        if (!target.exists() || !target.isFile) {
            return error("file_not_found", "File not found: ${displayPath(target)}")
        }
        val safeMaxBytes = maxBytes.coerceIn(1, MAX_READ_BYTES)
        if (target.length() > safeMaxBytes) {
            return error(
                "file_too_large",
                "File is ${target.length()} bytes, which exceeds maxBytes=$safeMaxBytes.",
            )
        }

        return success(target.readText())
    }

    fun writeText(
        path: String,
        content: String,
        overwrite: Boolean,
        createParents: Boolean,
    ): FileOperationResult {
        val target = resolveInsideRoot(path) ?: return pathOutsideRoot()
        if (target.exists() && target.isDirectory) {
            return error("target_is_directory", "Target is a directory: ${displayPath(target)}")
        }
        if (target.exists() && !overwrite) {
            return error("file_exists", "File already exists: ${displayPath(target)}")
        }
        val parent = target.parentFile
        if (parent != null && !parent.exists()) {
            if (createParents) {
                parent.mkdirs()
            } else {
                return error("parent_not_found", "Parent directory not found: ${displayPath(parent)}")
            }
        }

        target.writeText(content)
        return success("Wrote ${content.length} characters to ${displayPath(target)}.")
    }

    fun createFolder(path: String): FileOperationResult {
        val target = resolveInsideRoot(path) ?: return pathOutsideRoot()
        if (target.exists() && target.isFile) {
            return error("file_exists", "A file already exists at ${displayPath(target)}.")
        }
        if (!target.exists()) {
            target.mkdirs()
        }
        return success("Folder ready: ${displayPath(target)}")
    }

    fun rename(path: String, newName: String, overwrite: Boolean): FileOperationResult {
        val source = resolveInsideRoot(path) ?: return pathOutsideRoot()
        if (!source.exists()) {
            return error("source_not_found", "Source not found: ${displayPath(source)}")
        }
        if (!newName.isSafeFileName()) {
            return error("invalid_file_name", "New name must not be blank or contain path separators.")
        }

        val parent = source.parentFile
            ?: return error("parent_not_found", "Source has no parent directory.")
        val target = parent.resolve(newName).canonicalFile
        if (!target.isInsideRoot()) return pathOutsideRoot()
        if (target.exists() && !overwrite && target != source) {
            return error("target_exists", "Target already exists: ${displayPath(target)}")
        }
        if (target == source) {
            return success("No rename needed for ${displayPath(source)}.")
        }
        if (target.exists() && overwrite) {
            if (target.isDirectory) target.deleteRecursively() else target.delete()
        }

        return if (source.renameTo(target)) {
            success("Renamed ${displayPath(source)} to ${displayPath(target)}.")
        } else {
            error("rename_failed", "Failed to rename ${displayPath(source)}.")
        }
    }

    fun batchRenamePreview(
        directory: String,
        namePattern: String,
        startIndex: Int,
        padding: Int,
        includeDirectories: Boolean,
        limit: Int,
    ): FileOperationResult {
        val planResult = buildBatchRenamePlan(
            directory = directory,
            namePattern = namePattern,
            startIndex = startIndex,
            padding = padding,
            includeDirectories = includeDirectories,
            limit = limit,
        )
        val plan = planResult.getOrElse { return it }
        val text = buildString {
            appendLine("Batch rename preview for ${displayPath(plan.directory)}:")
            appendLine("previewToken=${plan.token}")
            if (plan.items.isEmpty()) {
                appendLine("- No matching entries.")
            } else {
                plan.items.forEach { item ->
                    appendLine("- ${item.source.name} -> ${item.target.name}")
                }
            }
        }.trimEnd()

        return success(text)
    }

    fun batchRenameApply(
        directory: String,
        namePattern: String,
        startIndex: Int,
        padding: Int,
        includeDirectories: Boolean,
        limit: Int,
        previewToken: String,
    ): FileOperationResult {
        val planResult = buildBatchRenamePlan(
            directory = directory,
            namePattern = namePattern,
            startIndex = startIndex,
            padding = padding,
            includeDirectories = includeDirectories,
            limit = limit,
        )
        val plan = planResult.getOrElse { return it }
        if (previewToken.isBlank() || previewToken != plan.token) {
            return error(
                "preview_token_mismatch",
                "Run batch rename preview again and pass the returned previewToken.",
            )
        }

        val renamed = mutableListOf<String>()
        plan.items.forEach { item ->
            if (item.source == item.target) return@forEach
            if (!item.source.renameTo(item.target)) {
                return error("batch_rename_failed", "Failed to rename ${displayPath(item.source)}.")
            }
            renamed += "${item.source.name} -> ${item.target.name}"
        }

        return success(
            buildString {
                appendLine("Batch rename completed:")
                if (renamed.isEmpty()) appendLine("- No files changed.") else renamed.forEach {
                    appendLine("- $it")
                }
            }.trimEnd(),
        )
    }

    fun delete(path: String, recursive: Boolean): FileOperationResult {
        val target = resolveInsideRoot(path) ?: return pathOutsideRoot()
        if (target == root) {
            return error("refuse_root_delete", "Refusing to delete the storage root.")
        }
        if (!target.exists()) {
            return error("target_not_found", "Target not found: ${displayPath(target)}")
        }
        if (target.isDirectory && !recursive) {
            return error("directory_requires_recursive", "Set recursive=true to delete a directory.")
        }

        val deleted = if (target.isDirectory) target.deleteRecursively() else target.delete()
        return if (deleted) {
            success("Deleted ${displayPath(target)}.")
        } else {
            error("delete_failed", "Failed to delete ${displayPath(target)}.")
        }
    }

    fun download(
        url: String,
        path: String,
        overwrite: Boolean,
        maxBytes: Long,
    ): FileOperationResult {
        val parsedUrl = runCatching { URL(url) }.getOrElse {
            return error("invalid_url", "Invalid URL.")
        }
        if (parsedUrl.protocol.lowercase() != "https") {
            return error("unsupported_url_scheme", "Only https downloads are supported.")
        }

        val target = resolveInsideRoot(path) ?: return pathOutsideRoot()
        if (target.exists() && target.isDirectory) {
            return error("target_is_directory", "Target is a directory: ${displayPath(target)}")
        }
        if (target.exists() && !overwrite) {
            return error("file_exists", "File already exists: ${displayPath(target)}")
        }
        val parent = target.parentFile
            ?: return error("parent_not_found", "Target has no parent directory.")
        parent.mkdirs()

        val safeMaxBytes = maxBytes.coerceIn(1, MAX_DOWNLOAD_BYTES)
        val tempFile = File(parent, ".${target.name}.download")
        val connection = openConnection(parsedUrl)
        connection.connectTimeout = DOWNLOAD_TIMEOUT_MILLIS
        connection.readTimeout = DOWNLOAD_TIMEOUT_MILLIS

        return runCatching {
            val responseCode = connection.responseCode
            if (responseCode !in 200..299) {
                return error("download_failed", "Download failed with HTTP $responseCode.")
            }
            val contentLength = connection.contentLengthLong
            if (contentLength > safeMaxBytes) {
                return error("download_too_large", "Download exceeds maxBytes=$safeMaxBytes.")
            }

            var totalBytes = 0L
            connection.inputStream.use { input ->
                tempFile.outputStream().use { output ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    while (true) {
                        val read = input.read(buffer)
                        if (read < 0) break
                        totalBytes += read
                        if (totalBytes > safeMaxBytes) {
                            tempFile.delete()
                            return error("download_too_large", "Download exceeds maxBytes=$safeMaxBytes.")
                        }
                        output.write(buffer, 0, read)
                    }
                }
            }
            if (target.exists()) {
                if (target.isDirectory) target.deleteRecursively() else target.delete()
            }
            if (!tempFile.renameTo(target)) {
                tempFile.copyTo(target, overwrite = true)
                tempFile.delete()
            }
            success("Downloaded $totalBytes bytes to ${displayPath(target)}.")
        }.getOrElse { error ->
            tempFile.delete()
            error("download_failed", error.message ?: "Download failed.")
        }.also {
            connection.disconnect()
        }
    }

    private fun buildBatchRenamePlan(
        directory: String,
        namePattern: String,
        startIndex: Int,
        padding: Int,
        includeDirectories: Boolean,
        limit: Int,
    ): BatchRenamePlanResult {
        val targetDirectory = resolveInsideRoot(directory) ?: return BatchRenamePlanResult.Error(pathOutsideRoot())
        if (!targetDirectory.exists() || !targetDirectory.isDirectory) {
            return BatchRenamePlanResult.Error(
                error("directory_not_found", "Directory not found: ${displayPath(targetDirectory)}"),
            )
        }
        if (namePattern.isBlank()) {
            return BatchRenamePlanResult.Error(error("missing_pattern", "namePattern is required."))
        }

        val entries = targetDirectory.listFiles()
            .orEmpty()
            .filter { file -> includeDirectories || file.isFile }
            .sortedBy { file -> file.name.lowercase() }
            .take(limit.coerceIn(1, MAX_BATCH_RENAME))

        val usedTargets = mutableSetOf<String>()
        val items = entries.mapIndexed { index, source ->
            val targetName = buildTargetName(
                source = source,
                namePattern = namePattern,
                index = startIndex + index,
                padding = padding.coerceIn(1, 10),
            )
            if (!targetName.isSafeFileName()) {
                return BatchRenamePlanResult.Error(
                    error("invalid_target_name", "Generated invalid file name: $targetName"),
                )
            }
            val parent = source.parentFile ?: return BatchRenamePlanResult.Error(
                error("parent_not_found", "Source has no parent directory."),
            )
            val target = parent.resolve(targetName).canonicalFile
            if (!target.isInsideRoot()) {
                return BatchRenamePlanResult.Error(pathOutsideRoot())
            }
            val targetKey = target.path.lowercase()
            if (!usedTargets.add(targetKey)) {
                return BatchRenamePlanResult.Error(
                    error("duplicate_target_name", "Generated duplicate target: $targetName"),
                )
            }
            if (target.exists() && target != source) {
                return BatchRenamePlanResult.Error(
                    error("target_exists", "Target already exists: ${displayPath(target)}"),
                )
            }
            BatchRenameItem(source = source, target = target)
        }

        return BatchRenamePlanResult.Success(
            BatchRenamePlan(
                directory = targetDirectory,
                items = items,
                token = batchRenameToken(
                    directory = targetDirectory,
                    namePattern = namePattern,
                    startIndex = startIndex,
                    padding = padding,
                    includeDirectories = includeDirectories,
                    items = items,
                ),
            ),
        )
    }

    private fun buildTargetName(
        source: File,
        namePattern: String,
        index: Int,
        padding: Int,
    ): String {
        val extension = source.extension.takeIf { value -> value.isNotBlank() }?.let { value ->
            ".$value"
        }.orEmpty()
        val baseName = source.name.removeSuffix(extension)
        val indexText = index.toString().padStart(padding, '0')
        val replaced = namePattern
            .replace("{index}", indexText)
            .replace("{name}", baseName)
            .replace("{ext}", extension)
        return if ("{ext}" in namePattern || extension.isBlank() || source.isDirectory) {
            replaced
        } else {
            replaced + extension
        }
    }

    private fun batchRenameToken(
        directory: File,
        namePattern: String,
        startIndex: Int,
        padding: Int,
        includeDirectories: Boolean,
        items: List<BatchRenameItem>,
    ): String {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(directory.path.toByteArray())
        digest.update(namePattern.toByteArray())
        digest.update(startIndex.toString().toByteArray())
        digest.update(padding.toString().toByteArray())
        digest.update(includeDirectories.toString().toByteArray())
        items.forEach { item ->
            digest.update(item.source.name.toByteArray())
            digest.update(item.source.length().toString().toByteArray())
            digest.update(item.source.lastModified().toString().toByteArray())
            digest.update(item.target.name.toByteArray())
        }
        return digest.digest().joinToString("") { byte -> "%02x".format(byte) }
    }

    private fun resolveInsideRoot(path: String): File? {
        val trimmedPath = path.trim().trim('/', '\\')
        val target = if (trimmedPath.isBlank()) root else File(root, trimmedPath)
        val canonicalTarget = runCatching { target.canonicalFile }.getOrNull() ?: return null
        return canonicalTarget.takeIf { file -> file.isInsideRoot() }
    }

    private fun File.isInsideRoot(): Boolean {
        return path == root.path || path.startsWith(root.path + File.separator)
    }

    private fun displayPath(file: File): String {
        return if (file.path == root.path) "." else file.relativeTo(root).path
    }

    private fun String.isSafeFileName(): Boolean {
        return isNotBlank() && this != "." && this != ".." && none { char ->
            char == '/' || char == '\\'
        }
    }

    private fun success(text: String): FileOperationResult {
        return FileOperationResult(ok = true, text = text)
    }

    private fun error(code: String, text: String): FileOperationResult {
        return FileOperationResult(ok = false, text = text, code = code)
    }

    private fun pathOutsideRoot(): FileOperationResult {
        return error("path_outside_root", "Path must stay inside the managed storage root.")
    }

    private data class BatchRenameItem(
        val source: File,
        val target: File,
    )

    private data class BatchRenamePlan(
        val directory: File,
        val items: List<BatchRenameItem>,
        val token: String,
    )

    private sealed class BatchRenamePlanResult {
        data class Success(val plan: BatchRenamePlan) : BatchRenamePlanResult()
        data class Error(val result: FileOperationResult) : BatchRenamePlanResult()

        inline fun getOrElse(onError: (FileOperationResult) -> Nothing): BatchRenamePlan {
            return when (this) {
                is Success -> plan
                is Error -> onError(result)
            }
        }
    }

    private companion object {
        const val MAX_LIMIT = 500
        const val MAX_BATCH_RENAME = 500
        const val MAX_READ_BYTES = 1_048_576L
        const val MAX_DOWNLOAD_BYTES = 104_857_600L
        const val DOWNLOAD_TIMEOUT_MILLIS = 15_000
    }
}

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import java.security.MessageDigest

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
}

data class PluginSpec(
    val category: String,
    val name: String,
) {
    val projectPath: String = ":src:$category:$name"
    val moduleDir: File = rootDir.resolve("src/$category/$name")
    val manifestJsonFile: File = moduleDir.resolve("src/main/res/raw/foneclaw_extension.json")
    val skillDir: File = moduleDir.resolve("src/main/assets")
}

val selectedPluginProvider = providers.gradleProperty("plugin")
val publishBuildTypeProvider = providers.gradleProperty("publishBuildType").orElse("debug")

tasks.register("validatePlugin") {
    group = "foneclaw plugin"
    description = "Validate one selected plugin with -Pplugin=category:name."

    doLast {
        val spec = selectedPluginProvider.orNull?.toPluginSpec()
            ?: error("Missing -Pplugin=category:name, for example -Pplugin=device:file-manager")
        validatePluginSpec(spec)
    }
}

tasks.register("publishPlugin") {
    group = "foneclaw plugin"
    description = "Build and publish one selected plugin APK into ../plugins."

    selectedPluginProvider.orNull?.toPluginSpec()?.let { spec ->
        dependsOn("${spec.projectPath}:assemble${publishBuildTypeProvider.get().variantName()}")
    }

    doLast {
        val spec = selectedPluginProvider.orNull?.toPluginSpec()
            ?: error("Missing -Pplugin=category:name, for example -Pplugin=device:file-manager")
        validatePluginSpec(spec)
        publishPluginSpec(spec, publishBuildTypeProvider.get())
    }
}

tasks.register("publishAllPlugins") {
    group = "foneclaw plugin"
    description = "Build and publish all plugin modules under src/<category>/<plugin>."

    val specs = discoverPluginSpecs()
    specs.forEach { spec ->
        dependsOn("${spec.projectPath}:assemble${publishBuildTypeProvider.get().variantName()}")
    }

    doLast {
        val specsToPublish = discoverPluginSpecs()
        require(specsToPublish.isNotEmpty()) { "No plugin modules found under src/<category>/<plugin>." }
        specsToPublish.forEach { spec ->
            validatePluginSpec(spec)
            publishPluginSpec(spec, publishBuildTypeProvider.get())
        }
    }
}

fun String.toPluginSpec(): PluginSpec {
    val parts = split(":")
    require(parts.size == 2 && parts.all { it.isNotBlank() }) {
        "Plugin must use category:name format, for example device:file-manager"
    }
    return PluginSpec(category = parts[0], name = parts[1])
}

fun discoverPluginSpecs(): List<PluginSpec> {
    val sourceDir = rootDir.resolve("src")
    if (!sourceDir.isDirectory) return emptyList()
    return sourceDir.listFiles()
        .orEmpty()
        .filter { category -> category.isDirectory }
        .flatMap { category ->
            category.listFiles()
                .orEmpty()
                .filter { plugin -> plugin.isDirectory }
                .map { plugin -> PluginSpec(category = category.name, name = plugin.name) }
        }
        .sortedWith(compareBy({ it.category }, { it.name }))
}

fun validatePluginSpec(spec: PluginSpec) {
    require(spec.moduleDir.isDirectory) { "Plugin module does not exist: ${spec.moduleDir}" }
    require(spec.manifestJsonFile.isFile) {
        "Plugin manifest JSON is missing: ${spec.manifestJsonFile}"
    }

    val manifest = spec.readManifest()
    val extensionId = manifest.stringValue("extensionId")
    val packageName = manifest.stringValue("packageName")
    val versionName = manifest.stringValue("versionName")
    val versionCode = manifest.longValue("versionCode")
    require(extensionId.isFoneClawPluginName()) {
        "Plugin name must use foneclaw:<name> format: $extensionId"
    }
    require(extensionId == "foneclaw:${spec.name}") {
        "Plugin name must match module name foneclaw:${spec.name}: $extensionId"
    }
    require(packageName.startsWith("ai.android.claw.plugin.")) {
        "Plugin packageName must start with ai.android.claw.plugin.: $packageName"
    }
    require(versionName.isNotBlank()) { "Plugin versionName cannot be blank." }
    require(versionCode > 0) { "Plugin versionCode must be positive." }

    val tools = manifest.listValue("tools")
    require(tools.isNotEmpty()) { "Plugin must declare at least one tool." }
    val toolNames = tools.map { tool -> tool.mapValue().stringValue("name") }
    val duplicate = toolNames.groupingBy { it }.eachCount()
        .filterValues { count -> count > 1 }
        .keys
        .firstOrNull()
    require(duplicate == null) { "Duplicate plugin tool name: $duplicate" }
    toolNames.forEach { toolName ->
        require(toolName.startsWith("plugin_")) {
            "Plugin tool name must start with plugin_: $toolName"
        }
    }

    val skill = manifest["skill"] as? Map<*, *>
    if (skill != null) {
        val entry = skill.stringValue("entry")
        require(spec.skillDir.resolve(entry).isFile) {
            "Skill entry is declared but missing: ${spec.skillDir.resolve(entry)}"
        }
    }
}

fun publishPluginSpec(spec: PluginSpec, buildType: String) {
    val manifest = spec.readManifest()
    val extensionId = manifest.stringValue("extensionId")
    val packageName = manifest.stringValue("packageName")
    val displayName = manifest.stringValue("displayName")
    val versionName = manifest.stringValue("versionName")
    val versionCode = manifest.longValue("versionCode")
    val tools = manifest.repoToolEntries()

    val apkSourceDir = spec.moduleDir.resolve("build/outputs/apk/$buildType")
    val sourceApk = apkSourceDir.listFiles()
        .orEmpty()
        .filter { file -> file.isFile && file.extension == "apk" }
        .maxByOrNull { file -> file.lastModified() }
        ?: error("APK output cannot be found in $apkSourceDir")
    validateApkMetadataMatchesManifest(
        apkSourceDir = apkSourceDir,
        sourceApk = sourceApk,
        versionName = versionName,
        versionCode = versionCode,
    )

    val pluginsDir = rootDir.parentFile.resolve("plugins")
    val apksDir = pluginsDir.resolve("apks")
    apksDir.mkdirs()

    val apkFileName = "$packageName-v$versionName-$versionCode.apk"
    val targetApk = apksDir.resolve(apkFileName)
    sourceApk.copyTo(targetApk, overwrite = true)

    writeRepoJson(pluginsDir)
    updateIndexJson(
        pluginsDir = pluginsDir,
        entry = linkedMapOf(
            "schema" to 2,
            "name" to extensionId,
            "displayName" to displayName,
            "pkg" to packageName,
            "apk" to "apks/$apkFileName",
            "version" to versionName,
            "code" to versionCode,
            "lang" to spec.category,
            "sha256" to targetApk.sha256(),
            "signingSha256" to OFFICIAL_PLUGIN_SIGNING_FINGERPRINT.normalizedFingerprint(),
            "minHostVersion" to MIN_HOST_VERSION,
            "capabilities" to spec.repoCapabilities(),
            "tools" to tools,
            "permissions" to spec.repoPermissions(),
        ),
    )

    logger.lifecycle("Published ${targetApk.relativeTo(rootDir.parentFile)}")
}

fun validateApkMetadataMatchesManifest(
    apkSourceDir: File,
    sourceApk: File,
    versionName: String,
    versionCode: Long,
) {
    val metadataFile = apkSourceDir.resolve("output-metadata.json")
    require(metadataFile.isFile) { "APK output metadata is missing: $metadataFile" }

    val metadata = JsonSlurper().parse(metadataFile).mapValue()
    val apkMetadata = metadata.listValue("elements")
        .map { element -> element.mapValue() }
        .firstOrNull { element -> element.stringValue("outputFile") == sourceApk.name }
        ?: error("APK metadata does not contain output file: ${sourceApk.name}")
    val apkVersionName = apkMetadata.stringValue("versionName")
    val apkVersionCode = apkMetadata.longValue("versionCode")

    require(apkVersionName == versionName && apkVersionCode == versionCode) {
        "APK manifest version $apkVersionName/$apkVersionCode does not match " +
            "plugin manifest $versionName/$versionCode"
    }
}

fun writeRepoJson(pluginsDir: File) {
    pluginsDir.mkdirs()
    val repoJson = linkedMapOf(
        "index" to "index.min.json",
        "meta" to linkedMapOf(
            "name" to "FoneClaw Official Plugins",
            "website" to "https://example.com/foneclaw/plugins",
            "signingKeyFingerprint" to OFFICIAL_PLUGIN_SIGNING_FINGERPRINT,
        ),
    )
    pluginsDir.resolve("repo.json").writeText(JsonOutput.prettyPrint(JsonOutput.toJson(repoJson)))
}

fun updateIndexJson(pluginsDir: File, entry: Map<String, Any>) {
    val indexFile = pluginsDir.resolve("index.min.json")
    val existingEntries = if (indexFile.isFile) {
        @Suppress("UNCHECKED_CAST")
        (JsonSlurper().parse(indexFile) as? List<Map<String, Any>>).orEmpty()
    } else {
        emptyList()
    }
    val packageName = entry["pkg"]
    val updatedEntries = existingEntries
        .filter { existing -> existing["pkg"] != packageName }
        .plus(entry)
        .sortedBy { existing -> existing["pkg"].toString() }
    indexFile.writeText(JsonOutput.toJson(updatedEntries))
}

fun PluginSpec.readManifest(): Map<*, *> {
    return JsonSlurper().parse(manifestJsonFile) as Map<*, *>
}

fun Any?.mapValue(): Map<*, *> {
    return this as? Map<*, *> ?: error("Expected JSON object but got: $this")
}

fun Map<*, *>.stringValue(key: String): String {
    return this[key]?.toString().orEmpty()
}

fun Map<*, *>.longValue(key: String): Long {
    return when (val value = this[key]) {
        is Number -> value.toLong()
        is String -> value.toLong()
        else -> error("Missing or invalid number: $key")
    }
}

fun Map<*, *>.listValue(key: String): List<*> {
    return this[key] as? List<*> ?: error("Missing or invalid list: $key")
}

fun Map<*, *>.repoToolEntries(): List<Map<String, Any>> {
    return listValue("tools")
        .map { tool -> tool.mapValue() }
        .map { tool ->
            linkedMapOf(
                "name" to tool.stringValue("name"),
                "displayName" to tool.stringValue("displayName"),
                "description" to tool.stringValue("description"),
                "risk" to tool.stringValue("risk"),
                "approvalMode" to tool.stringValue("approvalMode"),
            )
        }
}

fun PluginSpec.repoCapabilities(): List<String> {
    return when ("${category}:${name}") {
        "device:file-manager" -> listOf(
            "file.status",
            "file.open_settings",
            "file.list",
            "file.search",
            "file.read_text",
            "file.create",
            "file.write_text",
            "file.create_folder",
            "file.rename",
            "file.batch_rename",
            "file.delete",
            "file.download",
        )
        else -> emptyList()
    }
}

fun PluginSpec.repoPermissions(): List<String> {
    return when ("${category}:${name}") {
        "device:file-manager" -> listOf(
            "android.permission.MANAGE_EXTERNAL_STORAGE",
            "android.permission.INTERNET",
        )
        else -> emptyList()
    }
}

fun String.variantName(): String {
    return replaceFirstChar { char ->
        if (char.isLowerCase()) char.titlecase() else char.toString()
    }
}

fun String.isFoneClawPluginName(): Boolean {
    return matches(Regex("""foneclaw:[a-z0-9][a-z0-9-]{0,62}"""))
}

fun File.sha256(): String {
    val digest = MessageDigest.getInstance("SHA-256")
    inputStream().use { input ->
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        while (true) {
            val read = input.read(buffer)
            if (read <= 0) break
            digest.update(buffer, 0, read)
        }
    }
    return digest.digest().joinToString("") { byte -> "%02x".format(byte) }
}

fun String.normalizedFingerprint(): String {
    return filterNot { char -> char == ':' || char.isWhitespace() }
        .lowercase()
}

val MIN_HOST_VERSION = "0.0.6"
val OFFICIAL_PLUGIN_SIGNING_FINGERPRINT =
    "E3:31:65:67:CC:A6:49:2D:B9:42:00:80:C9:4D:6E:A4:" +
        "C3:31:88:67:79:70:DA:58:CE:71:7B:BF:3C:6F:3A:84"

pluginManagement {
    includeBuild("gradle/build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "FoneClawPluginSource"
include(":api")

val selectedPlugin = providers.gradleProperty("plugin").orNull
if (selectedPlugin != null) {
    val parts = selectedPlugin.split(":")
    require(parts.size == 2 && parts.all { it.isNotBlank() }) {
        "Plugin must use category:name format, for example -Pplugin=device:file-manager"
    }
    includePlugin(parts[0], parts[1])
} else {
    loadAllPlugins()
}

fun loadAllPlugins() {
    val sourceDir = file("src")
    if (!sourceDir.isDirectory) return
    sourceDir.eachDir { categoryDir ->
        categoryDir.eachDir { pluginDir ->
            includePlugin(categoryDir.name, pluginDir.name)
        }
    }
}

fun includePlugin(category: String, name: String) {
    val projectPath = ":src:$category:$name"
    include(projectPath)
    project(projectPath).projectDir = file("src/$category/$name")
}

fun File.eachDir(block: (File) -> Unit) {
    listFiles()
        ?.filter { file -> file.isDirectory && file.name != "build" && file.name != ".gradle" }
        ?.sortedBy { file -> file.name }
        ?.forEach(block)
}


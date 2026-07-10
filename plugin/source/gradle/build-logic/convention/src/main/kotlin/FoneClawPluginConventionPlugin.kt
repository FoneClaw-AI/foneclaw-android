import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType

abstract class FoneClawPluginExtension {
    abstract val pluginId: Property<String>
    abstract val packageName: Property<String>
    abstract val displayName: Property<String>
    abstract val versionName: Property<String>
    abstract val versionCode: Property<Int>
    abstract val category: Property<String>
}

class FoneClawPluginConventionPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.pluginManager.apply("com.android.application")

        val extension = project.extensions.create<FoneClawPluginExtension>("foneclawPlugin")
        val android = project.extensions.getByType<ApplicationExtension>()
        val androidComponents = project.extensions.getByType<ApplicationAndroidComponentsExtension>()
        val packageName = project.defaultPluginPackageName()
        val pluginSigningStore = project.rootDir
            .resolve("androidclaw-plugin.jks")
            .takeIf { file -> file.isFile }

        extension.packageName.convention(packageName)
        extension.versionCode.convention(1)
        extension.versionName.convention("0.0.1")

        android.apply {
            namespace = extension.packageName.get()
            compileSdk = 36

            defaultConfig {
                applicationId = extension.packageName.get()
                minSdk = 30
                targetSdk = 36
                versionCode = extension.versionCode.get()
                versionName = extension.versionName.get()
            }

            buildFeatures {
                aidl = true
            }

            buildTypes {
                val pluginSigningConfig = pluginSigningStore?.let { storeFile ->
                    signingConfigs.maybeCreate("foneClawPluginPublic").apply {
                        this.storeFile = storeFile
                        storePassword = "androidclaw-plugin123"
                        keyAlias = "androidclaw-plugin"
                        keyPassword = "androidclaw-plugin123"
                    }
                }
                getByName("debug") {
                    signingConfig = pluginSigningConfig ?: signingConfig
                }
                getByName("release") {
                    isMinifyEnabled = false
                    signingConfig = pluginSigningConfig ?: signingConfig
                }
            }

            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_11
                targetCompatibility = JavaVersion.VERSION_11
            }
        }

        androidComponents.finalizeDsl {
            val pluginId = extension.pluginId.get()
            require(pluginId.isFoneClawPluginName()) {
                "Plugin name must use foneclaw:<name> format: $pluginId"
            }
            require(pluginId == "foneclaw:${project.name}") {
                "Plugin name must match module name foneclaw:${project.name}: $pluginId"
            }
            android.namespace = extension.packageName.get()
            android.defaultConfig.applicationId = extension.packageName.get()
            android.defaultConfig.versionCode = extension.versionCode.get()
            android.defaultConfig.versionName = extension.versionName.get()
        }

        project.dependencies.add(
            "implementation",
            project.dependencies.project(mapOf("path" to ":api")),
        )
    }

    private fun Project.defaultPluginPackageName(): String {
        val parts = path.split(":").filter { part -> part.isNotBlank() }
        val category = parts.getOrNull(1).orEmpty()
        val pluginName = parts.getOrNull(2).orEmpty().replace("-", "")
        require(parts.firstOrNull() == "src" && category.isNotBlank() && pluginName.isNotBlank()) {
            "Plugin project path must use :src:<category>:<plugin>, got $path"
        }
        return "ai.android.claw.plugin.$category.$pluginName"
    }
}

private fun String.isFoneClawPluginName(): Boolean {
    return matches(Regex("""foneclaw:[a-z0-9][a-z0-9-]{0,62}"""))
}

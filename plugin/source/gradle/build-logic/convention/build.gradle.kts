plugins {
    `kotlin-dsl`
}

dependencies {
    compileOnly("com.android.tools.build:gradle:9.1.0-rc01")
}

gradlePlugin {
    plugins {
        register("foneclawPlugin") {
            id = "foneclaw.plugin"
            implementationClass = "FoneClawPluginConventionPlugin"
        }
    }
}

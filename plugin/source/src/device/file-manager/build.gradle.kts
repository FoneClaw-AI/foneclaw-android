plugins {
    id("foneclaw.plugin")
}

foneclawPlugin {
    pluginId.set("foneclaw.device.file_manager")
    packageName.set("ai.android.claw.plugin.device.filemanager")
    displayName.set("File Manager Plugin")
    versionName.set("0.0.2")
    versionCode.set(2)
    category.set("device")
}

dependencies {
    testImplementation("junit:junit:4.13.2")
}

plugins {
    id("foneclaw.plugin")
}

foneclawPlugin {
    pluginId.set("foneclaw:file-manager")
    packageName.set("ai.android.claw.plugin.device.filemanager")
    displayName.set("File Manager Plugin")
    versionName.set("0.0.3")
    versionCode.set(3)
    category.set("device")
}

dependencies {
    testImplementation("junit:junit:4.13.2")
}

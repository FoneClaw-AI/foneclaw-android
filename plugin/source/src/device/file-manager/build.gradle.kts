plugins {
    id("foneclaw.plugin")
}

foneclawPlugin {
    pluginId.set("foneclaw:file-manager")
    packageName.set("ai.android.claw.plugin.device.filemanager")
    displayName.set("File Manager Plugin")
    versionName.set("0.0.4")
    versionCode.set(4)
    category.set("device")
}

dependencies {
    testImplementation("junit:junit:4.13.2")
}

# Third-Party Notices

This standalone plugin APK includes the following software:

| Component | Version | License | Source |
|---|---:|---|---|
| AndroidX Room | 2.8.4 | Apache-2.0 | <https://developer.android.com/jetpack/androidx/releases/room> |
| youtubedl-android library | 0.18.1 | GPL-3.0 | <https://github.com/yausername/youtubedl-android> |
| yt-dlp bundled by youtubedl-android | 2025-11-12 | Unlicense | <https://github.com/yt-dlp/yt-dlp> |
| FFmpeg package from youtubedl-android | 0.18.1 package | See upstream build and license terms | <https://github.com/yausername/youtubedl-android> |

The YouTube Downloader Plugin is distributed separately from the FoneClaw host APK. Before a
public release, the release owner must provide the complete corresponding source and build
instructions for the exact plugin APK, include the GPL-3.0 license text, and record the exact
FFmpeg configuration and applicable LGPL/GPL terms.

The plugin does not silently update yt-dlp at runtime. Updating the extractor requires a new,
tested plugin release.

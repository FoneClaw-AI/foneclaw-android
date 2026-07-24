---
name: youtube-downloader-plugin
description: Download a public YouTube video or Short as user-selected video or audio.
version: 0.0.1
---

# YouTube Downloader

Use this Skill only when the user asks to download or save media from one supported public
YouTube video or Short.

## Required flow

1. Call `plugin_youtube_video_info` with exactly one URL.
2. Show the returned title, uploader, duration, and only the entries present in `formats`.
   For video, show the returned exact width, height, and estimated size. For audio, show the
   container, bitrate when present, and estimated size. Use one compact bullet per choice rather
   than a table so every size remains visible on a small phone screen.
3. Ask the user to choose one exact returned `format` value unless it is already explicit. Never
   invent or offer a quality that is absent from `formats`.
4. Before calling `plugin_youtube_download`, tell the user to download only content they own
   or are authorized to save.
5. Call `plugin_youtube_download` only after the user has explicitly chosen the format.
6. Keep the returned `jobId`. Use `plugin_youtube_download_status` when the user asks for
   progress, output location, or recent jobs.
7. If `plugin_youtube_download` returns `alreadyInProgress=true`, report the existing job's current
   status and do not call the download tool again. The existing job's format remains authoritative.
8. Call `plugin_youtube_download_cancel` only when the user asks to cancel a specific job.

For a URL received from Android sharing, set `source` to `share`. For a URL pasted or entered
in Home, set `source` to `home_link`. Otherwise use `agent_tool`.

## Boundaries

- Accept only HTTPS `youtube.com`, `www.youtube.com`, `m.youtube.com`, `youtu.be`, and Shorts
  URLs accepted by the tool.
- Public single videos and Shorts are supported. Playlists and live streams are not supported.
- Do not request or supply cookies, account credentials, OAuth tokens, proxy bypasses, raw
  yt-dlp arguments, output templates, or filesystem paths.
- Do not claim support for private, paid, member-only, age-restricted, region-bypassed, or DRM
  content.
- A queued response means the foreground download started; it does not mean the file finished.
- Video format names are exact resolutions. Never describe `video_1080p` as available unless it
  was returned by `plugin_youtube_video_info`; unavailable resolutions are not silently reduced.
- Video files are published under `Movies/FoneClaw`; audio files under `Music/FoneClaw`.
- The plugin is local and free. It does not upload the URL to FoneClaw services or use credits.

## Failure handling

- `YTDL_INVALID_URL` or `YTDL_MULTIPLE_URLS`: ask for exactly one supported URL.
- `YTDL_AUTH_REQUIRED`: explain that login-restricted content is unsupported; do not ask for
  cookies.
- `YTDL_EXTRACTOR_OUTDATED`: ask the user to update the plugin.
- `YTDL_STORAGE_LOW`: ask the user to free storage before retrying.
- `YTDL_INTERRUPTED`: calling `plugin_youtube_download` again with the same URL and format
  resumes the existing job when possible.

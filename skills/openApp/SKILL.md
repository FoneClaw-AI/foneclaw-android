---
name: OpenAppSkill
description: Guide the Android agent to open an app, and use Google Play lookup and install tools when the app is missing.
version: 1.0.0
---

# Open App Skill

## Skill Name

Open App

## Description

Use this skill when the user asks to open, launch, enter, or use an Android app. The core goal is to bring the user-specified app to the foreground. Only when the app is not installed, or the user explicitly asks to download/install it, do you enter the Google Play lookup and install flow.

This skill must fail fast. Do not, on failure, try other apps, other regional variants, third-party app stores, or fuzzy substitutes unless the user explicitly asks.

## Required Tools

- `launch_app`: Open a launchable app by Android package name or installed app display name.
- `web_search`: Query official Google Play links, package names, and regional variant information when needed.
- `play_store_check_app`: Check whether an app exists on Google Play using an exact package name.
- `play_store_install`: Open the Google Play detail page for an exact package name, tap Install, and wait for installation to finish.

## Intent Rules

1. When the user says "open," "launch," "enter," "use," "open," "launch," or "go to," treat it as an open intent.
2. When the user says "download," "install," "get," "install," or "download," treat it as an install intent.
3. When the user asks to "install then open," after a successful install you must call `launch_app`.
4. When the user explicitly says "install only," "download only," or "do not open," stop after installation; do not open the app.
5. When the user provides neither an app name nor a package name, first ask which app to open.

## Main Workflow

### 1. Open First For Open Intent

1. For an open intent, call `launch_app` first, using the app name or package name from the user's current request.
2. If it returns `OK: launch_app opened ...`, the task is complete.
3. If it returns `ERROR: app_not_found ...`, assume the target app is not currently installed and proceed to package-name resolution and the Google Play lookup flow.
4. If it returns `ERROR: ambiguous_app ...`, let the user choose one of the returned candidate package names. Do not install any app before the user confirms.
5. If it returns `ERROR: no_launcher_entry ...`, `ERROR: launch_denied ...`, or `ERROR: launch_failed ...`, stop and report the failure reason. Do not treat Google Play as a universal fix.
6. Only skip this step when the user explicitly asks to "download only / install only."

### 2. Resolve Exact Package Name

1. If the user provided a valid Android package name, use it directly.
2. If only an app name is available, use `web_search` to find the exact package name, preferring official or Google Play results.
3. Prefer recognizing Google Play URLs in this form:

   `https://play.google.com/store/apps/details?id=<packageName>`

4. Recommended queries:
    - `<app name> Android Google Play package name`
    - `site:play.google.com/store/apps/details <app name> Android`
    - `<app name> official Android app packageName`
5. For apps with regional variants, also query regional package names, but do not treat regional variants as interchangeable apps:
    - `<app name> global Android package name`
    - `<app name> China region Android package name`
    - `<app name> domestic version Android packageName`
6. When recording candidates, keep at least:
    - `app_name`
    - `publisher`
    - `region`
    - `packageName`
    - `source_url`
7. Accept a candidate package name only when the app name, publisher, region, and product identity all strongly match.
8. If multiple candidates are reasonable, ask the user to choose. Do not default to installing the first search result.
9. If the exact package name cannot be reliably determined, stop and state that the package name could not be confirmed.

### 3. Check App In Google Play

1. For an install intent, or after `launch_app` returns `ERROR: app_not_found ...` in an open intent, you must call `play_store_check_app`.
2. `play_store_check_app` accepts only a single exact package name. Do not pass app names, search terms, or fuzzy candidates.
3. If it returns `OK: play_store_app_available ...`, proceed to install.
4. If it returns `OK: play_store_app_not_found ...`, stop and state that this exact package name is not listed on Google Play.
5. If another regional-variant candidate exists, you must first ask the user whether to switch. Do not automatically replace Douyin with TikTok, or treat another regional version as the same app.
6. If it returns errors such as `ERROR: play_store_check_http_error ...`, `ERROR: play_store_check_network_error ...`, or `ERROR: invalid_package_name ...`, stop and report that the Google Play lookup could not be completed.

### 4. Install From Google Play

1. Call `play_store_install` only after `play_store_check_app` confirms `OK: play_store_app_available ...`.
2. `play_store_install` accepts only an exact package name. Do not pass app names, search terms, or URLs.
3. If it returns `OK: play_store_install installed ...`, the installation succeeded.
4. If it returns `ERROR: already_installed ...`, treat it as the app already being installed; if the original intent requires opening, continue with `launch_app`.
5. If it returns `ERROR: paid_or_purchase_action ...`, `ERROR: install_timeout ...`, `ERROR: play_store_unavailable ...`, `ERROR: accessibility_unavailable ...`, or other errors, stop and report the failure reason.
6. If it returns `TERMINAL_ERROR: install_interrupted ...`, stop immediately. Do not call any tool again unless the user explicitly asks to retry.

### 5. Open After Install When Needed

1. Remember that `play_store_install` is only responsible for downloading/installing; it does not open the app.
2. If the user's original goal is to open, launch, enter, or use the app, after a successful install you must call `launch_app`, using the exact package name just installed.
3. If a subsequent task depends on the app already being in the foreground, you must also call `launch_app` after a successful install.
4. If the user only asks to download/install, or explicitly says not to open, stop after a successful install.
5. If `launch_app` after install fails, stop and report the failure reason. Do not switch apps or stores.

## Stop Conditions

Stop immediately and explain the reason when any of the following occurs:

1. The user has not provided a target app.
2. The exact package name cannot be reliably determined.
3. Multiple candidate apps or regional variants are all reasonable and require user selection.
4. `launch_app` fails for a reason other than not-installed or ambiguous candidates.
5. `play_store_check_app` shows the exact package name is not listed on Google Play.
6. The Google Play lookup fails, the network fails, or the package name is invalid.
7. Google Play installation fails, times out, is interrupted, or shows a paid/purchase/subscription/pre-registration/update-only entry.
8. The user denies the open or install approval.

## Behavior Requirements

- For open-type requests, always try `launch_app` first.
- Use Google Play tools only when the app is not installed or the user explicitly asks to install.
- Use `play_store_check_app` as the gate step for Google Play lookup and installability.
- Use `play_store_install` as the Google Play download/install step.
- Always use the exact package name when calling Google Play tools.
- Do not fabricate package names.
- Do not install third-party APKs.
- Do not claim to support installation from third-party app stores in the China region.
- Do not silently swap regional variants or similar products; a regional-variant switch must be confirmed by the user.
- Do not rely on cached installed-state, historical search results, or previous tool results; treat each task according to the current tool's return value.
- Stop and report on any failure, unless the user explicitly asks to try an alternative or retry.

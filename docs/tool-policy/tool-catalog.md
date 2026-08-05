# Built-in Tool Catalog

This catalog is the public reference for writing FoneClaw Skills and Workflow templates.
Skills do not add new device capabilities. They teach the agent how to combine the built-in tools below.

For safety behavior, see [Tool Policy System](overview.md).

## Catalog Summary

- Public tools: **118**
- Categories: **11**

## How to Use This Catalog

1. Pick the smallest tool set that matches the user's goal.
2. List every tool you rely on in the Skill's `Available Tools` section.
3. Respect each tool's risk level and approval behavior.
4. Add failure paths for missing permissions, empty results, unavailable apps, and denied approvals.
5. Do not invent tool names. Use the exact names in this document.

## Risk and Approval Quick Reference

| Risk | Approval | Meaning |
|---|---|---|
| LOW | Auto | Read-only, no sensitive data, no side effects. |
| SENSITIVE_READ | Require approval | Reads private or user-specific data. |
| DEVICE_CONTROL | Require approval | Changes local device state or opens device UI. |
| EXTERNAL_EFFECT | Require approval | Affects external systems or the real world. |
| DESTRUCTIVE | Require approval | Deletes or permanently removes user data. |

## Screen and App Tools

| Tool | Risk | Approval | What it does |
|---|---|---|---|
| `camera_open_photo` | SENSITIVE_READ | Require approval | Open a specific visible photo from the system gallery. Use this only when the user explicitly asks to view or open a specific photo. Do not use this as a follow-up after camera_take_photo unless the user explicitly asks to view or open the captured photo. indexFromLatest is 1-based in newest-first order: 1 opens the latest photo, 2 opens the second latest photo, 3 opens the third latest photo. |
| `camera_take_photo` | SENSITIVE_READ | Require approval | Capture a new photo and return the saved photo URI when available. Use this when the user's intent is to capture a new photo. Leave lensFacing unset or auto for normal photo capture. Choose lensFacing=front only for front-facing or selfie intent; choose back only when the user explicitly asks for the back camera. Keep explicitLensSelection false for plain/default photo capture, even if the default physical lens is back. Set explicitLensSelection true only when the user explicitly asks for front, back, rear, or selfie capture. Do not call camera_open_photo after this tool unless the user explicitly asks to view or open the photo. Call this tool directly. Capture uses the native system camera first. Explicit front/back capture may use the direct backend only if the native camera cannot honor the requested lens. Do not ask the user to grant permission before calling this tool. |
| `cross_app_read_screen` | LOW | Auto | Open or switch to a target Android app, wait until that app is stably in the foreground, and return the visible accessibility screen tree. Use this when one request asks to both open/switch to an app and read, inspect, describe, or summarize its visible page. Do not split that task into launch_app and get_screen_info. This tool does not capture a screenshot or interact with nodes. |
| `get_installed_map_apps` | LOW | Auto | Query installed map apps supported by map tools. Returns each app's display name and package name. |
| `get_screen_info` | LOW | Auto | Get UI elements from the currently visible app screen. Each element has a node ID (for example [n3]) that can be used with tap_node. Do not use this to read, list, search, or summarize Android notifications; use sysinfo tools instead. Do not cache this result because node IDs change on each call. |
| `launch_app` | DEVICE_CONTROL | Require approval | Open a launchable Android app by package name or installed app display name. This changes the foreground app and may expose that app's screen. Use an exact package name when possible. If the result is ambiguous, ask the user to choose one returned package. |
| `play_store_check_app` | LOW | Auto | Check whether an exact Android package name is listed on Google Play by requesting https://play.google.com/store/apps/details?id=<packageName>. This is a read-only network check. Use it before play_store_install. |
| `play_store_install` | EXTERNAL_EFFECT | Require approval | Open a specific Google Play app details page by exact package name, tap Install, and wait for installation to complete. This tool does not open the installed app. Use only when an exact Android packageName is known. Never use this for fuzzy search results or to guess the first app. If packageName is unknown, ask the user for the exact app package first. Stops if Google Play shows paid, purchase, subscription, update, ambiguous UI, or if installation does not complete before timeout. |
| `screenshot_open` | SENSITIVE_READ | Require approval | Open a specific visible screenshot from the system gallery. index is 1-based in the selected order. sortOrder can be newest or oldest. |
| `screenshot_take` | SENSITIVE_READ | Require approval | Capture an Android screen with the FoneClaw accessibility service and save it to Pictures/FoneClaw/Screenshots. Use targetType=current for the current screen, targetType=app with targetApp for opening an app and capturing it, or targetType=notification_shade only for an explicit screenshot or image-capture request involving the visible notification shade. Never use targetType=notification_shade to read, list, query, or summarize notification data; use a SysInfo notification tool instead. Do not call launch_app or system_panel_open before this tool for app or notification-shade capture. Do not use this when the user wants to view an already saved screenshot; use screenshot_open for viewing saved screenshots. |
| `tap_node` | LOW | Auto | Tap a UI element on the screen. The element is identified by its node ID, which can be obtained from the screen_info tool. |

## Device Status and System Control Tools

| Tool | Risk | Approval | What it does |
|---|---|---|---|
| `alarm_modify_alarm` | DEVICE_CONTROL | Require approval | Immediately open the Android Clock alarms page when the user wants to modify, edit, adjust, or change an existing alarm. Call this tool for every separate modification request, even if it was already called earlier in the conversation. Do not ask for confirmation. Android does not provide a reliable public API for editing existing alarms, so this tool only navigates to the Clock app for manual editing. |
| `alarm_set_alarm` | DEVICE_CONTROL | Require approval | Set an Android Clock alarm. Collect missing time/repeat/label details before calling. |
| `bluetooth_connected_devices` | SENSITIVE_READ | Require approval | Return Bluetooth devices currently connected through public Android Bluetooth profiles visible to normal apps. |
| `bluetooth_nearby_devices` | SENSITIVE_READ | Require approval | Scan and return nearby Bluetooth devices. Includes BLE scan results and classic Bluetooth discovery results when permissions and Bluetooth state allow it. Devices without a readable name are hidden by default. |
| `bluetooth_pair_device` | DEVICE_CONTROL | Require approval | Start pairing with a Bluetooth device by MAC address. This launches Android's normal bonding flow and may require user confirmation or PIN entry. |
| `bluetooth_paired_devices` | SENSITIVE_READ | Require approval | Return Bluetooth devices already paired with this Android device. |
| `bluetooth_state` | LOW | Auto | Return the current Bluetooth adapter state: on, off, turning_on, turning_off, or unknown. |
| `close_bluetooth` | DEVICE_CONTROL | Require approval | Turn Bluetooth off using the public legacy disable API. If Android refuses, report that normal Android apps cannot turn Bluetooth off on this device. |
| `device_app_activity_snapshot` | SENSITIVE_READ | Require approval | Return a compact recent app activity snapshot from Android UsageStats. Requires usage access; it is not a live background-process monitor. |
| `device_app_permission_audit` | SENSITIVE_READ | Require approval | Return a compact permission-risk audit for installed apps visible to this app. Risk is heuristic and based on declared/granted sensitive permission groups. |
| `device_app_sensitive_audit` | SENSITIVE_READ | Require approval | Return compact sensitive app states: accessibility, notification listener, device admin, overlay, battery optimization exemption, and granted sensitive permission groups. |
| `device_battery_status` | LOW | Auto | Return compact Android battery and power-save status. No settings are changed. |
| `device_health_check` | LOW | Auto | Return a compact read-only health snapshot: memory, storage, battery, and network. No settings are changed. |
| `device_hidden_app_check` | SENSITIVE_READ | Require approval | Return a compact heuristic check for user apps without launcher entries or disabled components. This can flag suspicious apps, but legitimate service-only apps may appear too. |
| `device_memory_status` | LOW | Auto | Return compact Android memory status. No settings are changed. |
| `device_network_status` | LOW | Auto | Return compact Android active network status and capabilities. No network settings are changed. |
| `device_storage_status` | LOW | Auto | Return compact Android internal/shared storage status. No files are scanned or deleted. |
| `device_time_status` | LOW | Auto | Return the current Android system time zone and Unix timestamp for time-dependent tool calls. Set verifyWithNtp=true only when the system time is disputed or an online reference is required. NTP verification uses ntp1.aliyun.com and never changes the system clock. |
| `dnd_open_policy_access_settings` | DEVICE_CONTROL | Require approval | Open Android settings where the user can grant FoneClaw Do Not Disturb / Notification Policy access. Do not call this immediately after dnd_set_mode reports that it already opened the settings page. |
| `dnd_set_mode` | DEVICE_CONTROL | Require approval | Set Android Do Not Disturb mode. If policy access is missing or Android rejects the write, this opens the system DND policy access/settings page. |
| `dnd_status` | LOW | Auto | Read Android Do Not Disturb policy access state and current interruption filter. |
| `flashlight_set_enabled` | DEVICE_CONTROL | Require approval | Turn the Android flashlight on or off using the system camera torch API. |
| `flashlight_status` | LOW | Auto | Return whether the flashlight is available and the last known torch state. |
| `flashlight_toggle` | DEVICE_CONTROL | Require approval | Toggle the Android flashlight. If the current state is unknown, this turns it on. |
| `foneclaw_permission_status` | LOW | Auto | Check all current permissions and special-access capabilities of FoneClaw itself. Use this when the user asks which FoneClaw permissions are enabled, missing, limited, or available. This is a fresh read-only check; do not use app audit, notification-panel, or screen-reading tools as a substitute. |
| `open_bluetooth` | DEVICE_CONTROL | Require approval | Request Bluetooth to turn on. For legacy target SDKs this first tries the public enable API, then falls back to Android's system confirmation UI when needed. |
| `system_panel_open` | DEVICE_CONTROL | Require approval | Open one visible Android system panel. Use panel=notifications to open the notification shade, or panel=quick_settings to open quick settings / control center. This tool only opens the selected panel. It does not read, capture, search, list, or summarize panel content. Do not call it when any part of the request asks to read, capture, inspect, describe, or summarize notifications. Call it once only for an explicit panel-open-only request. Requires FoneClaw accessibility service. |
| `system_settings_open_battery_optimization_entry` | DEVICE_CONTROL | Require approval | Open Android battery optimization settings. If appNameOrPackage is provided, open that app's detail page by package name or installed app label. If it cannot be resolved or opened, return failure and do not open another settings page. If empty, open the system battery optimization list/settings page. |
| `system_settings_open_battery_saver_settings` | DEVICE_CONTROL | Require approval | Open Android Battery Saver / power saving / low power mode settings. Normal apps cannot directly toggle Battery Saver through public APIs, so after settings opens the user must enable or disable it manually. Vendor ROMs may fall back to generic battery settings. |
| `system_settings_open_date_time_settings` | DEVICE_CONTROL | Require approval | Open the Android Date & time settings page directly. Use this for date, time, automatic time, time zone, or automatic time-zone settings. This launches a system settings Intent and does not require accessibility, screen reading, or tapping. After it succeeds, do not call launch_app, get_screen_info, or tap_node. |
| `system_settings_open_hotspot_entry` | DEVICE_CONTROL | Require approval | Open Android hotspot settings. Normal apps cannot reliably toggle hotspot silently through public Android APIs. If the exact hotspot settings page cannot be opened, this returns failure and does not open another settings page. |
| `system_settings_open_location_entry` | DEVICE_CONTROL | Require approval | Open Android location settings. If appNameOrPackage is provided, open that app's detail page by package name or installed app label. If it cannot be resolved or opened, return failure and do not open another settings page. If empty, open the system Location settings page. |
| `system_settings_set_auto_rotate` | DEVICE_CONTROL | Require approval | Enable or disable Android auto rotation. Requires the special modify-system-settings permission. |
| `system_settings_set_brightness` | DEVICE_CONTROL | Require approval | Set Android screen brightness. Manual mode writes percent from 1 to 100; auto mode only enables automatic brightness and ignores percent. Requires the special modify-system-settings permission. |
| `system_settings_set_eye_comfort` | DEVICE_CONTROL | Require approval | Open Android Display settings for Eye comfort/Night Light/blue light filter/reading mode. Use this tool instead of only giving manual instructions for multilingual eye-comfort intents. Android vendors expose the exact switch differently, so this tool opens Display settings and lets the user finish the switch on that page. |
| `system_settings_set_font_scale` | DEVICE_CONTROL | Require approval | Set Android system font scale. Requires the special modify-system-settings permission. Typical values are 0.85 small, 1.0 default, 1.15 large, 1.3 larger, and up to 1.6 extra large. |
| `system_settings_set_haptic_feedback` | DEVICE_CONTROL | Require approval | Enable or disable Android haptic feedback. Requires the special modify-system-settings permission. |
| `system_settings_set_screen_timeout` | DEVICE_CONTROL | Require approval | Set Android screen-off timeout in seconds. Requires the special modify-system-settings permission. Values are clamped to 15..1800 seconds. |
| `system_settings_set_touch_sounds` | DEVICE_CONTROL | Require approval | Enable or disable Android touch sounds. Requires the special modify-system-settings permission. |
| `system_settings_set_user_rotation` | DEVICE_CONTROL | Require approval | Lock Android screen rotation. Requires the special modify-system-settings permission and disables auto rotation. Use portrait, landscape, reverse_portrait, or reverse_landscape. |
| `system_settings_status` | LOW | Auto | Return writable system settings permission state plus current screen brightness, font scale, screen timeout, rotation, touch sounds, and haptic feedback. This reads Android system settings only. |
| `volume_adjust_stream` | DEVICE_CONTROL | Require approval | Raise or lower one Android audio stream by one system step. Choose this for relative volume changes. |
| `volume_set_all_streams_muted` | DEVICE_CONTROL | Require approval | Mute or unmute all supported Android audio streams and report which streams actually changed. Choose this for whole-device or all-volume mute requests. |
| `volume_set_ringer_mode` | DEVICE_CONTROL | Require approval | Set Android phone ringer mode. For silent mode, this automatically falls back to muting all audio streams when Android rejects the ringer-mode write. |
| `volume_set_stream` | DEVICE_CONTROL | Require approval | Set one Android audio stream to a target percentage. Choose this when the user asks for an exact volume level. |
| `volume_set_stream_muted` | DEVICE_CONTROL | Require approval | Mute or unmute one Android audio stream. This changes a stream mute state, not the phone ringer mode. |
| `volume_status` | LOW | Auto | Read the current Android volume state for audio streams and the current ringer mode. |
| `wifi_configured_networks` | SENSITIVE_READ | Require approval | 读取 Android 已配置/已保存的 Wi-Fi 网络列表。只返回 SSID、networkId、安全类型、是否像是有凭据、是否隐藏网络、是否当前连接和配置状态；不会返回 Wi-Fi 密码。 |
| `wifi_connect` | DEVICE_CONTROL | Require approval | Connect to a Wi-Fi network. The tool first checks Android's saved Wi-Fi configurations for the SSID and connects directly if it exists. If it is not saved, it uses the provided passphrase to add/connect the network, or falls back to Android's Wi-Fi panel when password input is needed. |
| `wifi_disconnect` | DEVICE_CONTROL | Require approval | Disconnect from the current Wi-Fi access point. The tool first attempts a silent legacy WifiManager disconnect for target API 28 hosts; if Android rejects it, it falls back to opening Android's Wi-Fi panel for user action. |
| `wifi_forget` | DESTRUCTIVE | Require approval | Forget a saved Wi-Fi network. The tool first attempts silent legacy WifiManager removeNetwork, then falls back to Android's Wi-Fi panel for user action. |
| `wifi_scan_networks` | SENSITIVE_READ | Require approval | Scan and return nearby Wi-Fi access points. If Wi-Fi is off, this tool first attempts to enable Wi-Fi directly and only falls back to Android's Wi-Fi panel if direct enable fails. Requires location permissions and device location services. Results can reveal location-sensitive network data. |
| `wifi_set_enabled` | DEVICE_CONTROL | Require approval | Enable or disable Android Wi-Fi. The tool first attempts the direct WifiManager operation, then checks whether Android actually reached the requested state; if the request fails or remains pending, it falls back to opening Android's Wi-Fi panel for user action. |
| `wifi_status` | SENSITIVE_READ | Require approval | Return current Android Wi-Fi status and active Wi-Fi connection details when the app has permission. This reads network state only. |

## Location and Navigation Tools

| Tool | Risk | Approval | What it does |
|---|---|---|---|
| `get_location` | SENSITIVE_READ | Require approval | Get the current Android device location while FoneClaw is in use. Requires foreground location permission only. |
| `location_search_nearby_place` | SENSITIVE_READ | Require approval | Search nearby place candidates for an ambiguous place name using the current device location. Use this only when the user explicitly asks to find, verify, resolve, choose, or navigate to a place, or when a navigation destination is ambiguous. For calendar creation, a user-provided place name is sufficient: preserve it as locationName and do not call this tool solely because an exact address or coordinates are missing. |
| `map_navigate` | EXTERNAL_EFFECT | Require approval | Open a route or navigation screen in a map app for a destination and travel mode. This launches an external map app, uses only the provided arguments, and does not read device data. |

## Mail Tools

| Tool | Risk | Approval | What it does |
|---|---|---|---|
| `mail_account_list` | LOW | Auto | List configured mail accounts. Leave query empty to list all accounts. When query is provided, matching priority is account id, email, alias, then fuzzy alias or email. Exact matches can still return multiple accounts. If no FoneClaw mail account is configured and query is an email address, the result may include providerSetupGuide with a fixed provider credential URL. |
| `mail_account_save` | DEVICE_CONTROL | Require approval | Test both IMAP and SMTP connections and save the mail account only when both succeed. Call this as soon as every required configuration field is available. This operation requires user approval. |
| `mail_delete` | DESTRUCTIVE | Require approval | Move up to 20 exact messageRefs to trash, or permanently delete them only when the user explicitly requested permanent deletion. Never retry an uncertain result. |
| `mail_list` | LOW | Auto | List, filter, or search message summaries. Query searches subject, sender, recipients, and body. Call this Tool again for every new list, search, filter, date-range, summary, or reformat request even when conversation history contains earlier mail results. Historical messageRefs may be reused only for an operation on an exact already-selected message. Leave accountId empty to use the only account or the unique default account. |
| `mail_read` | SENSITIVE_READ | Require approval | Read one message from a messageRef returned by mail_list and mark it read on the server. After the tool succeeds, summarize the message for the user in the same response. |
| `mail_send` | EXTERNAL_EFFECT | Require approval | Send a new email, reply, reply all, or forward. Requires approval. Attachments must use the exact structured metadata announced with the current user message. |

## Communication and Contacts Tools

| Tool | Risk | Approval | What it does |
|---|---|---|---|
| `call_log_list` | SENSITIVE_READ | Require approval | List recent Android call log entries, optionally filtered by call type. Requires READ_CALL_LOG permission. Use type=missed for missed-call questions and before selecting a missed call to call back. After selecting a number, call phone_dial. |
| `contacts_list` | SENSITIVE_READ | Require approval | List a limited number of Android contacts, optionally filtered by display name or phone number. Requires READ_CONTACTS permission. Leave query empty only when the user explicitly asks to view contacts; set query when the user asks to find a contact. |
| `phone_dial` | EXTERNAL_EFFECT | Require approval | Open the system dialer for exactly one phone number or named contact. Set exactly one of phoneNumber and contactName. A named contact must resolve to one match; if multiple contacts match, stop and ask the user to choose. After the dialer opens, call get_screen_info, identify the visible call button, then call tap_node. |
| `send_to_sms_message` | EXTERNAL_EFFECT | Require approval | After approval, open a visible SMS/MMS draft. For a text SMS with a recipient, automatically tap Send only when FoneClaw accessibility is connected and the default message app, recipient, full message body, and one stable Send control are all verified. Attachments, dual-SIM prompts, ambiguous UI, or unavailable accessibility require manual action. After any terminal result, stop tool planning and never call get_screen_info or tap_node. |
| `sms_list` | SENSITIVE_READ | Require approval | Query live SMS messages on this Android device. With no keyword, address, contact, or threadId, list recent messages. Set keyword/address/contact to search. Set threadId only from an earlier sms_list result to read that conversation. For a summary request, call once with box=all and then summarize the returned received and sent messages. Never use SysInfo history as a replacement for this live result. |

## Calendar Tools

| Tool | Risk | Approval | What it does |
|---|---|---|---|
| `calendar_create_event` | EXTERNAL_EFFECT | Require approval | Create an Android calendar event. Collect missing time and reminder details before calling. A user-provided place name is sufficient and should be passed directly as locationName. Do not search for an exact address or coordinates unless the user explicitly asks to verify, resolve, choose, or navigate to the place. Prefer startLocalDateTime/endLocalDateTime in yyyy-MM-dd HH:mm for user dates and relative dates; the tool will parse them in the device timezone. Do not create events from model-calculated epoch milliseconds. Use calendarId=0 unless the user explicitly selected a calendar id. After creation, use the tool result actualStart/actualEnd as the user-facing created time. |
| `calendar_delete_event` | DESTRUCTIVE | Require approval | Delete an Android calendar event by id. Search/list events across all calendars with calendarId=0 unless the user explicitly selected a calendar id. Always pass the known title, startMillis, endMillis and location when known so the target is unambiguous. |
| `calendar_list_calendars` | SENSITIVE_READ | Require approval | List writable Android calendars. Use this to let the user choose a calendar; do not assume that calendarId=1 is the local or target calendar. |
| `calendar_list_events` | SENSITIVE_READ | Require approval | List Android calendar events in a concrete time range. Use for requests like today's schedule, tomorrow's schedule, existing events, or this week's events. Prefer startLocalDateTime/endLocalDateTime in yyyy-MM-dd HH:mm; the tool will parse them in the device timezone. Use startMillis/endMillis only as a deprecated fallback when exact epoch milliseconds came from a trusted tool result. Do not guess calendarId; use calendarId=0 to search all calendars unless the user explicitly selected a calendar id. |
| `calendar_open_event` | SENSITIVE_READ | Require approval | Open an Android calendar event detail page by event id. Search or list events first when the user describes an event in natural language. Do not guess event id. |
| `calendar_search_events` | SENSITIVE_READ | Require approval | Search Android calendar events by keyword in a concrete time range. Use before opening, updating, or deleting an event when the user describes the event by title or content. Prefer startLocalDateTime/endLocalDateTime in yyyy-MM-dd HH:mm; the tool will parse them in the device timezone. Use startMillis/endMillis only as a deprecated fallback when exact epoch milliseconds came from a trusted tool result. Do not guess calendarId; use calendarId=0 to search all calendars unless the user explicitly selected a calendar id. |
| `calendar_update_event` | EXTERNAL_EFFECT | Require approval | Update an Android calendar event by id. Search/list events first when the user describes the target in natural language, and pass known target details with the changed fields. Do not guess calendarId or event id; search all calendars with calendarId=0 unless the user explicitly selected a calendar id. Blank text fields are ignored. Use startMillis/endMillis=0 to keep existing times. Use reminderMinutes=-2 to keep reminders, -1 to remove reminders, and >=0 to replace reminders. |

## Memo Tools

| Tool | Risk | Approval | What it does |
|---|---|---|---|
| `memo_create` | DEVICE_CONTROL | Require approval | Create a user-visible local memo, note, or task record. Use when the user explicitly wants the content kept as a memo or actionable note. Do not use this tool to save or query assistant-managed user profile memory such as preferences, identity, relationships, or response style. |
| `memo_delete` | DESTRUCTIVE | Require approval | Soft-delete a local memo by id. Search/list memos first when the user describes the target in natural language so the target id/title is known. |
| `memo_list` | SENSITIVE_READ | Require approval | List user-visible local memo records. status can be active, done, archived, or all. Do not use this tool to answer what the assistant remembers about the user, user profile preferences, or conversation history. An empty result means only that no matching local memos exist; it does not mean that no user profile memory exists. |
| `memo_mark_done` | DEVICE_CONTROL | Require approval | Mark a local memo as done or active by id. |
| `memo_search` | SENSITIVE_READ | Require approval | Search user-visible local memos by keyword in title, content, or tags. status can be active, done, archived, or all. Do not use this tool to search assistant-managed user profile memory or conversation history. An empty result describes only the local memo store. |
| `memo_update` | DEVICE_CONTROL | Require approval | Update a local memo by id. Search/list memos first when the user describes the target in natural language, and pass known target details with the changed fields. Blank title/content/tags are ignored. reminderAtMillis=-2 keeps reminder, -1 clears reminder, and >0 sets reminder. |

## System Information Tools

| Tool | Risk | Approval | What it does |
|---|---|---|---|
| `sysinfo_delete` | DESTRUCTIVE | Require approval | Permanently delete matching FoneClaw local SysInfo cache only. This never deletes original Android notifications, SMS, call logs, or calendar events. At least one filter is required unless deleteAll=true. Multiple filters are combined with AND. |
| `sysinfo_mark_reviewed` | DEVICE_CONTROL | Require approval | Mark local SysInfo event ids as reviewed only after the user explicitly confirms. |
| `sysinfo_query` | SENSITIVE_READ | Require approval | Query already-captured local notifications, SMS, call logs, calendar events, and accessibility input. Use this single tool for daily briefs, notification queries, time-window queries, and keyword searches. It never opens a system panel or scans live system sources. Each row may include sourceRef for deduplication against a live provider result. Summarize the returned JSON for the user. |
| `sysinfo_thread` | SENSITIVE_READ | Require approval | Load one local SysInfo thread by a threadKey returned by sysinfo_query. |

## Web and Shopping Tools

| Tool | Risk | Approval | What it does |
|---|---|---|---|
| `shopping_compare` | LOW | Auto | Search public web results for a product on shopping platforms and return comparison evidence. Use this before giving shopping price advice. |
| `web_fetch` | LOW | Auto | Fetch a specific web page by URL and return readable page text. |
| `web_search` | LOW | Auto | Search the web for relevant pages. Returns result titles, URLs, and short snippets. |

## Tasks, Workflows, and Shortcuts Tools

| Tool | Risk | Approval | What it does |
|---|---|---|---|
| `shortcut_save_agent_prompt` | DEVICE_CONTROL | Require approval | Save a user-created shortcut that sends a prompt to the agent. Only call this after the user confirms the title, description, and behavior. |
| `shortcut_save_direct_tool` | DEVICE_CONTROL | Require approval | Save a user-created direct tool shortcut. Only call this when every requested action can be fully mapped to known tool steps with complete parameters. |
| `task_create` | LOW | Auto | Create the complete ordered TODO plan in one call for the current user request. This is a mandatory precondition before any non-task tool when the request requires three or more steps. Pass every bounded user-facing step in items. The plan can be created exactly once and cannot be extended later. Never include task management, cleanup, status-update, or final-response items. |
| `task_list` | LOW | Auto | List internal TODO items for the current user request. By default returns open, in_progress, and blocked items. |
| `task_update` | LOW | Auto | Update one internal TODO item. Allowed transitions: open to in_progress, blocked, or abandoned; in_progress to blocked, done, or abandoned; blocked to open or abandoned. Done and abandoned are terminal. Only one task may be in_progress. |
| `workflow_delete` | DESTRUCTIVE | Require approval | Delete a saved workflow by id. |
| `workflow_list` | SENSITIVE_READ | Require approval | List saved workflows. |
| `workflow_save` | DEVICE_CONTROL | Require approval | Save a reusable workflow. stepsJson must be a JSON array of {toolName, arguments}. |

## Skills and Plugins Tools

| Tool | Risk | Approval | What it does |
|---|---|---|---|
| `add_skill` | DEVICE_CONTROL | Require approval | Add a new skill document to the skill registry. |
| `disable_skill` | DEVICE_CONTROL | Require approval | Disable a user Skill so it is hidden from the agent prompt. |
| `enable_skill` | DEVICE_CONTROL | Require approval | Enable a valid user Skill so it can be used by the agent. |
| `list_skills` | SENSITIVE_READ | Require approval | List user and system Skills with source, enabled state, and validation state. |
| `load_skill_detail` | SENSITIVE_READ | Require approval | Load a specialized skill when the task at hand matches one of the skills listed in the system prompt. Use this tool to inject the skill's instructions and resources into current conversation. The output may contain detailed workflow guidance as well as references to scripts, files, etc in the same directory as the skill. The skill name must match one of the skills listed in your system prompt. |
| `plugin_request_install` | EXTERNAL_EFFECT | Require approval | Create a structured install proposal for one official plugin. This does not download or install the plugin; HomeScreen must ask the user. |
| `plugin_search` | LOW | Auto | Search official FoneClaw plugin metadata when a user asks for a capability that is not available in the current tool list. This only searches metadata and never downloads or installs a plugin. |
| `preview_skill_import` | LOW | Auto | Preview a pasted SKILL.md document. This parses and validates but does not save. |
| `remove_skill` | DESTRUCTIVE | Require approval | Remove the skill |
| `save_skill_draft` | DEVICE_CONTROL | Require approval | Save a SKILL.md document as a disabled user Skill draft. |
| `update_skill` | DEVICE_CONTROL | Require approval | Update an existing skill document in the skill registry. |

## Skill Authoring Checklist

- Every referenced tool appears in this catalog.
- Every destructive or external-effect workflow includes explicit confirmation language.
- Read tools that expose private data have a clear reason and limited scope.
- The Skill describes what to do when permissions are missing.
- The Skill describes what to do when a tool returns an empty or ambiguous result.
- The Skill does not include credentials, access tokens, passwords, personal data, or private examples.

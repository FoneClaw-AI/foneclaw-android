# Built-in Tool Catalog

This catalog is the public reference for writing FoneClaw Skills and Workflow templates.
Skills do not add new device capabilities. They teach the agent how to combine the
built-in tools below.

For safety behavior, see [Tool Policy System](overview.md).

## How to Use This Catalog

When writing a Skill:

1. Pick the smallest tool set that matches the user's goal.
2. List every tool you rely on in the Skill's `Available Tools` section.
3. Respect each tool's risk level and approval behavior.
4. Add failure paths for missing permissions, empty results, unavailable apps, and denied approvals.
5. Do not invent tool names. Use the exact `tool_name` values in this document.

## Risk and Approval Quick Reference

| Risk | Approval | Meaning |
|------|----------|---------|
| LOW | Auto | Read-only, no sensitive data, no side effects. |
| SENSITIVE_READ | Require approval | Reads private or user-specific data. |
| DEVICE_CONTROL | Require approval | Changes local device state or opens device UI. |
| EXTERNAL_EFFECT | Require approval | Affects external systems or the real world. |
| DESTRUCTIVE | Require approval | Deletes or permanently removes user data. |

## Screen and App Tools

| Tool | Risk | Approval | Use in Skills |
|------|------|----------|---------------|
| `get_screen_info` | SENSITIVE_READ | Require approval | Read the current UI tree before screen-aware guidance or tap workflows. |
| `tap_node` | DEVICE_CONTROL | Require approval | Tap a node returned by `get_screen_info`; include a fallback when the target node is missing. |
| `launch_app` | DEVICE_CONTROL | Require approval | Open an installed app by package name or display name. |
| `play_store_check_app` | LOW | Auto | Check Play Store availability before install flows. |
| `play_store_install` | EXTERNAL_EFFECT | Require approval | Open/install an app through Play Store after the user intent is clear. |
| `get_installed_map_apps` | LOW | Auto | Detect available map apps before navigation workflows. |

## Device Status and Audit Tools

| Tool | Risk | Approval | Use in Skills |
|------|------|----------|---------------|
| `device_health_check` | LOW | Auto | Get a compact device health snapshot. |
| `device_memory_status` | LOW | Auto | Read memory status. |
| `device_storage_status` | LOW | Auto | Read internal/shared storage status. |
| `device_battery_status` | LOW | Auto | Read battery and power-save status. |
| `device_network_status` | LOW | Auto | Read active network status and capabilities. |
| `device_app_permission_audit` | SENSITIVE_READ | Require approval | Review app permissions; avoid exposing sensitive app details unnecessarily. |
| `device_app_sensitive_audit` | SENSITIVE_READ | Require approval | Find apps with sensitive permission combinations. |
| `device_app_activity_snapshot` | SENSITIVE_READ | Require approval | Review recent app activity within a lookback window. |
| `device_hidden_app_check` | SENSITIVE_READ | Require approval | Check for suspicious or hidden app indicators. |

## Wi-Fi Tools

| Tool | Risk | Approval | Use in Skills |
|------|------|----------|---------------|
| `wifi_status` | SENSITIVE_READ | Require approval | Check Wi-Fi power and current connection first. |
| `wifi_set_enabled` | DEVICE_CONTROL | Require approval | Turn Wi-Fi on or off. |
| `wifi_scan_networks` | SENSITIVE_READ | Require approval | Scan nearby networks; mention permission/location limitations. |
| `wifi_configured_networks` | SENSITIVE_READ | Require approval | List saved networks. |
| `wifi_connect` | DEVICE_CONTROL | Require approval | Connect to a network; never ask the LLM to expose passwords. |
| `wifi_disconnect` | DEVICE_CONTROL | Require approval | Disconnect current Wi-Fi. |
| `wifi_forget` | DESTRUCTIVE | Require approval | Forget a saved network only after the target SSID is confirmed. |

## Bluetooth Tools

| Tool | Risk | Approval | Use in Skills |
|------|------|----------|---------------|
| `bluetooth_state` | LOW | Auto | Check adapter state before pairing or device queries. |
| `bluetooth_nearby_devices` | SENSITIVE_READ | Require approval | Scan nearby devices; handle permission and scan timeout failures. |
| `bluetooth_paired_devices` | SENSITIVE_READ | Require approval | List paired devices. |
| `bluetooth_connected_devices` | SENSITIVE_READ | Require approval | List currently connected devices. |
| `bluetooth_pair_device` | DEVICE_CONTROL | Require approval | Pair a device after the user confirms the target. |
| `open_bluetooth` | DEVICE_CONTROL | Require approval | Request enabling Bluetooth. |
| `close_bluetooth` | DEVICE_CONTROL | Require approval | Disable Bluetooth. |

## System Control Tools

| Tool | Risk | Approval | Use in Skills |
|------|------|----------|---------------|
| `volume_status` | LOW | Auto | Read current stream volume and ringer mode. |
| `volume_set_stream` | DEVICE_CONTROL | Require approval | Set a specific stream volume. |
| `volume_adjust_stream` | DEVICE_CONTROL | Require approval | Raise or lower a stream volume. |
| `volume_set_stream_muted` | DEVICE_CONTROL | Require approval | Mute or unmute one stream. |
| `volume_set_all_streams_muted` | DEVICE_CONTROL | Require approval | Mute or unmute all controllable streams. |
| `volume_set_ringer_mode` | DEVICE_CONTROL | Require approval | Change ringer mode. |
| `flashlight_status` | LOW | Auto | Check flashlight availability and known state. |
| `flashlight_set_enabled` | DEVICE_CONTROL | Require approval | Turn flashlight on or off. |
| `flashlight_toggle` | DEVICE_CONTROL | Require approval | Toggle flashlight; prefer explicit on/off when possible. |
| `dnd_status` | LOW | Auto | Read Do Not Disturb state and permission access. |
| `dnd_set_mode` | DEVICE_CONTROL | Require approval | Set DND mode or open settings if access is missing. |
| `dnd_open_policy_access_settings` | DEVICE_CONTROL | Require approval | Open Android DND policy access settings. |
| `alarm_set_alarm` | DEVICE_CONTROL | Require approval | Create an alarm after confirming time, repeat rule, label, and vibration. |
| `alarm_modify_alarm` | DEVICE_CONTROL | Require approval | Open or modify an existing alarm flow after confirming the target. |
| `system_settings_status` | LOW | Auto | Read brightness, font scale, timeout, auto-rotate, and related settings. |
| `system_settings_set_brightness` | DEVICE_CONTROL | Require approval | Change screen brightness. |
| `system_settings_set_font_scale` | DEVICE_CONTROL | Require approval | Change font scale. |
| `system_settings_set_screen_timeout` | DEVICE_CONTROL | Require approval | Change screen timeout. |
| `system_settings_set_auto_rotate` | DEVICE_CONTROL | Require approval | Enable or disable auto-rotate. |
| `system_settings_set_user_rotation` | DEVICE_CONTROL | Require approval | Set user rotation when auto-rotate is disabled. |
| `system_settings_set_touch_sounds` | DEVICE_CONTROL | Require approval | Enable or disable touch sounds. |
| `system_settings_set_haptic_feedback` | DEVICE_CONTROL | Require approval | Enable or disable haptic feedback. |
| `system_settings_open_hotspot_entry` | DEVICE_CONTROL | Require approval | Open hotspot settings when direct control is unavailable. |
| `system_settings_open_location_entry` | DEVICE_CONTROL | Require approval | Open location settings. |
| `system_settings_open_battery_optimization_entry` | DEVICE_CONTROL | Require approval | Open battery optimization settings. |
| `system_settings_open_battery_saver_settings` | DEVICE_CONTROL | Require approval | Open battery saver settings. |
| `system_settings_set_eye_comfort` | DEVICE_CONTROL | Require approval | Toggle eye comfort/night display where supported. |
| `system_panel_open_notifications` | SENSITIVE_READ | Require approval | Open notification shade. |
| `system_panel_open_quick_settings` | DEVICE_CONTROL | Require approval | Open quick settings panel. |

## Location and Map Tools

| Tool | Risk | Approval | Use in Skills |
|------|------|----------|---------------|
| `get_location` | SENSITIVE_READ | Require approval | Get device location; handle missing permission and unavailable providers. |
| `location_search_nearby_place` | SENSITIVE_READ | Require approval | Search nearby places around current location. |
| `map_drive` | EXTERNAL_EFFECT | Require approval | Open driving route/navigation in a map app. |
| `map_walk` | EXTERNAL_EFFECT | Require approval | Open walking route/navigation. |
| `map_bicycle` | EXTERNAL_EFFECT | Require approval | Open bicycling route. |
| `map_electric_bicycle` | EXTERNAL_EFFECT | Require approval | Open electric-bicycle route where supported. |
| `map_public_transport` | EXTERNAL_EFFECT | Require approval | Open public transport route. |

## Mail Tools

| Tool | Risk | Approval | Use in Skills |
|------|------|----------|---------------|
| `mail_account_list` | SENSITIVE_READ | Require approval | List configured mail accounts. |
| `mail_account_test` | SENSITIVE_READ | Require approval | Test mail account connectivity. |
| `mail_account_save` | DESTRUCTIVE | Require approval | Save or update mail account settings; never expose credentials. |
| `mail_account_delete` | DESTRUCTIVE | Require approval | Delete a configured mail account after explicit confirmation. |
| `mail_account_rename` | DEVICE_CONTROL | Require approval | Rename a configured mail account after confirming the target. |
| `mail_list` | SENSITIVE_READ | Require approval | List recent message summaries before reading full bodies. |
| `mail_read` | SENSITIVE_READ | Require approval | Read a message body by ID returned from `mail_list`. |
| `mail_send` | EXTERNAL_EFFECT | Require approval | Send email only after recipient, subject, and body are clear. |
| `mail_mark_read` | DEVICE_CONTROL | Require approval | Mark a message read or unread. |
| `mail_delete` | DESTRUCTIVE | Require approval | Delete a message only after confirming the exact target. |

## Phone, Contacts, and SMS Tools

| Tool | Risk | Approval | Use in Skills |
|------|------|----------|---------------|
| `contacts_search` | SENSITIVE_READ | Require approval | Search contacts before dialing or messaging by name. |
| `contacts_list` | SENSITIVE_READ | Require approval | List contacts with a limit; avoid dumping large contact sets. |
| `phone_dial` | EXTERNAL_EFFECT | Require approval | Open dialer for a phone number. |
| `phone_dial_contact` | EXTERNAL_EFFECT | Require approval | Dial a contact after disambiguation. |
| `call_log_recent` | SENSITIVE_READ | Require approval | Read recent call log entries. |
| `call_log_missed` | SENSITIVE_READ | Require approval | Read missed calls. |
| `send_to_sms_message` | EXTERNAL_EFFECT | Require approval | Open SMS compose with recipient and message. |
| `sms_recent` | SENSITIVE_READ | Require approval | Read recent SMS summaries. |
| `sms_search` | SENSITIVE_READ | Require approval | Search SMS by keyword or sender. |
| `sms_thread` | SENSITIVE_READ | Require approval | Read messages from a specific thread. |
| `sms_summary` | SENSITIVE_READ | Require approval | Summarize SMS within a lookback window. |

## Calendar and Memo Tools

| Tool | Risk | Approval | Use in Skills |
|------|------|----------|---------------|
| `calendar_list_calendars` | SENSITIVE_READ | Require approval | List available calendars. |
| `calendar_create_event` | EXTERNAL_EFFECT | Require approval | Create an event after confirming title, time, and calendar. |
| `calendar_list_events` | SENSITIVE_READ | Require approval | List events in a time window. |
| `calendar_search_events` | SENSITIVE_READ | Require approval | Search events by keyword. |
| `calendar_open_event` | SENSITIVE_READ | Require approval | Open an event in the calendar app. |
| `calendar_update_event` | EXTERNAL_EFFECT | Require approval | Update an event after confirming the target and changes. |
| `calendar_delete_event` | DESTRUCTIVE | Require approval | Delete an event only after explicit confirmation. |
| `memo_create` | DEVICE_CONTROL | Require approval | Create a local memo or reminder. |
| `memo_list` | SENSITIVE_READ | Require approval | List local memos. |
| `memo_search` | SENSITIVE_READ | Require approval | Search local memos. |
| `memo_update` | DEVICE_CONTROL | Require approval | Update memo fields after confirming the target. |
| `memo_mark_done` | DEVICE_CONTROL | Require approval | Mark a memo done or active. |
| `memo_delete` | DESTRUCTIVE | Require approval | Delete a memo only after explicit confirmation. |

## Camera and Screenshot Tools

| Tool | Risk | Approval | Use in Skills |
|------|------|----------|---------------|
| `camera_take_photo` | SENSITIVE_READ | Require approval | Take a photo or open camera flow. |
| `camera_open_latest_photo` | SENSITIVE_READ | Require approval | Open the latest photo. |
| `camera_open_photo` | SENSITIVE_READ | Require approval | Open a specific photo URI/path. |
| `screenshot_take` | SENSITIVE_READ | Require approval | Capture the screen; treat captured content as sensitive. |
| `screenshot_open_latest` | SENSITIVE_READ | Require approval | Open the latest screenshot. |
| `screenshot_open` | SENSITIVE_READ | Require approval | Open a specific screenshot URI/path. |

## Web, Shopping, and System Information Tools

| Tool | Risk | Approval | Use in Skills |
|------|------|----------|---------------|
| `web_search` | LOW | Auto | Search the public web for candidate pages. |
| `web_fetch` | LOW | Auto | Fetch readable text from a specific URL. |
| `shopping_compare` | LOW | Auto | Compare shopping prices from public sources. |
| `sysinfo_brief_sources` | SENSITIVE_READ | Require approval | List sources available for system information briefing. |
| `sysinfo_today_brief` | SENSITIVE_READ | Require approval | Generate today's system information brief. |
| `sysinfo_search` | SENSITIVE_READ | Require approval | Search stored system information. |
| `sysinfo_thread` | SENSITIVE_READ | Require approval | Read a system information thread. |
| `sysinfo_mark_reviewed` | DEVICE_CONTROL | Require approval | Mark system information items as reviewed. |

## Workflow, Shortcut, and Skill Tools

| Tool | Risk | Approval | Use in Skills |
|------|------|----------|---------------|
| `workflow_save` | DEVICE_CONTROL | Require approval | Save a reusable workflow. |
| `workflow_list` | SENSITIVE_READ | Require approval | List saved workflows. |
| `workflow_delete` | DESTRUCTIVE | Require approval | Delete a workflow after explicit confirmation. |
| `shortcut_save_agent_prompt` | DEVICE_CONTROL | Require approval | Save a shortcut that reruns an agent prompt. |
| `shortcut_save_direct_tool` | DEVICE_CONTROL | Require approval | Save a shortcut that invokes direct tool steps. |
| `load_skill_detail` | SENSITIVE_READ | Require approval | Load full Skill content at runtime. |
| `add_skill` | DEVICE_CONTROL | Require approval | Add a user Skill. |
| `update_skill` | DEVICE_CONTROL | Require approval | Update a user Skill. |
| `remove_skill` | DESTRUCTIVE | Require approval | Remove a user Skill after explicit confirmation. |

## Skill Authoring Checklist

Before submitting a Skill, verify:

- Every referenced tool appears in this catalog.
- Every destructive or external-effect workflow includes explicit confirmation language.
- Read tools that expose private data have a clear reason and limited scope.
- The Skill describes what to do when permissions are missing.
- The Skill describes what to do when a tool returns an empty or ambiguous result.
- The Skill does not include credentials, access tokens, passwords, personal data, or private examples.


# Tool Policy System

FoneClaw's safety model is built on a compile-time annotation system that assigns a **risk level** and **approval mode** to every tool. This ensures that no tool executes without the appropriate level of user consent.

## Risk Levels

Every tool is classified into one of five risk levels, ordered from least to most dangerous:

| Level | Description | Examples |
|-------|-------------|----------|
| **LOW** | Read-only, no side effects, no sensitive data | `web_search`, `wifi_status`, `bluetooth_state`, `device_battery_status` |
| **SENSITIVE_READ** | Reads private/user data | `mail_list`, `sms_recent`, `contacts_search`, `get_location`, `screenshot_take` |
| **DEVICE_CONTROL** | Changes device state | `wifi_connect`, `volume_set_stream`, `flashlight_toggle`, `dnd_set_mode` |
| **EXTERNAL_EFFECT** | Affects external systems or the real world | `mail_send`, `phone_dial`, `sms_send`, `calendar_create_event` |
| **DESTRUCTIVE** | Permanently destroys data | `mail_delete`, `wifi_forget`, `workflow_delete`, `calendar_delete_event` |

## Approval Modes

| Mode | Behavior | Eligible Risk Levels |
|------|----------|---------------------|
| **AUTO** | Executes immediately without user confirmation | LOW only |
| **REQUIRE_APPROVAL** | Pauses execution and shows a confirmation card to the user | Any level |
| **DENY** | Tool is blocked entirely | N/A (reserved, not currently used) |

The system enforces a strict rule: **only LOW-risk tools can be set to AUTO approval**. Every other risk level defaults to REQUIRE_APPROVAL. This is validated at compile time — you cannot accidentally ship a tool that auto-approves a destructive action.

## How It Works

### Compile Time

1. Each tool method is annotated with `@BuiltInTool(policy = BuiltInToolPolicy.MAIL_SEND)`
2. The KSP (Kotlin Symbol Processing) processor scans all annotations
3. It validates: no duplicate tool names, no AUTO on non-LOW risk, every policy bound to exactly one function
4. It generates a `GeneratedBuiltInToolIndex` — a compile-time registry of all tools and their policies

### Runtime

1. The LLM decides to call a tool
2. The runtime looks up the tool's policy in the index
3. If `AUTO` → execute immediately
4. If `REQUIRE_APPROVAL` → show approval card with:
   - Tool name
   - Masked/sanitized parameters (sensitive values hidden)
   - Risk level indicator
   - Description of what the tool will do
5. User approves or denies
6. On approve → execute; on deny → report back to the LLM

## Policy Reference by Category

### Always Auto-Approved (LOW risk)

These tools run instantly without user confirmation:

- Device diagnostics: `device_health_check`, `device_battery_status`, `device_memory_status`, `device_storage_status`, `device_network_status`
- Status reads: `wifi_status`, `bluetooth_state`, `flashlight_status`, `volume_status`, `dnd_status`, `system_settings_status`
- Web: `web_search`, `web_fetch`
- Shopping: `shopping_compare`
- Maps: `get_installed_map_apps`
- Play Store: `play_store_check_app`

### Require Approval (SENSITIVE_READ)

These tools read private data and always ask for permission:

- Mail: `mail_list`, `mail_read`, `mail_account_list`, `mail_account_test`
- SMS: `sms_recent`, `sms_search`, `sms_thread`, `sms_summary`
- Phone: `call_log_recent`, `call_log_missed`, `contacts_search`, `contacts_list`
- Screen: `get_screen_info`, `screenshot_take`, `screenshot_open_latest`
- Location: `get_location`, `location_search_nearby_place`
- System info: `sysinfo_today_brief`, `sysinfo_search`, `sysinfo_thread`
- Workflows: `workflow_list`
- Skills: `load_skill_detail`

### Require Approval (DEVICE_CONTROL)

These tools change device state:

- Wi-Fi: `wifi_set_enabled`, `wifi_scan_networks`, `wifi_configured_networks`, `wifi_connect`, `wifi_disconnect`
- Volume: `volume_set_stream`, `volume_adjust`, `volume_mute`, `ringer_mode_set`
- Bluetooth: `bluetooth_nearby_devices`, `bluetooth_paired_devices`, `bluetooth_connected_devices`, `bluetooth_pair_device`, `bluetooth_request_enable`, `bluetooth_disable`
- Flashlight: `flashlight_set_enabled`, `flashlight_toggle`
- System settings: brightness, font scale, screen timeout, auto-rotate, hotspot, etc.
- Camera: `camera_take_photo`, `camera_open_latest`
- Alarm: `alarm_set_alarm`, `alarm_modify_alarm`
- Screen: `tap_node`, `launch_app`, `screenshot_open`
- Play Store: `play_store_install`
- DND: `dnd_set_mode`
- Skills: `add_skill`, `update_skill`

### Require Approval (EXTERNAL_EFFECT)

These tools interact with external systems:

- Mail: `mail_send`
- SMS: `send_to_sms_message`
- Phone: `phone_dial`, `phone_dial_contact`
- Calendar: `calendar_create_event`
- Maps: `map_drive`, `map_walk`, `map_bicycle`, `map_electric_bicycle`, `map_public_transport`
- Memos: `memo_create`

### Require Approval (DESTRUCTIVE)

These tools permanently delete data:

- Mail: `mail_delete`, `mail_account_delete`
- Wi-Fi: `wifi_forget`
- Calendar: `calendar_delete_event`
- Memos: `memo_delete`
- Workflows: `workflow_delete`
- Skills: `remove_skill`

## Design Principles

**Fail Safe** — When in doubt, require approval. The system defaults to the more restrictive mode.

**Transparency** — The approval card shows exactly what will happen. Users see the tool name, sanitized parameters, and risk level before deciding.

**Compile-Time Guarantee** — The annotation processor prevents accidental policy violations before the code even runs. No tool can ship without a defined policy.

**Human Override** — Users can always deny an approval. The agent receives the denial and can offer alternatives or explain why the action was needed.

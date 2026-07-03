---
name: daily-device-brief
description: Prepare a concise daily device and personal planning brief.
version: 1.0.0
---

# Daily Device Brief

## Scope

Use this Skill when the user asks for a quick daily briefing, morning check,
evening recap, or a compact status summary for their FoneClaw or ClawFone.

Do not use this Skill for long research tasks, account setup, or tasks that
require sending messages, changing settings, or deleting data.

## Available Tools

- `device_battery_status`: Check battery level and power-save status.
- `device_network_status`: Check current network transport and connectivity.
- `calendar_list_events`: List upcoming calendar events in a requested time window.
- `memo_list`: List local memos when the user wants reminders included.
- `sysinfo_today_brief`: Summarize today's local system information sources.

## Recommended Workflows

### 1. Morning brief

1. Ask for the time range if the user did not specify today, this morning, or the next 24 hours.
2. Call `device_battery_status` to check battery and power-save state.
3. Call `device_network_status` to check connectivity.
4. Call `calendar_list_events` for the requested time range.
5. If the user asks for reminders or notes, call `memo_list`.
6. Reply with a short brief grouped as device status, schedule, reminders, and next step.

### 2. Low-distraction ClawFone recap

1. Call `sysinfo_today_brief` to gather today's supported local sources.
2. Call `device_battery_status` to decide whether the user should charge soon.
3. Reply in a compact format suitable for a small screen.
4. If permissions or services are missing, explain exactly which source could not be covered.

## Rules

- Keep the response concise and practical.
- Do not expose raw notification, SMS, mail, calendar, or memo content unless the user asks.
- Do not change device settings from this Skill.
- If a read tool requires approval or permission, ask through the normal approval flow.
- If there is no data, say which time range or source was checked.

## Completion Criteria

- The user receives a short status summary.
- The summary names any missing permission, missing service, or unavailable source.
- The final reply includes one clear next step only when it is useful.

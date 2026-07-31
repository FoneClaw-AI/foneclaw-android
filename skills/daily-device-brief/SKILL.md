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
- `sysinfo_query`: Query already-captured local system information by source and time window.

## Recommended Workflows

### 1. Morning brief

1. Treat broad requests to catch up on important, pending, missed, or actionable phone information
   as this workflow even when the user does not say "brief".
2. Resolve the requested time range; use today when no range is given.
3. In one batch, call `sysinfo_query(sourceType=all)`, live `sms_list(box=all)`, and live
   `calendar_list_events(calendarId=0)` for the same range.
4. If SysInfo collection is disabled or partial, continue with the live SMS and calendar results
   and state the missing coverage.
5. Call `device_battery_status`, `device_network_status`, or `memo_list` only when relevant.
6. Deduplicate SMS and calendar items returned by both paths. Prefer the live provider row and
   display/count it once; use source reference/provider id first, then exact source, actor/title,
   occurrence time, and normalized preview.
7. Reply with a short brief grouped by actionability, schedule, updates, and next step.

### 2. Low-distraction ClawFone recap

1. Follow the same SysInfo plus live SMS/calendar workflow as the morning brief.
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

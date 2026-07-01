# Daily Device Brief Skill

Daily Device Brief is a sample community Skill for validating FoneClaw's
self-growing Skill import flow. It teaches the agent how to prepare a compact
daily status summary across device health, connectivity, calendar, memos, and
local system information.

## One-click Install

[Install Daily Device Brief in FoneClaw](https://www.foneclaw.ai/skill/install?url=https%3A%2F%2Fraw.githubusercontent.com%2FFoneClaw-AI%2Ffoneclaw-android%2Fmain%2Fskills%2Fdaily-device-brief%2FSKILL.md)

This HTTPS link is safe to use from GitHub README pages. On Android, it can open
FoneClaw through App Links when the domain is verified; otherwise, the web page
can show an "Open in FoneClaw" fallback button. FoneClaw downloads the hosted
`SKILL.md` and sends it to Home for preview. FoneClaw still shows the Skill id,
execution flow, referenced tools, validation status, and current state before
the user saves or enables it. When testing a local-only branch before the public
raw URL exists, use the manual install path below.

Copy-only legacy shorthand entry:

```text
foneclaw://skill.https://raw.githubusercontent.com/FoneClaw-AI/foneclaw-android/main/skills/daily-device-brief/SKILL.md
```

## Manual Install

1. Open [`SKILL.md`](SKILL.md).
2. Copy the whole file.
3. Paste it into FoneClaw Home.
4. Review the import preview.
5. Save the draft, then enable it after confirming the validation result.

## Expected Preview Flow

When the Skill text is imported, the preview should show a flow similar to:

1. Ask for the briefing time range when it is missing.
2. Call `device_battery_status` to check battery and power-save state.
3. Call `device_network_status` to check connectivity.
4. Call `calendar_list_events` for the requested time window.
5. Optionally call `memo_list` when reminders or notes are requested.
6. Reply with a concise brief and at most one useful next step.

## Test Prompts

- "Give me my morning device brief."
- "Summarize what I should know for the next 24 hours."
- "On my ClawFone, show a compact daily recap."

## Safety Notes

- This Skill only reads status or planning data.
- It does not send messages, delete content, or change system settings.
- Calendar, memo, and system information reads still follow the normal approval
  and permission flow.

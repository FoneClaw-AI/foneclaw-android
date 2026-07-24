---
name: my-skill
description: A one-line summary describing what this skill helps the agent accomplish.
version: 1.0.0
---

# My Skill

## Scope / Applicable Scenarios

Use this skill when the user wants to:

- Scenario A: what triggers it and what the user expects.
- Scenario B: another common trigger.

Do NOT use this skill when:

- The task is outside this skill's domain.
- Another skill is more appropriate.

## Available Tools

- `tool_name_1`: Brief description of what this tool does and its key parameters.
- `tool_name_2`: Brief description.

## Recommended Workflows

### 1. Primary Scenario

1. Call `tool_name_1` with the necessary parameters.
2. Check the result. If successful, proceed to step 3.
3. If the result indicates an error, inform the user and suggest next steps.
4. Call `tool_name_2` to complete the task.

### 2. Alternative Scenario

1. Ask the user for any missing information (e.g., destination, account name).
2. Call `tool_name_1` with the confirmed parameters.
3. Present the result clearly to the user.

## Rules / Agent Behavior Requirements

- Always confirm with the user before performing destructive or high-risk actions.
- Never expose sensitive data (passwords, tokens) in responses or tool parameters.
- If a tool returns missing permission, guide the user to grant the permission first.
- Handle empty results gracefully — explain possible causes rather than stating "nothing found."

## Completion Criteria

The task is complete when:

- The requested information has been presented to the user.
- The requested action has been executed successfully.
- The user has been clearly informed of any required manual steps or limitations.

## Failure Conditions

Stop and notify the user when:

- Required permissions are missing and the user declines to grant them.
- A tool fails after a reasonable retry attempt.
- The user's request is ambiguous and they cannot provide clarification.

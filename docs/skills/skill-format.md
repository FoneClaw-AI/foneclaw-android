# Skill Format Specification

## What is a Skill?

A Skill is a **task knowledge pack** that teaches FoneClaw's agent how to handle a specific domain. It's not code — it's structured Markdown that describes workflows, rules, tool usage patterns, and boundaries for a particular scenario.

Think of it as a recipe book: the agent has the ingredients (tools), and the Skill tells it how to combine them for a specific type of dish (task).

## File Structure

Every skill lives in its own directory under `skills/`:

```
skills/
  my-skill/
    SKILL.md          ← The skill definition (required)
```

The directory name becomes the skill's identifier. It must be a single path segment (no `/`), lowercase with hyphens.

## SKILL.md Format

```markdown
---
name: my-skill
description: A one-line summary of what this skill does.
version: 1.0.0
---

# Skill Title

## Scope / Applicable Scenarios

Describe when this skill should be used and when it should NOT be used.

## Available Tools

List the tools this skill uses, with one-line descriptions:
- `tool_name`: What this tool does.

## Recommended Workflows

### 1. Scenario Name

Step-by-step procedure:
1. First step (call `tool_name` with these parameters)
2. Second step
3. Decision point (if X, do Y; else do Z)

### 2. Another Scenario

...

## Rules / Agent Behavior Requirements

- Rule 1: What the agent should always do
- Rule 2: What the agent should never do
- Rule 3: Edge case handling

## Completion Criteria

When is the task considered done?
- Condition 1
- Condition 2
```

## YAML Front Matter Fields

| Field | Required | Description |
|-------|----------|-------------|
| `name` | Recommended | Skill identifier (should match directory name) |
| `description` | Yes | One-line summary shown in the agent's skill list |
| `version` | No | Semantic version (defaults to `1.0.0`) |

> **Note:** The skill identity is derived from the **directory name**, not the `name` field in YAML. Keep them consistent.

## Body Structure

The body is free-form Markdown. There's no rigid schema, but well-structured skills consistently include:

1. **Scope** — When to use and when NOT to use this skill
2. **Available Tools** — List of tool names with descriptions
3. **Recommended Workflows** — Step-by-step procedures for common scenarios
4. **Rules / Constraints** — What the agent should and shouldn't do
5. **Completion Criteria** — How to know the task is done
6. **Failure Conditions** — When to abort and notify the user

Use the [Built-in Tool Catalog](../tool-policy/tool-catalog.md) when filling the
**Available Tools** section. It lists exact tool names, risk levels, approval
behavior, and Skill-specific usage notes. Do not invent tool names.

## How Skills Are Loaded

```
SkillRegistry
  ├── AssetSkillProvider    (system skills from APK assets)
  └── FileSkillStore         (user skills from filesystem)
```

**System Skills** — Bundled in the APK, loaded from `assets/skills/`. Read-only.

**User Skills** — Stored in the app's internal directory (`filesDir/skills/`). Can be added, updated, and removed through the agent's skill management tools.

User skills can shadow system skills by ID — if a user skill has the same name as a system skill, the user version takes precedence.

## Importing Skills From Text Or Links

Users can paste a `SKILL.md` document into Home, or open an HTTPS Skill install
link from GitHub, a website, email, or chat:

```text
https://www.foneclaw.ai/skill/install?url=https%3A%2F%2Fexample.com%2Fskill.md
```

FoneClaw also accepts this Android deep link internally:

```text
foneclaw://skill/import?url=https%3A%2F%2Fexample.com%2Fskill.md
```

GitHub strips custom schemes such as `foneclaw://` from rendered Markdown links,
so public README pages should use the HTTPS install page above. The legacy
shorthand below is still accepted by the app, but should be shown as copy-only
text:

```text
foneclaw://skill.https://example.com/skill.md
```

Example one-click community Skill entry:

[Install Daily Device Brief](https://www.foneclaw.ai/skill/install?url=https%3A%2F%2Fraw.githubusercontent.com%2FFoneClaw-AI%2Ffoneclaw-android%2Fmain%2Fskills%2Fdaily-device-brief%2FSKILL.md)

For direct Android App Links, publish the repository's
`.well-known/assetlinks.json` under both `https://www.foneclaw.ai/` and
`https://foneclaw.ai/`. Without domain verification, the HTTPS page should still
offer a browser fallback button that opens the internal `foneclaw://` deep link.

FoneClaw previews the Skill before saving. The preview includes:

- Skill id and description.
- Execution flow.
- Referenced tools.
- Validation result.
- Current state.

Only enabled and valid Skills are injected into the agent prompt.

## How Skills Are Used at Runtime

1. On startup, the agent sees all skill names and descriptions (lightweight, ~100 bytes per skill)
2. When the agent encounters a task, it loads the full skill content via `load_skill_detail`
3. The skill's Markdown body becomes part of the conversation context
4. The agent follows the workflows and rules defined in the skill

This lazy-loading design keeps the system prompt small while making detailed knowledge available on demand.

## Best Practices

**Be Specific** — Don't write "check the state." Write "call `wifi_status` and check if the result is `enabled` or `disabled`."

**Use Real Tool Names** — Reference tools exactly as they appear in the
[Built-in Tool Catalog](../tool-policy/tool-catalog.md). If a tool is not listed
there, assume the public Skill runtime cannot rely on it.

**Define Boundaries** — Clearly state what the skill does NOT cover. This prevents the agent from over-reaching.

**Include Failure Paths** — What should the agent do when a tool fails? What should it tell the user? Don't assume every call succeeds.

**Use Natural Language** — The skill is read by an LLM. Write clear, conversational instructions — not pseudocode.

**Version Your Skills** — Use semantic versioning. Breaking changes (removed tools, changed workflows) should increment the major version.

**Keep It Focused** — One skill = one domain. If your skill covers mail AND calendar, split it into two skills.

## Submitting a Skill

1. Copy the [skill template](../../skills/_template/SKILL.md)
2. Create a directory under `skills/` with your skill name
3. Write your `SKILL.md`
4. Test it locally on your FoneClaw installation
5. Submit a Pull Request with your skill

See [CONTRIBUTING.md](../../CONTRIBUTING.md) for detailed contribution guidelines.

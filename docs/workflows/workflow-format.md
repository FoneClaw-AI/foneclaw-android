# Workflow Format Specification

## What is a Workflow?

A Workflow is a **recorded sequence of tool calls** that can be replayed instantly — without going through the LLM. When you find yourself repeating the same task (like "check my work email" every morning), you can save it as a workflow. Next time, the WorkflowAgent matches your input and executes the steps directly.

Think of it as a keyboard shortcut for multi-step tasks.

## How It Works

```
User Input
    │
    ▼
AgentDispatcher
    │
    ├── WorkflowAgent (fast path)
    │     ├── WorkflowMatcher (score input vs saved phrases)
    │     ├── If matched → execute steps → return result
    │     └── If not matched → fall back ↓
    │
    └── ReasoningAgent (LLM path)
```

The WorkflowAgent uses phrase similarity scoring to decide if a saved workflow matches your input. It's fast and free (no LLM call needed), but falls back to full reasoning when no match is found.

## JSON Format

Workflows are stored as individual JSON files:

```json
{
  "id": "unique-workflow-id",
  "name": "Human-readable name",
  "description": "What this workflow does.",
  "matchPhrases": [
    "check work mail",
    "list work email",
    "show my work inbox"
  ],
  "steps": [
    {
      "toolName": "mail_list",
      "arguments": {
        "accountId": "work",
        "folder": "INBOX",
        "limit": "10"
      }
    },
    {
      "toolName": "mail_read",
      "arguments": {
        "messageId": "${first.id}"
      }
    }
  ],
  "createdAtMillis": 1719000000000,
  "updatedAtMillis": 1719000000000
}
```

## Field Reference

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `id` | string | Yes | Unique identifier (lowercase, hyphens, no spaces) |
| `name` | string | Yes | Display name shown in the workflow list |
| `description` | string | Yes | Short description of what the workflow does |
| `matchPhrases` | string[] | Yes | Phrases that trigger this workflow (normalized: lowercase, whitespace-collapsed) |
| `steps` | Step[] | Yes | Ordered list of tool calls to execute |
| `createdAtMillis` | number | No | Creation timestamp (milliseconds) |
| `updatedAtMillis` | number | No | Last update timestamp (milliseconds) |

### Step Format

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `toolName` | string | Yes | Exact tool name (e.g., `mail_list`, `wifi_status`) |
| `arguments` | Map\<string, string\> | Yes | Tool arguments as key-value pairs. All values are strings. |

## Matching Algorithm

The WorkflowMatcher scores user input against each workflow's `matchPhrases` plus its `name`:

| Score | Condition |
|-------|-----------|
| 100 | Input exactly equals a phrase |
| 80 | Input contains a phrase |
| 60 | A phrase contains the input |
| Variable | Keyword overlap: `(hits × 40) / phraseTokenCount` |

The highest-scoring workflow wins. If two workflows tie for the top score, the match is rejected to avoid ambiguity — the input falls through to the ReasoningAgent.

## Currently Supported Tools in Workflows

The WorkflowStepExecutor currently supports mail-related tools:

- `mail_list`, `mail_read`, `mail_send`, `mail_mark_read`, `mail_delete`
- `mail_account_list`, `mail_account_delete`, `mail_account_rename`

Support for additional tool categories will be added in future releases.

## Creating Workflows

### Through the Agent

Talk to FoneClaw: "Save this as a workflow." The agent will use the `saveWorkflow` tool to create it interactively.

### Manually

1. Create a JSON file matching the format above
2. Place it in FoneClaw's workflow directory (`filesDir/workflows/`)
3. Or submit it as a template in this repository's [`/workflows/examples`](../../workflows/examples/) directory

### Via Template

Copy the [workflow template](../../workflows/_template/workflow.json) and modify it for your use case.

## Best Practices

**Use Natural Phrases** — Write `matchPhrases` the way users actually talk: "check my email," "any new mail," "what's in my inbox."

**Keep Steps Minimal** — Each step is a tool call. Fewer steps mean faster execution and fewer failure points.

**Handle Edge Cases** — If a step might fail (e.g., no messages in inbox), document what the user should expect.

**Use Meaningful IDs** — `check_work_email` is better than `workflow_1`.

## Submitting a Workflow Template

1. Copy the [template](../../workflows/_template/workflow.json)
2. Create a JSON file in [`workflows/examples/`](../../workflows/examples/)
3. Submit a Pull Request

See [CONTRIBUTING.md](../../CONTRIBUTING.md) for detailed guidelines.

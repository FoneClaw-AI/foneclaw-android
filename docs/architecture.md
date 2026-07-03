# System Architecture

This document provides a high-level overview of FoneClaw's architecture for developers and contributors. It describes the main components and how they interact, without exposing internal source code.

## Architecture at a Glance

```
┌──────────────────────────────────────────────────────┐
│                    FoneClaw App                       │
│                                                       │
│  ┌─────────────┐   ┌──────────────┐   ┌───────────┐ │
│  │  Compose UI  │   │  ViewModel   │   │  Room DB  │ │
│  │  Chat Screen │──▶│  State Mgmt  │──▶│  Chat Hx  │ │
│  └─────────────┘   └──────┬───────┘   └───────────┘ │
│                           │                           │
│                    ┌──────▼───────┐                   │
│                    │  ClawRuntime │                   │
│                    │  (Agent API) │                   │
│                    └──────┬───────┘                   │
│                           │                           │
│              ┌────────────▼────────────┐              │
│              │    AgentDispatcher       │              │
│              │  ┌────────┐ ┌────────┐ │              │
│              │  │Workflow│ │Reason- │ │              │
│              │  │ Agent  │ │ing Agt │ │              │
│              │  └────────┘ └───┬────┘ │              │
│              └────────────────┬─┘                   │
│                               │                       │
│              ┌────────────────▼────────────┐         │
│              │      Koog Agent Engine       │         │
│              │  LLM ◀──▶ Tools ◀──▶ Memory  │         │
│              └────────────────┬────────────┘         │
│                               │                       │
│         ┌─────────────────────▼──────────────────┐   │
│         │            Tool System                  │   │
│         │  ┌──────┐ ┌─────┐ ┌──────┐ ┌────────┐│   │
│         │  │ Mail │ │ Wi-Fi│ │Screen│ │ 120+   ││   │
│         │  │      │ │     │ │ /App │ │ Tools   ││   │
│         │  └──────┘ └─────┘ └──────┘ └────────┘│   │
│         │           ToolPolicy + Approval        │   │
│         └─────────────────────┬──────────────────┘   │
│                               │                       │
│         ┌─────────────────────▼──────────────────┐   │
│         │      Accessibility Service              │   │
│         │  Read UI Tree · Tap · Swipe · Gesture   │   │
│         └────────────────────────────────────────┘   │
└──────────────────────────────────────────────────────┘
```

## Core Execution Chain

The main path from user input to task completion:

1. **User Input** → `HomeScreen` captures text or voice-to-text
2. **State Management** → `MainViewModel` manages chat messages, tool approvals, and running state
3. **Agent Entry** → `ClawRuntime.process()` receives the input and session context
4. **Dispatch** → `AgentDispatcher` routes to `WorkflowAgent` (recorded workflows) or `ReasoningAgent` (LLM)
5. **LLM Reasoning** → Koog Agent calls the LLM with system prompt, chat history, and available tools
6. **Tool Decision** → LLM decides whether to answer directly or call a tool
7. **Approval Gate** → If the tool requires approval, `ApprovalNode` pauses and shows a confirmation card
8. **Execution** → `ToolExecutionNode` runs the approved tool via `AndroidToolExecutor`
9. **Result Loop** → LLM checks the tool result and either continues calling tools or produces a final answer
10. **Persistence** → Chat history is saved to Room database via `DatabaseChatHistoryProvider`

## Agent Dispatcher

FoneClaw uses a two-tier dispatch system to optimize for both speed and flexibility:

**WorkflowAgent (Fast Path)** — Matches user input against saved workflows using phrase similarity scoring. If matched, executes the recorded tool steps directly without calling the LLM. This makes common tasks instant and free of model costs.

**ReasoningAgent (Full Path)** — Falls back to LLM-based reasoning when no workflow matches. The LLM has access to all tools, skills, and chat history to handle novel requests.

If a workflow match fails during execution, the system gracefully falls back to the ReasoningAgent.

## Tool System

### Tool Sets

Tools are organized into domain-specific `ToolSet` classes. Each ToolSet groups related capabilities:

- `MailToolSet` — mail_list, mail_read, mail_send, mail_delete, etc.
- `WiFiToolSet` — wifi_status, wifi_scan_networks, wifi_connect, etc.
- `ScreenToolSet` — get_screen_info, tap_node
- `MapToolSet` — map_drive, map_walk, map_public_transport
- ...and 12 more ToolSets covering 120+ tools total

### Tool Registration

Tools use compile-time annotation processing:

1. Each tool method is annotated with `@BuiltInTool(policy = ...)`
2. The `tool-policy-processor` (KSP plugin) scans all annotations at compile time
3. It validates policy definitions and generates a `GeneratedBuiltInToolIndex`
4. At runtime, the policy is looked up by tool name to determine risk level and approval mode

This ensures that every tool has a policy defined at compile time — there are no "unguarded" tools.

### Approval Flow

When the LLM decides to call a tool:

1. Runtime looks up the tool's `BuiltInToolPolicy`
2. If `ApprovalMode.AUTO` (only for LOW risk tools) → execute immediately
3. If `ApprovalMode.REQUIRE_APPROVAL` → pause, show confirmation card with tool name, masked parameters, and potential impact
4. User approves or denies
5. On approve → execute
6. On deny → report back to LLM that the action was declined

## Skill System

Skills are task knowledge packs that extend the agent's domain expertise:

- **Format:** Markdown + YAML front matter (`SKILL.md`)
- **Loading:** System skills from APK assets, user skills from filesystem
- **On-demand:** Only name + description are injected into the system prompt. Full content is loaded via `load_skill_detail` when the agent needs it.
- **Shadowing:** User skills can override system skills by ID

See the [Skill Format Guide](../skills/skill-format.md) for authoring details.

## Accessibility Service Integration

The Accessibility Service is FoneClaw's "hands and eyes" on the device:

- **Read** — UI hierarchy tree, text content, view IDs, interactive windows
- **Act** — Tap, swipe, input text, press keys, execute gestures
- **Scope** — Configured to listen to all window events (required for cross-app automation)

This service must be manually enabled by the user in Android Settings → Accessibility. FoneClaw prompts for this on first launch.

## Memory & Persistence

- **Chat History** — Persisted in Room database, supports session-based conversations
- **Workflows** — JSON files in app-internal storage (`filesDir/workflows/`)
- **User Skills** — Markdown files in app-internal storage (`filesDir/skills/`)
- **Mail Accounts** — Encrypted configuration via AES-256-GCM (ConfigEncryptor)

## Internationalization

FoneClaw supports 14 languages with a tiered coverage model:

- **Core (100%):** English, Simplified Chinese, Traditional Chinese (HK), Traditional Chinese (TW)
- **Extended (growing):** Japanese, Korean, Spanish, Portuguese, French, German, Arabic, Indonesian, Thai, Vietnamese

All user-facing strings are managed through `strings.xml` resource files — no hardcoded text in UI code.

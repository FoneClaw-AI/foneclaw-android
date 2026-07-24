# Product Overview

## What is FoneClaw?

FoneClaw is an AI-powered personal assistant that runs natively on Android devices. Unlike traditional voice assistants that handle simple queries, FoneClaw understands multi-step instructions and executes them directly on your device — reading emails, comparing prices, planning routes, managing connections, and controlling system settings.

## How It Works

FoneClaw combines three core capabilities:

**Natural Language Understanding** — Powered by Large Language Models (DeepSeek, GPT, or any OpenAI-compatible endpoint). You speak or type naturally, and the agent interprets intent, plans steps, and calls the right tools.

**Device Control via Accessibility Service** — FoneClaw reads what's on your screen, taps buttons, fills forms, swipes, and navigates apps — just like a human would, but faster. The Accessibility Service integration enables cross-app automation without requiring root access.

**Tool System with Safety Guarantees** — Every capability is wrapped in a tool with a defined risk level. Read-only operations (checking battery, searching the web) execute automatically. Anything that changes state, sends data, or deletes content requires your explicit approval before proceeding.

## Key Design Principles

**Agent-First, Not App-First** — The primary interface is conversation, not a dashboard. You tell FoneClaw what you want; it figures out which apps and tools to use.

**Transparent Execution** — Every tool call is visible. You see what the agent is doing, what it found, and why it made each decision. No black boxes.

**Safety by Default** — High-risk actions (sending email, deleting messages, dialing numbers) always pause for your confirmation. You're in control.

**Privacy-Aware** — Sensitive data (passwords, API keys, account credentials) is never exposed in tool calls or stored in plaintext. Email credentials are managed through encrypted configuration, not passed through the LLM.

**Extensible Through Skills** — The built-in tools cover common scenarios. When the agent encounters a specialized task, it loads a Skill — a knowledge pack that teaches it the right workflow, rules, and boundaries for that domain.

## Product Form

FoneClaw is a standalone app installable on any Android 9+ device. It works as a full-featured personal assistant that can control your phone through natural language. Manual setup of the Accessibility Service permission is required on first launch.

## What Makes FoneClaw Different?

| Feature | Traditional Assistant | FoneClaw |
|---------|----------------------|----------|
| Interaction model | Fixed voice commands | Natural language conversation |
| Execution scope | Single app actions | Cross-app workflows |
| Tool variety | Limited built-in intents | 120+ tools across 16 categories |
| Safety model | Implicit trust | Risk-graded approval system |
| Extensibility | Closed system | Community Skills & Workflows |
| Screen understanding | None | Full UI tree reading + tap/swipe/gesture execution |
| Offline thinking | N/A | WorkflowAgent executes recorded sequences without LLM calls |

## Current Status

FoneClaw is in active development. The current release (v0.0.2) is an alpha-quality build intended for early testing and feedback. See [Release Notes](../../releases) for details on each version.

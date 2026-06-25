# 🧩 FoneClaw Skills

> **Skills are knowledge packs that teach FoneClaw specialized workflows.** A Skill tells the agent *how* to approach a domain — which tools to use, in what order, with what rules, and where the boundaries are. They're the easiest way to make FoneClaw smarter for your specific needs.

---

## What is a Skill?

A Skill is **not** a new tool. FoneClaw already ships with 120+ built-in tools. A Skill is a **knowledge layer** that teaches the agent the right way to combine those tools for a particular task.

Think of it as teaching FoneClaw a recipe:

| Without a Skill | With a Skill |
|-----------------|--------------|
| *"Check my email"* → reads inbox, dumps everything | *"Check my email"* → triages by priority, summarizes urgent threads, flags action items |
| *"Set up my morning routine"* → needs step-by-step instructions | *"Set up my morning routine"* → knows to check calendar, read overnight email, report weather, all in the right order |

📖 **How Skills work under the hood:** [Skill Format Guide](../docs/skills/skill-format.md) · [Built-in Tool Catalog](../docs/tool-policy/tool-catalog.md)

---

## Available Skills

| Skill | Category | What it does | Status |
|-------|----------|--------------|--------|
| 📧 [`mail`](mail/SKILL.md) | Productivity | Smart email triage, priority filtering, safe sending | ✅ Stable |
| 🗺️ [`navigation`](navigation/SKILL.md) | Navigation | Route planning across drive/walk/transit, nearby search | ✅ Stable |
| 📶 [`wifi`](wifi/SKILL.md) | Connectivity | Wi-Fi scan, connect, network management | ✅ Stable |
| 🔵 [`bluetooth`](bluetooth/SKILL.md) | Connectivity | Bluetooth pairing, device management | ✅ Stable |
| 🛒 [`shopping`](shopping/SKILL.md) | Shopping | Cross-store price comparison | ✅ Stable |
| 🔍 [`webResearch`](webResearch/SKILL.md) | Research | Multi-source web research & synthesis | ✅ Stable |
| 📲 [`openApp`](openApp/SKILL.md) | System | App launch & deep-link routing | ✅ Stable |

**Don't see what you need?** That's where you come in — see [Contributing a Skill](#-contributing-a-skill) below.

---

## 🚀 Quick Start: Use a Skill

Skills are loaded automatically by FoneClaw when a task matches. As a user, you don't need to do anything special — just talk to FoneClaw naturally, and it will invoke the right Skill.

For example, just say:
- *"What's new in my work inbox?"* → triggers the `mail` Skill
- *"Navigate me to the nearest gas station"* → triggers the `navigation` Skill
- *"Compare prices for Sony WH-1000XM5"* → triggers the `shopping` Skill

---

## ✍️ Contributing a Skill

Building a Skill is the **fastest way to contribute** to FoneClaw — no Android code, no build tools, just a Markdown file with structured front matter. If you can write a checklist, you can write a Skill.

### Your First Skill in 5 Minutes

1. **Copy the template**

   ```bash
   cp -r skills/_template skills/your-skill-name
   ```

2. **Edit the front matter** — set `name`, `description`, and `triggers`:

   ```yaml
   ---
   name: your-skill-name
   description: One-line description of what this Skill does
   triggers:
     - "keyword or phrase that activates this Skill"
   ---
   ```

3. **Write the body** — document the workflow, rules, tool usage, and boundaries. Use the [Built-in Tool Catalog](../docs/tool-policy/tool-catalog.md) for exact tool names, risk levels, and approval behavior:

   ```markdown
   # Your Skill Name

   ## Goal
   What this Skill helps the user accomplish.

   ## Workflow
   1. First step (which tool to call)
   2. Second step
   3. ...

   ## Rules
   - Always ask before doing X
   - Never do Y

   ## Failure Cases
   - If Z happens, report back and stop
   ```

4. **Test it** — load your Skill into FoneClaw and try a matching phrase.

5. **Submit** — [open a skill submission issue](../.github/ISSUE_TEMPLATE/skill-submission.md) or send a Pull Request.

📖 **Full format spec:** [Skill Format Guide](../docs/skills/skill-format.md) · [Built-in Tool Catalog](../docs/tool-policy/tool-catalog.md)

### What Makes a Good Skill

| ✅ Do | ❌ Don't |
|-------|---------|
| Document a clear, repeatable workflow | Add new tools (use existing ones) |
| Specify which catalog tools to call and in what order | Write generic advice with no tools |
| Define explicit boundaries and failure cases | Leave safety behavior ambiguous |
| Cover real user scenarios with examples | Duplicate an existing Skill |
| Keep it focused on one domain | Try to do everything in one Skill |

### Skill Safety Guidelines

Because Skills guide how the agent uses tools, they inherit safety responsibilities:

- **Respect the risk levels** — never instruct the agent to skip approval for high-risk tools
- **Define failure modes** — what should happen if a tool fails or data is missing?
- **Protect sensitive data** — don't instruct the agent to log or expose credentials
- **Be explicit about side effects** — if your Skill causes external actions (sending, deleting), make that clear

---

## 📁 Skill Structure

```
skills/
├── README.md             This file
├── _template/            Starter template — copy this to begin
│   └── SKILL.md
├── mail/
│   └── SKILL.md
├── navigation/
│   └── SKILL.md
└── ... (one folder per Skill, each with a SKILL.md)
```

Each Skill lives in its own folder containing a single `SKILL.md` file with YAML front matter + Markdown body.

---

## 💡 Skill Ideas (Community Wishlist)

Looking for inspiration? Here are Skills the community would love:

- 📅 **Calendar concierge** — proactive daily agenda briefing with smart prep
- 🔋 **Battery optimizer** — analyze drain patterns and suggest settings changes
- 📱 **App organizer** — categorize and tidy installed apps
- 🌤️ **Weather-aware planner** — factor weather into commute and outdoor plans
- 💰 **Subscription tracker** — find recurring charges across email

Have an idea? [Open a feature request](../.github/ISSUE_TEMPLATE/feature-request.md) or just build it and submit a PR.

---

## Related

- [Skill Format Guide](../docs/skills/skill-format.md) — Complete specification
- [Built-in Tool Catalog](../docs/tool-policy/tool-catalog.md) — Exact tool names, risks, and Skill usage notes
- [Workflow Templates](../workflows/README.md) — No-code automation recipes
- [Contributing Guide](../CONTRIBUTING.md) — How to contribute to FoneClaw

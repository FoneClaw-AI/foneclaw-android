# Contributing to FoneClaw

Thank you for your interest in contributing to FoneClaw! This repository is the public hub for skills, workflows, product documentation, and build configurations. We welcome community contributions.

## Ways to Contribute

### 1. Submit a Skill

Skills are task knowledge packs that teach FoneClaw how to handle specific domains. If you've built a useful skill for your own use, share it with the community.

**Steps:**
1. Read the [Skill Format Guide](docs/skills/skill-format.md)
2. Copy the [skill template](skills/_template/SKILL.md)
3. Create your skill under `skills/your-skill-name/SKILL.md`
4. Test it on your FoneClaw installation
5. Submit a Pull Request

### 2. Share a Workflow

Workflows are reusable task sequences. If you have a workflow that saves you time, others will benefit too.

**Steps:**
1. Read the [Workflow Format Guide](docs/workflows/workflow-format.md)
2. Copy the [workflow template](workflows/_template/workflow.json)
3. Create your workflow under `workflows/examples/your-workflow.json`
4. Submit a Pull Request

### 3. Improve Documentation

Found a typo, unclear explanation, or missing guide? Documentation improvements are always welcome.

**Steps:**
1. Edit the relevant file in `docs/`
2. Submit a Pull Request with a clear description of what you improved

### 4. Report Issues

Found a bug or have a feature idea? [Open an issue](../../issues/new/choose) with as much detail as possible.

## Pull Request Process

1. **Fork** the repository
2. **Create a branch** from `main`: `git checkout -b feature/your-feature-name`
3. **Make your changes** following the guidelines below
4. **Test** — ensure your skill/workflow works on a real FoneClaw installation
5. **Commit** with clear messages: `git commit -m "Add skill: calendar-event-extractor"`
6. **Push** and open a Pull Request
7. **Respond** to review feedback

### Skill Submission Checklist

- [ ] Skill directory name is lowercase with hyphens (e.g., `my-skill`, not `My Skill`)
- [ ] `SKILL.md` has valid YAML front matter (`name`, `description`, `version`)
- [ ] Description is concise and descriptive
- [ ] Scope section clearly defines when to use and when NOT to use
- [ ] Available tools are listed with descriptions
- [ ] Workflows are step-by-step and reference real tool names
- [ ] Rules and completion criteria are defined
- [ ] No sensitive data (passwords, API keys, personal info)

### Workflow Submission Checklist

- [ ] JSON is valid
- [ ] `id` is unique, lowercase with hyphens
- [ ] `matchPhrases` are natural language (how users actually talk)
- [ ] `steps` reference valid tool names
- [ ] `arguments` match the tool's expected parameters
- [ ] No sensitive data (SSIDs with passwords, account credentials)

## Code of Conduct

Be respectful, constructive, and inclusive. We're building a community for FoneClaw users worldwide.

- Use welcoming and inclusive language
 Respect differing viewpoints and experiences
 Gracefully accept constructive criticism
- Focus on what's best for the community

## Style Guidelines

### Skill Markdown

- Use `##` for main sections (Scope, Tools, Workflows, Rules, etc.)
- Use `###` for sub-sections within Workflows
- Reference tool names in backticks: `` `mail_list` ``
- Reference parameters in backticks: `` `accountId` ``
- Keep paragraphs concise — the LLM reads this, so clarity matters more than verbosity

### Workflow JSON

- Use 2-space indentation
- Use meaningful IDs: `check_work_email`, not `wf_1`
- Sort `matchPhrases` by importance (most likely first)
- All argument values are strings, even for numbers: `"limit": "10"`

## Questions?

- Open a [Discussion](../../discussions) (coming soon)
- Check existing [Issues](../../issues)
- Read the [Product Overview](docs/overview.md)

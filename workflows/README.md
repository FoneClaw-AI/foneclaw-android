# Community Workflows

Workflows are recorded sequences of tool calls that can be replayed instantly — perfect for repetitive tasks. Instead of explaining the same task every time, save it once and trigger it with a natural phrase.

## How to Use

1. Download a workflow JSON file from [`examples/`](examples/)
2. Place it in FoneClaw's workflow directory (`filesDir/workflows/`)
3. Or import it through the agent: "Load this workflow"
4. Trigger it by saying one of the `matchPhrases`

## Example Workflows

| File | Trigger | Description |
|------|---------|-------------|
| [check-work-email.json](examples/check-work-email.json) | "check work mail" | List 10 most recent work emails |
| [home-wifi-connect.json](examples/home-wifi-connect.json) | "connect home wifi" | Connect to home Wi-Fi network |

## Create Your Own

Check out the [Workflow Format Guide](../docs/workflows/workflow-format.md) and the [workflow template](_template/workflow.json) to get started.

Submit your workflow via Pull Request — see [CONTRIBUTING.md](../CONTRIBUTING.md) for details.

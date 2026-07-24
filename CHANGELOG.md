# Changelog

All notable changes to the FoneClaw public repository will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/), and this project adheres to [Semantic Versioning](https://semver.org/).

## [Unreleased]

### Added
- v0.0.9 release notes for notification summaries, permission recovery, Home Markdown output, SMS/model/request handling, and the preview YouTube downloader plugin package in the plugin repository
- Missing public release notes for v0.0.6, v0.0.7, and v0.0.8
- v0.0.5 release notes for cleaner Agent replies, clearer approvals, cross-app screenshots, and feedback submission fixes

## [v0.0.9] - 2026-07-24

### Added
- `sysinfo_notification_query` for recent, yesterday, ordered, and update-history notification queries
- FoneClaw host permission status reporting
- Device time status checks with local time zone and optional NTP drift information
- Home UI Markdown controls for prompt, permission, and device-control actions
- Preview YouTube downloader plugin package in the plugin repository for separate validation

### Changed
- Notification summaries now route through the Agent and SysInfo tools instead of a direct notification handler
- Permission recovery now performs return-to-app status checks before continuing the original task
- SMS context, summaries, Xiaomi empty-list permission guidance, and draft-send handling were refined
- Public relay requests are trimmed more safely when conversation history exceeds request limits
- Online model configuration and connectivity errors are presented more clearly

### Fixed
- Notification listener recovery is safer during connect, reconnect, and active-notification replay
- Tool arguments are validated before execution
- Date and time settings can open without relying on Accessibility
- Home input stays locked while active cards are still running

### Known Issues
- GitHub release publication is still pending for this prepared package
- YouTube downloader plugin uses a public test signing certificate and still needs production signing, host trust configuration, full-format device validation, and license review before production rollout
- Member guest-login and member relay code remains disabled by default

## [v0.0.8] - 2026-07-22

### Added
- Modular privacy-safe Firebase Analytics integration
- Permission recovery flow that opens the relevant settings and can resume the original task
- Model vision capability controls and refreshed temporary image access for attachments

### Changed
- Upgraded Koog to 1.1.1 and Java runtime to 21
- Improved image, attachment, camera, screenshot, and image-summary reliability
- Hardened OpenAI-compatible tool-call argument compatibility

### Fixed
- Repeated send-and-cancel cleanup no longer stalls the next Agent request
- Unreadable local images are not replayed into later model image parameters
- Image and provider errors receive clearer local presentation

### Known Issues
- Released as a GitHub pre-release and not submitted to Google Play
- Broader permission matrix and full Top scenario regression remained later release gates

## [v0.0.7] - 2026-07-17

### Added
- Top scenario acceptance list and structured failure sample library
- Task-list based Agent execution guards for multi-step tasks
- Local L1 personal memory foundation with settings, Room storage, retrieval, migration, and prompt injection
- Smart Memory outbox and receipt handling for recoverable background extraction

### Changed
- Improved Agent error presentation and sanitized display text for Home, copy, share, and TTS
- Improved approval cards and tool execution terminal states
- Improved camera, screenshot, cross-app page reading, calendar, feedback, image summary, and model configuration reliability

### Fixed
- Internal JSON, HTML error pages, provider failures, and protocol fragments are no longer directly exposed to users
- Memory and Agent data are excluded from Android backup and device migration paths

### Known Issues
- L1 memory remained a local foundation; broader long-term memory types were not included
- Full Top scenario and multi-device regression continued into later versions

## [v0.0.6] - 2026-07-09

### Added
- Official Plugin and Extension foundation with marketplace search, install proposals, APK verification, trusted plugin refresh, and dynamic tool registration
- File Manager Plugin example with file CRUD, search, batch rename, delete, and HTTPS download tools
- Agent checkpoint and history compaction for long conversation stability
- Tool Approval Mode with Auto approve, Follow tool policy, and Deny all options

### Changed
- Workflows, shortcuts, notification panel actions, and direct tool paths now honor tool approval decisions more consistently
- Mail account parsing and IMAP/SMTP timeout handling were improved

### Known Issues
- Plugin repository metadata is not a trust root; host-side trusted fingerprints remain required
- Checkpoints reduce long-context pressure but are not user-controlled long-term memory

## [v0.0.5] - TBD

### Added
- Cross-app screenshot flow that opens or switches to a target app before capturing the screen
- Failure samples for JSON exposure and Agent fallback regression tracking

### Changed
- Improved Agent reply cleanup for JSON artifacts, internal placeholders, and reasoning artifacts
- Improved local display handling for screenshot, camera, calendar, memo, mail, SMS, contacts, Wi-Fi, and workflow tool results
- Improved approval cards with clearer titles, action labels, and target details
- Updated onboarding visuals

### Fixed
- Support Center feedback submission configuration for FoneClaw
- Feedback screenshot upload signing and response parsing

## [v0.0.4] - TBD

### Added
- Community skills repository with 8 initial skills: bluetooth, daily-device-brief, mail, navigation, openApp, shopping, webResearch, wifi
- GitHub-safe HTTPS Skill install entry for hosted `SKILL.md` links
- Skill template for community contributions
- Workflow template and examples (check-work-email, home-wifi-connect)
- Product documentation: overview, architecture, core scenarios
- Tool policy system documentation
- Skill and workflow format specifications
- Issue templates: bug report, feature request, skill submission
- Contributing guidelines

## [v0.0.2] - 2026-06-17

### Added
- Voice input with transcription
- Intelligent suggestion chips (3-5 per agent reply)
- Google Play auto-install via Accessibility Service
- Notification summary feature
- Shortcut command system (agent prompts, direct tools, auto-recommendation)
- Homepage UX optimization
- HMAC-SHA256 backend API request signing

### Known Issues
- Limited error recovery without retry
- Mixed-language suggestion chip detection
- Domestic manufacturer compatibility pending

## [v0.0.1-alpha] - 2026-06-10

### Added
- Initial alpha release
- Chat interface with Material 3 design
- Core agent pipeline (ClawRuntime → Koog → LLM → Tools)
- 60+ built-in tools across 8 categories
- Accessibility Service integration for device control
- Room-based chat history persistence
- Multi-language support (4 core locales)
- Mail, SMS, contacts, calendar, navigation, Wi-Fi, Bluetooth tools
- Skill system with Markdown format
- Workflow recording and replay
- Tool policy with risk levels and approval system

# Changelog

All notable changes to the FoneClaw public repository will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/), and this project adheres to [Semantic Versioning](https://semver.org/).

## [Unreleased]

### Added
- Community skills repository with 7 initial skills: bluetooth, mail, navigation, openApp, shopping, webResearch, wifi
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

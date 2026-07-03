# Core Scenarios & Capability Map

> FoneClaw turns natural language into **120+ real actions across 16 categories**. This page covers both the headline scenarios that define the product, and the full capability map so you can see exactly what FoneClaw can do.

---

## Headline Scenarios

These are the primary user journeys that guide FoneClaw's product design, tool development, and Skill creation.

### 1. Email Summary, Reply & Trip Reminders

The user asks FoneClaw to check their inbox. The agent lists recent messages, summarizes them by sender, topic, and urgency, and extracts action items — meetings, deadlines, contacts, trips. It can draft replies, create calendar events, and set reminders.

**Safety:** Outgoing emails always require explicit confirmation before sending.

### 2. Messaging App Unread Summary

The user asks "What did I miss?" FoneClaw reads notifications and aggregates unread messages across messaging apps (LINE, WhatsApp, etc.), producing a structured summary grouped by app, conversation, and contact, with action items highlighted.

**Safety:** This involves sensitive personal data. Explicit user authorization is required. Auto-reply needs double confirmation.

### 3. Price Comparison

The user asks to compare prices for a product. FoneClaw searches across platforms (Shopee, momo, and others), compares price, shipping, coupons, seller reputation, delivery time, and warranty. It recommends the best overall option — not just the cheapest.

**Safety:** Results clearly distinguish organic results from ads/sponsored listings. Real-time prices always need final confirmation on the seller page.

### 4. Web Research

The user asks a question that requires current, external information. FoneClaw searches the web, fetches authoritative sources, cross-checks claims, and returns a conclusion with citations and dates.

**Safety:** Time-sensitive, legal, medical, or financial questions always trigger web search rather than relying on model knowledge.

### 5. Route Planning & Navigation

The user says "Navigate to the airport." FoneClaw detects installed map apps, determines the best travel mode, opens navigation with the correct destination, and monitors until the map enters navigation mode.

**Safety:** Low-distraction interaction. Driving safety reminders supplement but don't replace driver judgment.

### 6. Smart Recording & Summary

The user starts a recording during a meeting. FoneClaw transcribes, summarizes key points, extracts to-dos and timeline, and generates meeting minutes.

**Safety:** Complies with local recording consent laws. Audio and transcript retention policies are configurable.

### 7. Audio Content Assistant

The user asks to play music, a podcast, or control playback. FoneClaw handles playback controls and can summarize podcast episodes, extract chapters, and surface to-do items from audio content.

**Safety:** Respects environment context (meeting, driving, night mode). Low-distraction during driving.

### 8. Quick Device Actions

The user gives short commands: "Volume up," "Turn off Bluetooth," "Open camera." FoneClaw parses natural language into standard device actions and executes immediately for low-risk operations.

**Safety:** Send, delete, pay, and publish actions always require confirmation. All actions are logged with type, target, result, and any failure details.

---

## Capability Map — 16 Categories, 120+ Actions

Every FoneClaw action belongs to one of 16 categories. Below is the full map, with example commands you can try today.

### 📱 Screen & App Control

Control any app on your phone as if you were tapping it yourself.

| What you can do | Example command |
|-----------------|-----------------|
| Read what's on screen | *"What's currently showing on my screen?"* |
| Tap a specific element | *"Tap the Send button"* |
| Launch an app | *"Open Spotify"* |
| See installed map apps | *"Which navigation apps do I have?"* |

### 📶 Connectivity (Wi-Fi & Bluetooth)

Manage your wireless connections hands-free.

| What you can do | Example command |
|-----------------|-----------------|
| Check Wi-Fi status | *"Am I connected to Wi-Fi?"* |
| Connect to a network | *"Connect to the coffee shop Wi-Fi"* |
| Scan nearby networks | *"What Wi-Fi networks are around me?"* |
| Pair a Bluetooth device | *"Pair my Bluetooth headphones"* |
| Check Bluetooth state | *"Is my Bluetooth on?"* |

### 📍 Location & Maps

Find places, plan routes, and get directions.

| What you can do | Example command |
|-----------------|-----------------|
| Get current location | *"Where am I right now?"* |
| Search nearby places | *"Find gas stations nearby"* |
| Plan a driving route | *"Navigate to the airport by car"* |
| Plan transit route | *"How do I get to the museum by transit?"* |

### 📞 Phone & Contacts

Make calls and look up the people you need.

| What you can do | Example command |
|-----------------|-----------------|
| Dial a number | *"Call mom"* |
| Search contacts | *"Find Sarah's number"* |
| List contacts | *"Show my recent contacts"* |
| Check call log | *"Who called me recently?"* |
| View missed calls | *"Did I miss any calls?"* |

### 💬 Messages (SMS)

Read, search, and summarize your messages.

| What you can do | Example command |
|-----------------|-----------------|
| Read recent SMS | *"What texts did I get today?"* |
| Search messages | *"Find the message about the delivery"* |
| Summarize a thread | *"Summarize my chat with the bank"* |
| Send a message | *"Text John that I'll be 10 minutes late"* |

### 📧 Mail

Triage, read, and send email — the smart way.

| What you can do | Example command |
|-----------------|-----------------|
| List inbox | *"What's new in my work inbox?"* |
| Read & summarize | *"Summarize the email from my boss"* |
| Send email | *"Reply to the client with the project update"* |
| Manage accounts | *"Add my Gmail account"* |

### 📅 Calendar

Keep your schedule organized.

| What you can do | Example command |
|-----------------|-----------------|
| Create an event | *"Block 2pm tomorrow for a dentist appointment"* |
| List events | *"What's on my calendar this week?"* |
| Delete an event | *"Cancel my 3pm meeting"* |

### 📝 Memos & Notes

Capture and retrieve your thoughts.

| What you can do | Example command |
|-----------------|-----------------|
| Create a memo | *"Remember to pick up dry cleaning"* |
| List memos | *"What notes do I have?"* |
| Search memos | *"Find my notes about the project plan"* |

### 🔊 Device Settings (Volume, Brightness, Display)

Tweak your phone to fit the moment.

| What you can do | Example command |
|-----------------|-----------------|
| Adjust volume | *"Turn the volume down"* |
| Set brightness | *"Make the screen brighter"* |
| Change font size | *"Increase the text size"* |
| Set screen timeout | *"Keep the screen on longer"* |
| Toggle auto-rotate | *"Turn off auto-rotate"* |
| Toggle hotspot | *"Turn on my hotspot"* |

### 🔦 Hardware Quick Actions

Instant control of phone hardware.

| What you can do | Example command |
|-----------------|-----------------|
| Toggle flashlight | *"Turn on the flashlight"* |
| Take a photo | *"Take a photo"* |
| Take a screenshot | *"Take a screenshot"* |
| Set an alarm | *"Wake me up at 7am"* |
| Toggle Do Not Disturb | *"Turn on Do Not Disturb"* |

### 🔍 System Info & Briefings

Get a smart overview of your day.

| What you can do | Example command |
|-----------------|-----------------|
| Daily brief | *"What's my briefing for today?"* |
| Search system info | *"What notifications did I miss?"* |
| App permission audit | *"Which apps can access my location?"* |
| Sensitive behavior audit | *"Check for suspicious app activity"* |

### 🌐 Web & Research

Search and synthesize information from the web.

| What you can do | Example command |
|-----------------|-----------------|
| Web search | *"What's the latest news on the election?"* |
| Fetch a page | *"Read this article and summarize it"* |
| Compare prices | *"Compare prices for Sony WH-1000XM5"* |

### 📊 Device Health

Monitor your phone's status.

| What you can do | Example command |
|-----------------|-----------------|
| Check battery | *"How's my battery?"* |
| Check storage | *"How much storage do I have left?"* |
| Check memory | *"Is my phone running low on memory?"* |
| Check network | *"Is my connection stable?"* |

### 🎛️ System Panels

Open system panels quickly.

| What you can do | Example command |
|-----------------|-----------------|
| Open notifications | *"Show my notifications"* |
| Open quick settings | *"Open quick settings"* |

### ⚡ Workflows (No-Code Automation)

Record once, replay forever — without LLM calls.

| What you can do | Example command |
|-----------------|-----------------|
| Save a workflow | *"Record my morning routine"* |
| List workflows | *"What workflows do I have?"* |
| Run a workflow | *"Run my morning routine"* |

### 🧩 Skills

Teach FoneClaw new domains.

| What you can do | Example command |
|-----------------|-----------------|
| Load a skill | *"Load the mail skill"* |
| Add a skill | *"Add a new skill for fitness tracking"* |
| Browse skills | *"What skills are available?"* |

---

## Want to See More?

- **Try it yourself:** Download FoneClaw at [foneclaw.ai](https://foneclaw.ai)
- **Extend it:** Browse [Community Skills](../skills/README.md) and [Workflow Templates](../workflows/README.md)
- **Understand the safety model:** Every action above respects the [risk-graded approval system](security.md)

---
name: mail
description: Manage email using configured IMAP and SMTP accounts.
version: 1.0.0
---

# Mail Skill

Use this skill when the user asks to list, read, send, mark, or delete email.

## Tools

- mail_list: list recent messages from a folder. Prefer this before reading a message.
- mail_read: read one message by the id returned from mail_list.
- mail_send: send an email. Confirm recipient, subject, and body before calling it.
- mail_mark_read: mark a message as read or unread.
- mail_delete: delete a message. Use only after explicit user confirmation.

## Rules

- Never ask the user to provide a mail password inside a tool call.
- Mail account credentials are configured externally through MailAccountConfig.
- Use the default account id unless the user explicitly names another configured account.
- Keep message bodies concise when summarizing.
- Do not expose authentication details in responses.
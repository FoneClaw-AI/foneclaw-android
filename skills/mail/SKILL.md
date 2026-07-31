---
name: mail
description: List, read, summarize, send, and delete email using configured accounts.
version: 1.1.0
---

# Mail Skill

Use this skill when the user asks to search, read, summarize, send, reply to, forward, or
delete email.

## Tools

- `mail_account_list`: list accounts, or match by exact account ID, exact full email address,
  exact alias, then fuzzy alias/email.
- `mail_list`: list, filter, and search summaries. Its `query` searches subject, sender,
  recipients, and body. Use its opaque `messageRef` for later operations.
- `mail_read`: read one `messageRef`, return attachment metadata and paged body content, and
  mark the server message as read.
- `mail_send`: send `NEW`, `REPLY`, `REPLY_ALL`, or `FORWARD` mail, optionally with files the
  user selected in the current message.
- `mail_delete`: move up to 20 `messageRef` values to Trash, or permanently delete only when
  the user explicitly requests permanent deletion.

## Rules

- Never ask the user to provide a mail password inside a tool call.
- Mail account credentials are configured externally through MailAccountConfig.
- Leave `accountId` empty only when one account exists or exactly one account is the default.
  If the tool returns `account_selection_required`, call `mail_account_list` and ask the user
  to choose; never select the first account.
- Prefer `mail_list` before `mail_read`, `mail_send` reply modes, or `mail_delete`.
- After every successful or partial `mail_read`, summarize the message in the same response.
  Continue with `nextBodyCursor` only when the remaining body is needed.
- `mail_send` and `mail_delete` require approval. Do not claim an email was sent or deleted
  before the tool result confirms it.
- An `unknown` send/delete result must not be retried automatically.
- Empty subjects are allowed; an empty send body is not.
- Attachments must use the exact `attachmentRef`, file name, MIME type, and size announced for
  the current user message. Maximum: 10 files, 10 MiB each, 18 MiB total. Do not invent a
  local path, content URI, URL, or attachment reference.
- Reply and forward do not automatically include attachments from the source message.
- `TRASH` must not fall back to permanent deletion when no reliable Trash folder exists.
- Keep summaries concise and mention important attachment metadata.
- Do not expose authentication details in responses.

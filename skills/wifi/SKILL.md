---
name: wifi
description: Guide an Android Agent to inspect Wi-Fi status, scan nearby Wi-Fi networks, read configured Wi-Fi entries, connect with saved or provided credentials, disconnect, forget Wi-Fi, and use Wi-Fi settings fallbacks when Android app-layer APIs cannot complete the action.
version: 1.0.0
---

# Wi-Fi Tool Guide

## Scope

Use this skill when the user wants the Agent to inspect, scan, connect, switch, disconnect, or forget Wi-Fi. The Wi-Fi tools operate at the normal Android app layer and prefer legacy `WifiManager` APIs available to targetSdk=28 apps. If Android rejects the action, permissions are missing, the connection cannot be verified, or the user must enter a password, follow the tool result and guide the user accordingly.

Good fits:

- Parse a photo or text showing a store Wi-Fi name and password, then connect automatically.
- Search nearby Wi-Fi networks and help choose a target by signal strength.
- Switch to an already configured Wi-Fi network.
- Disconnect the current Wi-Fi.
- Forget a specified or current Wi-Fi network.

Not good fits:

- Reading the plaintext password of a saved or connected Wi-Fi network.
- Guaranteeing silent connect or forget behavior across all Android versions and vendor ROMs.
- Treating an SSID in `configuredNetworks` as proof that the password is valid. It only means Android has a configuration record.
- Treating `enableNetwork`, `addNetwork`, or `updateNetwork` success as proof that Wi-Fi is connected. Only trust the tool's final verified result.

## Available Tools

- `wifi_status`: Check Wi-Fi power and current connection.
- `wifi_set_enabled`: Open or close Wi-Fi. The tool waits for state verification and opens Wi-Fi settings if needed.
- `wifi_scan_networks`: Scan nearby Wi-Fi networks. The tool ensures Wi-Fi is on before scanning.
- `wifi_configured_networks`: Read Android configured/saved Wi-Fi entries. It returns SSID, security type, whether credentials appear configured, and whether the entry is connected. It never returns passwords.
- `wifi_connect`: Connect to Wi-Fi. It checks whether the phone is already connected, tries saved configuration first, then uses the provided password to add/update and connect, or returns that a password is needed.
- `wifi_disconnect`: Disconnect the current Wi-Fi. Falls back to Wi-Fi settings if Android rejects the request.
- `wifi_forget`: Forget a specified or current Wi-Fi. Falls back to Wi-Fi settings if Android rejects the request.

## Recommended Workflows

### 1. Photo or Text Contains Wi-Fi Name and Password

1. Extract `ssid` and `passphrase` from the photo or text.
2. If the security type is known, pass it as `security`; otherwise use `wpa2`.
3. Call `wifi_connect(ssid, security, passphrase)`.
4. Tell the user Wi-Fi is connected only when the tool reports a verified connection.
5. If the tool says a password is needed or the connection was not verified, ask the user to confirm the password or connect manually.

### 2. Search Nearby Wi-Fi and Try a Publicly Found Password

1. Call `wifi_scan_networks` to get nearby networks.
2. Prefer the SSID explicitly requested by the user. If none is provided, choose candidates by signal strength, name similarity, or user context.
3. If the user asks to search for a public password, use web search for the SSID, store name, or venue name. Explain that public results are not guaranteed to be correct.
4. After finding a candidate password, try `wifi_connect` once. Do not brute-force or repeatedly try passwords.
5. If it fails, explain likely causes: wrong password, wrong security type, weak signal, Android rejection, or vendor ROM limitation.

### 3. Switch to an Existing Wi-Fi

1. Call `wifi_configured_networks` to inspect saved entries.
2. If the user provided an SSID, match that SSID directly. If the user says something vague such as "home Wi-Fi", combine configured entries, scan results, and context to choose the best candidate.
3. Call `wifi_connect(ssid)` for a clear candidate. No password is needed for this attempt.
4. If the tool says a password is needed, Android has no verified saved credential for that SSID. Ask the user for the password or instruct them to connect manually.

### 4. Disconnect Wi-Fi

1. Call `wifi_disconnect`.
2. If the tool says Wi-Fi is disconnected or disconnecting, confirm the action.
3. If Android rejects the request or settings are opened, tell the user to disconnect from the Wi-Fi settings page.

### 5. Forget Wi-Fi

1. If the user specified an SSID, call `wifi_forget(ssid)`.
2. If the user asks to forget the current Wi-Fi but did not provide an SSID, call `wifi_status` first, then call `wifi_forget`.
3. Do not claim that the forgotten Wi-Fi password can be read or displayed.
4. If the tool fails or opens settings, tell the user to forget the network manually in Android Wi-Fi settings.

## Permissions and System Boundaries

- Common requirements include `ACCESS_WIFI_STATE`, `CHANGE_WIFI_STATE`, location permission, and system Location being enabled.
- Nearby Wi-Fi scanning may require location permission and system Location.
- targetSdk=28 can use legacy `WifiManager` APIs, but behavior still depends on the device OS version and vendor ROM.
- On Android 10/API 29 and later, apps targeting API 29+ are restricted from modifying Wi-Fi configuration with legacy APIs.
- Normal apps cannot read saved Wi-Fi plaintext passwords.
- Normal apps cannot reliably know that a password is definitely wrong. They can only infer failure when the phone does not connect to the target SSID after a connect attempt.

## Agent Behavior Rules

- Minimize tool calls. Goal-oriented tools such as scan and connect already handle required Wi-Fi prerequisites.
- Trust the tool's action result. Do not explain intermediate API return values to the user.
- Use action-oriented wording: "Wi-Fi opened", "connected", "password needed", "please confirm manually". Do not expose function-level details such as `setEnabled=false` or `enableNetwork=true`.
- If both SSID and password are already known, do not scan first.
- Use configured Wi-Fi entries only to decide whether a saved-connect attempt is worth trying. They do not prove the password is valid.
- On connection failure, do not assert that the password is wrong. Mention possible causes: wrong password, wrong security type, weak signal, Android rejection, or vendor limitation.
- When the user must enter a password, say that clearly. Do not attempt to read a saved password.

## Completion Criteria

The task is complete when one of the following is true:

- The current Wi-Fi status or connection has been confirmed.
- Nearby Wi-Fi or configured Wi-Fi entries have been shown with their limitations.
- Wi-Fi has been connected, switched, disconnected, or forgotten.
- The user has been clearly told that a password, permission, system confirmation, or manual action is required.

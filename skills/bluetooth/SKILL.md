---
name: bluetooth
description: Guide an Android agent to inspect Bluetooth state, nearby, paired, and publicly visible connected devices, start pairing, and request Bluetooth enable or disable within normal Android app limits.
version: 1.0.0
---

# Bluetooth Tool Guide

## Scope

Use this skill when the user wants the Agent to inspect, scan, pair, or guide enabling/disabling Bluetooth. The Bluetooth tools operate at the normal Android app layer — they are not equivalent to the system Settings app and do not have system-level privileges.

Good fits:

- View nearby Bluetooth devices.
- View already paired Bluetooth devices.
- View connected devices visible through public app profiles.
- Check the current Bluetooth on/off state.
- Initiate the system pairing flow using a Bluetooth MAC address.
- Request to enable Bluetooth.
- Request to disable Bluetooth; under targetSdk 28, prefer the legacy public disable API, and when the system rejects it, clearly tell the user to disable Bluetooth manually.

Not good fits:

- Fully simulate the interaction of the system Bluetooth settings page.
- Guarantee retrieval of all connected Bluetooth devices.
- Bypass system restrictions to silently force-enable or force-disable Bluetooth; when the legacy public toggle API is rejected by the system, the user must handle it manually.
- Directly establish protocol-level connections such as GATT, RFCOMM, HID, or audio.
- Bypass system permissions, system dialogs, or user confirmation.

## Available Tools

- `bluetooth_nearby_devices`: Scan for nearby Bluetooth devices. Default scan duration is 3000 ms; unknown devices without a readable name are hidden by default.
- `bluetooth_state`: Read the Bluetooth adapter state. Returns on, off, turning_on, turning_off, or unknown.
- `bluetooth_paired_devices`: Read the system's list of paired devices.
- `bluetooth_connected_devices`: Read connected devices visible to a normal app through public Bluetooth profile APIs.
- `bluetooth_pair_device`: Initiate the Android system pairing flow using a Bluetooth MAC address.
- `bluetooth_request_enable`: Request to enable Bluetooth. Under targetSdk 28, it tries the legacy public enable API first, then falls back to the system confirmation dialog.
- `bluetooth_disable`: Request to disable Bluetooth. Under targetSdk 28, it tries the legacy public disable API first; on failure it reports that normal apps cannot disable Bluetooth.
- `bluetooth_request_disable`: Compatibility alias for `bluetooth_disable`.

## Permissions and System Boundaries

Android 12 and above typically require:

- `BLUETOOTH_SCAN`: to scan for nearby Bluetooth devices.
- `BLUETOOTH_CONNECT`: to read paired/connected devices, initiate pairing, and request enable/disable.

The current app targets SDK 28, so it can still try legacy public toggle APIs such as `BluetoothAdapter.enable()` / `BluetoothAdapter.disable()`. However, the system ROM, device policy, or a future targetSdk bump may still reject them. When a tool fails, follow the returned information and guide the user to handle it manually.

On Android 11 and below, scanning for nearby Bluetooth devices usually depends on location permissions:

- `ACCESS_FINE_LOCATION` or `ACCESS_COARSE_LOCATION`

If a tool reports a missing permission, the Agent should first guide the user to the permissions page to grant it, then retry the original action. Do not interpret a missing permission as "no devices."

## Recommended Workflows

### 1. Inspect the Bluetooth Environment

When the user asks "What Bluetooth devices are nearby?" or "Help me find my Bluetooth headphones":

1. You may call `bluetooth_state` first to check whether Bluetooth is enabled.
2. Call `bluetooth_nearby_devices`.
3. Use the default 3000 ms scan duration.
4. By default, do not include unknown devices, unless the user explicitly wants troubleshooting, debugging, or cannot find a device.
5. If the result is empty, explain the possible causes: Bluetooth is off, permission is missing, the device is not discoverable/broadcasting, it is too far away, or the scan window was too short.

### 2. View Paired Devices

When the user asks "What devices have I paired?" or "Which Bluetooth devices does this phone remember":

1. Call `bluetooth_paired_devices`.
2. Tell the user that "paired" does not mean "currently connected."
3. If the list is empty, explain that the system has no paired devices, or that permissions are insufficient.

### 3. View Connected Devices

When the user asks "Which Bluetooth devices are connected right now":

1. Call `bluetooth_connected_devices`.
2. Explain that this list only covers public profiles visible to normal apps, such as GATT, A2DP, and HEADSET.
3. If the user sees a device in the system Bluetooth page but the tool does not return it, do not assert that the device is not connected. Explain that it may be an HID connection, a vendor-private profile, a system-internal connection, or a connection Android does not expose to normal apps.

### 4. Pair a Device

When the user asks to pair a Bluetooth device:

1. First confirm Bluetooth is enabled:
   - Prefer calling `bluetooth_state` to check the current on/off state.
   - If the tool returns `Bluetooth is disabled`, call `bluetooth_request_enable` first to ask the user to turn Bluetooth on; continue after the user confirms.
   - If the tool reports a missing permission, guide the user to grant it first, then continue.
2. Call `bluetooth_paired_devices` to check whether the target device is already paired:
   - If the target device is already in the list, tell the user it is already paired. Do not initiate pairing again.
3. If the target device is not paired, call `bluetooth_nearby_devices` to search for nearby devices:
   - Prefer matching by the device name, model, or MAC address provided by the user.
   - If there is no match, explain the possible causes: the device is not in pairing mode, it is too far away, its name is not visible, it is not broadcasting, or the scan window was too short.
   - If there are multiple similar candidates, ask the user to confirm the target device first.
4. Once a clearly matched candidate is found, call `bluetooth_pair_device` with a standard MAC address, for example `01:23:45:67:89:AB`.
5. Tell the user that the system may show a confirmation dialog, a PIN entry prompt, or require confirmation on the device side.

Note: The current Bluetooth tools only support initiating the system pairing flow. They do not provide the ability to actively connect to a Bluetooth device.

### 5. Enable Bluetooth

When the user asks to turn Bluetooth on:

1. Call `bluetooth_request_enable`.
2. If it returns that the enable request was accepted, the system accepted the request; you may call `bluetooth_state` again to confirm the final state.
3. If it returns that the system confirmation dialog is open, prompt the user to confirm in the dialog.
4. If it returns not supported, tell the user that normal apps cannot open that confirmation UI and they need to enable Bluetooth manually in system settings.

Do not fall back to the system settings page on tool failure unless a separate tool explicitly provides an "open Bluetooth settings" capability.

### 6. Disable Bluetooth

When the user asks to turn Bluetooth off:

1. Call `bluetooth_disable`.
2. If it returns that the disable request was accepted, the system accepted the request; you may call `bluetooth_state` again to confirm the final state.
3. If it returns not supported, clearly tell the user that normal Android apps cannot disable Bluetooth and they need to turn it off manually.

Do not claim that Bluetooth has been disabled unless the tool explicitly returns that the disable request was sent or that the system state is confirmed off.

## Handling Unknown Devices

Unknown devices are filtered out by default. Bluetooth broadcasts without a readable name are usually very noisy — they may be low-energy sensors, temporary broadcasts, privacy (random) addresses, nearby phones, wearables, or other unidentifiable devices.

Only include unknowns in these scenarios:

- The user explicitly says "I can't find the device, help me troubleshoot."
- The user knows the target device has no name.
- Bluetooth scan debugging is in progress.
- You need to observe the volume of surrounding BLE broadcasts.

When including unknowns, remind the user: an unknown device is not necessarily a pairable peripheral, and not necessarily someone else's phone.

## Common Returns and Explanations

- `Bluetooth adapter is unavailable`: The current device has no available Bluetooth adapter, or the system service is unavailable.
- `Bluetooth state: on/off/turning_on/turning_off/unknown`: The current Bluetooth adapter state.
- `Bluetooth is disabled`: Bluetooth is not enabled; it needs to be turned on first.
- `missing permission`: Missing Android Bluetooth or location permission; authorization is required first.
- `No named nearby Bluetooth devices found`: No nearby devices with a name were found within the scan window.
- `No paired Bluetooth devices found`: The system has no paired devices.
- `No connected Bluetooth devices found from public Bluetooth profiles`: No connected devices are visible through public app profiles; this does not mean the system Bluetooth page has no connection.
- `Bluetooth enable prompt is not supported`: The current system does not allow a normal app to open the Bluetooth confirmation dialog, and the legacy enable API was not accepted; the user must enable it manually.
- `Bluetooth disable is not supported`: The current system does not allow a normal app to disable Bluetooth, or the legacy disable API was not accepted; the user must disable it manually.

## Agent Behavior Requirements

- Explain the system boundaries first, then give the user the next step. Do not promise capabilities that a normal app cannot deliver.
- When a tool reports a missing permission, prioritize guiding the user to grant it. Do not repeatedly retry the same tool.
- When a scan result is empty, do not immediately conclude that the device does not exist.
- When the connected-device list is empty, do not immediately conclude that there are no Bluetooth connections at all.
- Confirm the target device before pairing to avoid initiating an incorrect pairing against a similarly named device.
- Enabling/disabling Bluetooth must respect system limits; when a tool returns a system confirmation dialog, the user must be prompted to confirm.
- Do not promise or perform "connect to a Bluetooth device"; the current tools have no active-connect capability.

## Completion Criteria

The task is complete when one of the following is true:

- The nearby, paired, or connected device list has been shown to the user, with the result boundaries explained.
- The Bluetooth state has been shown, with an explanation of whether further user action is needed.
- A pairing flow has been initiated, and the user has been told that confirmation may be required.
- The system Bluetooth enable confirmation dialog has been opened, and the user has been prompted to confirm.
- It has been clearly stated that the system does not support the current enable/disable request, with a manual-operation suggestion.
- When a permission is missing, the missing permission and the authorization path have been clearly stated.

---
name: android-emulator-control
description: Build, install, launch, drive, and inspect an Android application on an emulator or connected device. Use when an agent must verify runtime behavior, reproduce a bug, inspect layout or logs, capture evidence, exercise intents or deep links, or control app and device state; confirm before destructive operations such as clearing data or wiping an emulator.
license: Apache-2.0
---

# Android Emulator Control

Operate in a repeatable `act, observe, record` loop. Prefer semantic targets and verifiable state over hardcoded coordinates.

## Discover before acting

Determine from the repository and local tools:

- Android CLI and SDK tool locations and versions;
- Gradle wrapper and relevant build or install tasks;
- application ID, launcher activity, variants, APK outputs, and signing compatibility;
- connected device serials and available AVDs;
- whether the requested action changes or deletes app or device data.

Do not guess package IDs, activity names, variants, output paths, or emulator names. When multiple devices are connected, select one explicitly with the supported Android CLI option or `adb -s <serial>`.

## Prefer Android CLI

When a modern Android CLI is installed, inspect `android --help` and the relevant subcommand help, then use supported commands for project description, emulator management, install, run, layout inspection, screenshots, and Journeys. Record the CLI version because the command surface can change.

If `android` resolves to the deprecated SDK tool or lacks the needed command, use Gradle, `adb`, and `emulator` directly. State which path was used.

Read [references/adb-cookbook.md](references/adb-cookbook.md) only for the fallback operation being performed.

## Build and install current code

1. Run the repository's required focused checks.
2. Build the exact target artifact and confirm the command succeeded.
3. Install that artifact on the selected device.
4. Confirm the installed package update time or another reliable build identifier.
5. Force-stop and relaunch so the process cannot continue running stale code.

Never continue runtime QA after a failed build or install. The old artifact may still be present.

## Interact semantically

Use accessibility text, content descriptions, resource IDs, Android CLI semantic actions, or UI hierarchy bounds. Use `scripts/ui.py` for simple ADB fallback discovery and taps. Use coordinates only when no stable semantic target exists, and document device resolution and orientation.

After every material action, observe at least one relevant signal: screenshot, layout hierarchy, current activity, log output, process state, or visible UI result.

## Capture evidence

Record:

- source revision or working-tree state;
- build and install commands;
- artifact path and package;
- device model, API level, serial, resolution, and orientation;
- actions and expected results;
- screenshots, layouts, logs, and timestamps;
- pass, fail, or blocked result with limitations.

Redact tokens, personal data, account details, private URLs, and other sensitive content before sharing or committing evidence.

## Safety

Ask immediately before clearing app data, uninstalling when data loss matters, wiping an AVD, revoking critical permissions, resetting network state, or making other destructive device changes unless that exact action was already authorized.

Restore permissions, connectivity, animation scales, orientation, locale, temporary intents, and repository hooks changed for testing. Do not leave an exported test activity, debug bypass, or test-only production branch in deliverable code.


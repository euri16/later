# ADB fallback cookbook

Use these commands only after selecting the correct device, package, artifact, and activity.

## Device discovery

```bash
adb devices -l
emulator -list-avds
adb -s <serial> shell getprop ro.build.version.sdk
adb -s <serial> shell wm size
adb -s <serial> shell wm density
```

Boot an AVD without wiping its data:

```bash
emulator -avd <avd-name> -no-snapshot
adb -s <serial> wait-for-device
adb -s <serial> shell getprop sys.boot_completed
```

Do not add `-wipe-data` without explicit authorization.

## Install and lifecycle

```bash
adb -s <serial> install -r <apk-path>
adb -s <serial> shell dumpsys package <application-id>
adb -s <serial> shell am force-stop <application-id>
adb -s <serial> shell monkey -p <application-id> -c android.intent.category.LAUNCHER 1
```

Use `-d` with install only when intentionally allowing a version downgrade.

## Observe

```bash
adb -s <serial> exec-out screencap -p > screenshot.png
adb -s <serial> shell uiautomator dump /sdcard/window_dump.xml
adb -s <serial> shell cat /sdcard/window_dump.xml
adb -s <serial> shell dumpsys activity top
adb -s <serial> shell pidof -s <application-id>
adb -s <serial> logcat --pid=<pid> -d -T 500
```

Prefer a bounded time or PID filter over clearing the device-wide log buffer. Clearing logs removes evidence for other processes and concurrent debugging sessions.

## Input

```bash
adb -s <serial> shell input tap <x> <y>
adb -s <serial> shell input swipe <x1> <y1> <x2> <y2> <duration-ms>
adb -s <serial> shell input text '<escaped-text>'
adb -s <serial> shell input keyevent BACK
```

Prefer semantic lookup through `scripts/ui.py` before raw coordinates.

## Intents and deep links

```bash
adb -s <serial> shell am start -W \
  -a android.intent.action.VIEW \
  -d '<uri>' \
  <application-id>
```

Use only documented or repository-discovered routes. Never invent private deep links or pass secrets on the command line.

## Permissions and connectivity

```bash
adb -s <serial> shell pm grant <application-id> <permission>
adb -s <serial> shell pm revoke <application-id> <permission>
adb -s <serial> shell svc wifi enable
adb -s <serial> shell svc wifi disable
```

Permission revocation and connectivity changes affect test state. Obtain approval when destructive or disruptive, and restore the previous state afterward.

## Screen recording

```bash
adb -s <serial> shell screenrecord /sdcard/recording.mp4
adb -s <serial> pull /sdcard/recording.mp4 ./recording.mp4
adb -s <serial> shell rm /sdcard/recording.mp4
```

Review recordings for personal or confidential information before sharing.

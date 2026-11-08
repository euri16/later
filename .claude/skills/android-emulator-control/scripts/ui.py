#!/usr/bin/env python3
"""Find or tap Android UI Automator nodes using semantic attributes."""

from __future__ import annotations

import argparse
import re
import subprocess
import sys
import xml.etree.ElementTree as ET
from uuid import uuid4


ADB_TIMEOUT_SECONDS = 15


def adb(arguments: list[str], serial: str | None) -> str:
    command = ["adb"]
    if serial:
        command.extend(["-s", serial])
    command.extend(arguments)
    return subprocess.run(
        command,
        check=True,
        capture_output=True,
        text=True,
        timeout=ADB_TIMEOUT_SECONDS,
    ).stdout


def hierarchy(serial: str | None) -> str:
    remote_path = f"/data/local/tmp/agent-window-{uuid4().hex}.xml"
    try:
        adb(["shell", "uiautomator", "dump", remote_path], serial)
        output = adb(["exec-out", "cat", remote_path], serial)
    finally:
        try:
            adb(["shell", "rm", "-f", remote_path], serial)
        except (subprocess.CalledProcessError, subprocess.TimeoutExpired):
            pass
    start = output.find("<?xml")
    end = output.rfind("</hierarchy>")
    if start < 0 or end < 0:
        raise ValueError("ADB returned no UI hierarchy XML")
    return output[start : end + len("</hierarchy>")]


def center(bounds: str) -> tuple[int, int] | None:
    match = re.fullmatch(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]", bounds)
    if not match:
        return None
    left, top, right, bottom = map(int, match.groups())
    return (left + right) // 2, (top + bottom) // 2


def matches(
    xml: str,
    query: str,
    fields: set[str],
    exact: bool,
    clickable_only: bool,
) -> list[dict[str, str | tuple[int, int]]]:
    root = ET.fromstring(xml)
    found: list[dict[str, str | tuple[int, int]]] = []
    for node in root.iter("node"):
        values = {
            "text": node.attrib.get("text", ""),
            "desc": node.attrib.get("content-desc", ""),
            "id": node.attrib.get("resource-id", ""),
        }
        if clickable_only and node.attrib.get("clickable") != "true":
            continue
        normalized_query = query.casefold()
        if exact:
            matched = any(normalized_query == values[field].casefold() for field in fields)
        else:
            matched = any(normalized_query in values[field].casefold() for field in fields)
        if not matched:
            continue
        node_center = center(node.attrib.get("bounds", ""))
        if node_center:
            found.append(
                {
                    **values,
                    "bounds": node.attrib.get("bounds", ""),
                    "center": node_center,
                    "clickable": node.attrib.get("clickable", "false"),
                }
            )
    return found


def describe(index: int, item: dict[str, str | tuple[int, int]]) -> str:
    return (
        f"{index}: center={item['center']} bounds={item['bounds']} "
        f"clickable={item['clickable']} text={item['text']!r} "
        f"desc={item['desc']!r} id={item['id']!r}"
    )


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("action", choices=("dump", "find", "tap"))
    parser.add_argument("query", nargs="?", default="")
    parser.add_argument("--serial")
    parser.add_argument("--index", type=int)
    parser.add_argument("--text", action="store_true")
    parser.add_argument("--desc", action="store_true")
    parser.add_argument("--id", action="store_true")
    parser.add_argument("--exact", action="store_true")
    parser.add_argument("--clickable", action="store_true")
    arguments = parser.parse_args()

    try:
        xml = hierarchy(arguments.serial)
    except (subprocess.CalledProcessError, subprocess.TimeoutExpired, ValueError) as error:
        print(f"Unable to read the UI hierarchy: {error}", file=sys.stderr)
        return 2
    if arguments.action == "dump":
        print(xml)
        return 0

    requested = {name for name in ("text", "desc", "id") if getattr(arguments, name)}
    fields = requested or {"text", "desc", "id"}
    if not arguments.query:
        print("find and tap require a non-empty query", file=sys.stderr)
        return 2
    found = matches(xml, arguments.query, fields, arguments.exact, arguments.clickable)
    if not found:
        print(f"No match for {arguments.query!r} in {sorted(fields)}", file=sys.stderr)
        return 1

    if arguments.action == "find":
        for index, item in enumerate(found):
            print(describe(index, item))
        return 0

    if arguments.index is None and len(found) > 1:
        print("Tap is ambiguous. Re-run with --index after reviewing these matches:", file=sys.stderr)
        for index, item in enumerate(found):
            print(describe(index, item), file=sys.stderr)
        return 2
    selected_index = arguments.index if arguments.index is not None else 0
    if selected_index < 0 or selected_index >= len(found):
        print(f"Index {selected_index} is outside 0..{len(found) - 1}", file=sys.stderr)
        return 1
    selected = found[selected_index]
    print(f"Selected {describe(selected_index, selected)}")
    x, y = selected["center"]
    try:
        adb(["shell", "input", "tap", str(x), str(y)], arguments.serial)
    except (subprocess.CalledProcessError, subprocess.TimeoutExpired) as error:
        print(f"Unable to tap selected node: {error}", file=sys.stderr)
        return 2
    print(f"Tapped ({x}, {y})")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

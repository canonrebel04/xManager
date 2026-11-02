#!/usr/bin/env python3
"""adb-driven emulator click-through for xManager. Saves screenshots + UI dumps to docs/emulator/."""
import os, re, subprocess, sys, time, xml.etree.ElementTree as ET
ADB = "/opt/android-sdk/platform-tools/adb"
PKG = "com.xc3fff0e.xmanager"
OUT = os.path.join(os.path.dirname(__file__), "..", "docs", "emulator")
os.makedirs(OUT, exist_ok=True)

def adb(*a, timeout=120, check=False):
    r = subprocess.run([ADB, *a], capture_output=True, timeout=timeout)
    return r.stdout.decode(errors="replace")

def dump(name=None):
    for _ in range(3):
        adb("shell", "uiautomator", "dump", "/sdcard/ui.xml", timeout=180)
        x = adb("shell", "cat", "/sdcard/ui.xml")
        if x.strip().startswith("<?xml"):
            break
        time.sleep(3)
    if name:
        open(os.path.join(OUT, name + ".xml"), "w").write(x)
    try:
        return ET.fromstring(x)
    except ET.ParseError:
        return None

def center(node):
    a, b, c, d = map(int, re.findall(r"\d+", node.get("bounds")))
    return (a + c) // 2, (b + d) // 2

def dismiss_anr(root=None):
    for _ in range(3):
        root = root if root is not None else dump()
        if root is None:
            return root
        hit = [n for n in root.iter("node") if "isn't responding" in (n.get("text") or "")]
        if not hit:
            return root
        wait = [n for n in root.iter("node") if n.get("text") == "Wait"]
        if wait:
            adb("shell", "input", "tap", *map(str, center(wait[0])))
        time.sleep(4)
        root = None
    return dump()

def find(root, rid=None, text=None, desc=None):
    for n in root.iter("node"):
        if rid and n.get("resource-id") == f"{PKG}:id/{rid}": return n
        if text and n.get("text") == text: return n
        if desc and n.get("content-desc") == desc: return n
    return None

def tap(rid=None, text=None, desc=None, settle=4):
    root = dismiss_anr()
    n = find(root, rid, text, desc) if root is not None else None
    if n is None:
        return False
    adb("shell", "input", "tap", *map(str, center(n)))
    time.sleep(settle)
    return True

def shot(name):
    dismiss_anr()
    data = subprocess.run([ADB, "exec-out", "screencap", "-p"], capture_output=True, timeout=120).stdout
    p = os.path.join(OUT, name + ".png")
    open(p, "wb").write(data)
    return p

def swipe_up(n=1):
    for _ in range(n):
        adb("shell", "input", "swipe", "540", "1700", "540", "600", "400")
        time.sleep(3)

def resumed():
    s = adb("shell", "dumpsys", "activity", "activities")
    m = re.search(r"mResumedActivity: ActivityRecord\{\S+ \S+ (\S+)", s)
    return m.group(1) if m else ""

def text_of(rid):
    root = dismiss_anr()
    n = find(root, rid) if root is not None else None
    return None if n is None else n.get("text")

if __name__ == "__main__":
    print(globals()[sys.argv[1]](*sys.argv[2:]))

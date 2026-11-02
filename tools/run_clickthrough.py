import json, time, emu_clickthrough as e
R = {}
def rec(k, ok, note=""):
    R[k] = {"ok": bool(ok), "note": note}; print(k, ok, note, flush=True)

e.adb("logcat", "-c")
root = e.dump("main_top")
rec("main_resumed", "MainActivity" in e.resumed(), e.resumed())
# main list rows: rows/labels visible
labels = [n.get("text") for n in root.iter("node") if n.get("text")]
rec("main_rows_present", any("STOCK" in t.upper() for t in labels) and any("AMOLED" in t.upper() for t in labels), "; ".join(labels[:25]))
# expand first "Versions" row
vers = [n for n in root.iter("node") if (n.get("text") or "").upper() == "VERSIONS"]
if vers:
    e.adb("shell", "input", "tap", *map(str, e.center(vers[0]))); time.sleep(5)
    e.shot("03_main_versions_expanded")
rec("main_versions_tap", bool(vers), f"{len(vers)} 'Versions' rows; list empty because api_url is empty")
e.swipe_up(2); e.shot("04_main_scrolled_bottom"); e.dump("main_bottom")
# Settings
ok = e.tap(rid="box_switch", settle=6)
e.shot("05_settings_top")
r = e.dump("settings_top")
rec("settings_open", ok and "MainActivity" in e.resumed(), "crash check in logcat")
e.swipe_up(2); e.shot("06_settings_scrolled"); e.swipe_up(3); e.shot("07_settings_bottom")
rec("settings_scroll", "MainActivity" in e.resumed())
e.tap(rid="box_settings_icon_close", settle=5)
# About
e.swipe_up(2)
ok = e.tap(rid="box_about", settle=6)
e.shot("08_about_top"); e.dump("about_top")
e.swipe_up(2); e.shot("09_about_scrolled"); e.swipe_up(3); e.shot("10_about_bottom")
rec("about_open_scroll", ok and "MainActivity" in e.resumed())
e.adb("shell", "input", "keyevent", "4"); time.sleep(5)
# Experiments
ok = e.tap(rid="box_experiment", settle=6)
e.shot("11_experiments"); e.dump("experiments")
rec("experiments_open", ok and "MainActivity" in e.resumed())
e.adb("shell", "input", "keyevent", "4"); time.sleep(5)
crash = e.adb("logcat", "-d", "-b", "crash")
open(e.OUT + "/logcat_crash_clickthrough.txt", "w").write(crash)
open(e.OUT + "/logcat_clickthrough.txt", "w").write(e.adb("logcat", "-d", "-v", "time", timeout=180))
rec("no_crash_buffer_entries", "FATAL" not in crash, crash[:300])
json.dump(R, open(e.OUT + "/clickthrough_results.json", "w"), indent=1)

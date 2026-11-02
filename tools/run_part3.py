import json, time, os, emu_clickthrough as e
P = e.OUT + "/clickthrough_results.json"
R = json.load(open(P))
def rec(k, ok, note=""):
    R[k] = {"ok": bool(ok), "note": note}; print(k, ok, note, flush=True)
    json.dump(R, open(P, "w"), indent=1)
def labels():
    return e.text_of("sub_installed"), e.text_of("sub_installed_c")
s0 = labels(); e.shot("12_installed_none")
rec("installed_label_not_installed", s0[0] == "N/A", f"sub_installed={s0[0]!r} sub_installed_c={s0[1]!r}")
# install dummy while MainActivity in foreground -> PACKAGE_ADDED receiver path
print(e.adb("install", "-r", "/tmp/dummy/com.spotify.music.apk", timeout=300).strip()); time.sleep(8)
s1 = labels(); e.shot("13_installed_music_live")
rec("installed_label_live_after_install (receiver)", s1[0] == "0.0.1-dummy", f"{s1}")
print(e.adb("install", "-r", "/tmp/dummy/com.spotify.musix.apk", timeout=300).strip()); time.sleep(8)
s2 = labels(); e.shot("14_installed_both_live")
rec("installed_label_both_live", s2 == ("0.0.1-dummy", "0.0.1-dummy"), f"{s2}")
# uninstall while app is backgrounded -> onResume refresh path
e.adb("shell", "input", "keyevent", "3"); time.sleep(5)
print(e.adb("uninstall", "com.spotify.music", timeout=300).strip()); time.sleep(4)
e.adb("shell", "am", "start", "-n", "com.xc3fff0e.xmanager/.MainActivity"); time.sleep(10)
s3 = labels(); e.shot("15_installed_cloned_only_after_resume")
rec("installed_label_after_bg_uninstall (onResume)", s3[0] is None and s3[1] == "0.0.1-dummy", f"{s3} (sub_installed GONE expected)")
print(e.adb("uninstall", "com.spotify.musix", timeout=300).strip()); time.sleep(8)
s4 = labels(); e.shot("16_installed_none_again_live")
rec("installed_label_live_after_uninstall (receiver)", s4[0] == "N/A", f"{s4}")
# experiments
ok = e.tap(rid="box_experiment", settle=8)
e.shot("11_experiments"); e.dump("experiments")
rec("experiments_open", ok and "MainActivity" in e.resumed())
e.adb("shell", "input", "keyevent", "4"); time.sleep(5)
crash = e.adb("logcat", "-d", "-b", "crash", timeout=300)
open(e.OUT + "/logcat_crash.txt", "w").write(crash)
rec("no_crash_buffer_entries", "FATAL" not in crash, crash[:300])

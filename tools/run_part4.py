import json, time, emu_clickthrough as e
P = e.OUT + "/clickthrough_results.json"
R = json.load(open(P))
for k in ["installed_label_after_bg_uninstall (onResume)", "installed_label_live_after_uninstall (receiver)", "experiments_open"]:
    R.pop(k, None)
def rec(k, ok, note=""):
    R[k] = {"ok": bool(ok), "note": note}; print(k, ok, note, flush=True)
    json.dump(R, open(P, "w"), indent=1)
def labels(): return e.text_of("sub_installed"), e.text_of("sub_installed_c")
def front():
    # resume the existing task the way the launcher does
    e.adb("shell", "am", "start", "-a", "android.intent.action.MAIN", "-c", "android.intent.category.LAUNCHER",
          "-f", "0x10200000", "-n", "com.xc3fff0e.xmanager/.SplashActivity"); time.sleep(15); e.dismiss_anr()
e.adb("install", "-r", "/tmp/dummy/com.spotify.music.apk", timeout=300)
e.adb("install", "-r", "/tmp/dummy/com.spotify.musix.apk", timeout=300)
front()
print("resumed:", e.resumed(), labels())
e.adb("shell", "input", "keyevent", "3"); time.sleep(5)
e.adb("uninstall", "com.spotify.music", timeout=300); time.sleep(4)
front()
s3 = labels(); e.shot("15_installed_cloned_only_after_resume")
rec("installed_label_after_bg_uninstall (onResume)", s3 == (None, "0.0.1-dummy"), f"{s3} resumed={e.resumed()} (sub_installed GONE expected)")
e.adb("uninstall", "com.spotify.musix", timeout=300); time.sleep(8)
s4 = labels(); e.shot("16_installed_none_again_live")
rec("installed_label_live_after_uninstall (receiver)", s4[0] == "N/A", f"{s4}")
ok = e.tap(rid="box_experiment", settle=8)
e.shot("11_experiments"); e.dump("experiments")
rec("experiments_open", ok and "MainActivity" in e.resumed())
e.adb("shell", "input", "keyevent", "4"); time.sleep(5)
crash = e.adb("logcat", "-d", "-b", "crash", timeout=300)
open(e.OUT + "/logcat_crash.txt", "w").write(crash)
rec("no_crash_buffer_entries", "FATAL" not in crash, crash[:300])

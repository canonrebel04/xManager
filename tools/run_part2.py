import json, time, os, emu_clickthrough as e
P = e.OUT + "/clickthrough_results.json"
R = json.load(open(P)) if os.path.exists(P) else {}
def rec(k, ok, note=""):
    R[k] = {"ok": bool(ok), "note": note}; print(k, ok, note, flush=True)
    json.dump(R, open(P, "w"), indent=1)
e.adb("logcat", "-c")
e.adb("shell", "am", "start", "-n", "com.xc3fff0e.xmanager/.SplashActivity"); time.sleep(30)
e.dismiss_anr()
rec("relaunch_after_emulator_restart", "MainActivity" in e.resumed(), e.resumed())
e.swipe_up(2)
ok = e.tap(rid="box_about", settle=8)
e.shot("08_about_top"); e.dump("about_top")
e.swipe_up(2); e.shot("09_about_scrolled"); e.swipe_up(3); e.shot("10_about_bottom")
rec("about_open_scroll", ok and "MainActivity" in e.resumed())
e.adb("shell", "input", "keyevent", "4"); time.sleep(6)
e.shot("10b_after_back_from_about")
e.swipe_up(-0)

import sys, re, xml.etree.ElementTree as ET
for f in sys.argv[1:]:
    root = ET.parse(f).getroot()
    nodes = list(root.iter("node"))
    clickable = [n for n in nodes if n.get("clickable") == "true"]
    def label(n):
        own = (n.get("text") or n.get("content-desc") or "").strip()
        if own: return own
        kids = [ (c.get("text") or c.get("content-desc") or "").strip() for c in n.iter("node") if c is not n]
        kids = [k for k in kids if k]
        return ("(child) " + " / ".join(kids[:3])) if kids else ""
    unl = []
    small = []
    for n in clickable:
        a,b,c,d = map(int, re.findall(r"\d+", n.get("bounds")))
        if not label(n): unl.append((n.get("resource-id"), n.get("class"), n.get("bounds")))
        if (c-a) < 126 or (d-b) < 126: small.append((n.get("resource-id"), label(n), f"{c-a}x{d-b}px"))
    imgs = [n for n in nodes if n.get("class") == "android.widget.ImageView" and n.get("clickable") == "true"]
    print(f"== {f}: {len(clickable)} clickable, {len(unl)} unlabelled, {len(small)} < 48dp (126px @ 2.625x)")
    for u in unl: print("  UNLABELLED", u)
    for s in small: print("  SMALL", s)
    print("  clickable ImageViews:", [(i.get('resource-id','').split('/')[-1], i.get('content-desc')) for i in imgs])

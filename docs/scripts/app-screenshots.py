"""Takes the app's screenshots for the documentation on a phone, in English (public/screenshots/app/en/),
German (de/) and Spanish (es/): cropped below the status bar and above the navigation bar, at half size, as WebP.

Two apps on the phone:
- the debug build (dev.elkuku.kmybanners.dev, `./gradlew installDebug`) for the start: signed out, the sign-in
  code, creating the page, the empty page, screenshots shared, the first build. Its demo mode shows these as
  fixed screens (app/src/main/java/dev/elkuku/kmybanners/ui/Demo.kt), whatever its sign-in;
- the release app (dev.elkuku.kmybanners), signed in, with a page that has medal rows to check, for the rest.

Needs adb (ADB, else on the PATH or in ~/Android/Sdk) and Python with Pillow. Android 13+: the language is set
as the apps' language (cmd locale) and put back to the phone's at the end.

    python3 scripts/app-screenshots.py [en de es]

The buttons are found by their text, read from the app's string resources.
"""
import io
import os
import re
import shutil
import subprocess
import sys
import time
from html import unescape
from pathlib import Path

from PIL import Image

DOCS = Path(__file__).resolve().parent.parent
RES = DOCS.parent / 'app/src/main/res'
OUT = DOCS / 'public/screenshots/app'
APP = 'dev.elkuku.kmybanners'
DEBUG_APP = 'dev.elkuku.kmybanners.dev'
ADB = os.environ.get('ADB') or shutil.which('adb') or str(Path.home() / 'Android/Sdk/platform-tools/adb')
FOLDERS = {'en': 'values', 'de': 'values-de', 'es': 'values-es'}


def adb(*args: str) -> bytes:
    return subprocess.run([ADB, *args], check=True, capture_output=True).stdout


def strings(lang: str) -> dict[str, str]:
    xml = (RES / FOLDERS[lang] / 'strings.xml').read_text()
    return {k: unescape(v).replace("\\'", "'") for k, v in re.findall(r'<string name="([^"]+)"[^>]*>([^<]*)</string>', xml)}


def tap(text: str, wait: float = 2, required: bool = True) -> bool:
    """Taps the element showing [text]; without it, stops (or returns False when not [required])."""
    adb('shell', 'uiautomator', 'dump', '/sdcard/ui.xml')
    xml = adb('exec-out', 'cat', '/sdcard/ui.xml').decode()
    for node in re.findall(r'<node [^>]*>', xml):
        if f'text="{text}"' in node:
            x1, y1, x2, y2 = map(int, re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', node).groups())
            adb('shell', 'input', 'tap', str((x1 + x2) // 2), str((y1 + y2) // 2))
            time.sleep(wait)
            return True
    if not required:
        return False
    raise SystemExit(f'"{text}" is not on the screen')


def bar_heights() -> tuple[int, int]:
    """The status bar and navigation bar heights in pixels, from the phone's insets."""
    dump = adb('shell', 'dumpsys', 'window', 'windows').decode()
    top = re.search(r'type=statusBars[^}]*?top=(\d+)', dump)
    bottom = re.search(r'type=navigationBars[^}]*?bottom=(\d+)', dump)
    return int(top.group(1)) if top else 132, int(bottom.group(1)) if bottom else 63


def shot(path: Path, bars: tuple[int, int]) -> None:
    im = Image.open(io.BytesIO(adb('exec-out', 'screencap', '-p'))).convert('RGB')
    im = im.crop((0, bars[0], im.width, im.height - bars[1]))
    im = im.resize((im.width // 2, im.height // 2), Image.LANCZOS)
    path.parent.mkdir(parents=True, exist_ok=True)
    im.save(path, 'WEBP', quality=82)
    print(path.relative_to(DOCS))


def start() -> None:
    adb('shell', 'am', 'force-stop', APP)
    adb('shell', 'monkey', '-p', APP, '-c', 'android.intent.category.LAUNCHER', '1')
    time.sleep(5)


def demo(name: str) -> None:
    """The debug build showing one of its demo screens: without the DEV mark (else it shows the real app, e.g.
    right after an install; then it is started again)."""
    for _ in range(3):
        adb('shell', 'am', 'force-stop', DEBUG_APP)
        adb('shell', 'am', 'start', '-n', f'{DEBUG_APP}/dev.elkuku.kmybanners.MainActivity', '--es', 'demo', name)
        time.sleep(3)
        adb('shell', 'uiautomator', 'dump', '/sdcard/ui.xml')
        if 'text="DEV"' not in adb('exec-out', 'cat', '/sdcard/ui.xml').decode():
            return
    raise SystemExit(f'The demo screen "{name}" did not show')


def demo_shots(lang: str, s: dict[str, str], bars: tuple[int, int]) -> None:
    out = OUT / lang
    for name in ['signin', 'code', 'empty', 'shared', 'building']:
        demo(name)
        shot(out / f'start-{name}.webp', bars)
    demo('create')
    tap(s['agent_name'], 1)
    adb('shell', 'input', 'text', 'nikp3h')
    adb('shell', 'input', 'keyevent', 'BACK')  # the keyboard
    time.sleep(1)
    shot(out / 'start-create.webp', bars)
    adb('shell', 'am', 'force-stop', DEBUG_APP)


def main(langs: list[str]) -> None:
    adb('shell', 'input', 'keyevent', 'WAKEUP')
    bars = bar_heights()
    try:
        for lang in langs:
            s = strings(lang)
            adb('shell', 'cmd', 'locale', 'set-app-locales', DEBUG_APP, '--locales', lang)
            demo_shots(lang, s, bars)
            adb('shell', 'cmd', 'locale', 'set-app-locales', APP, '--locales', lang)
            start()
            out = OUT / lang
            shot(out / 'home.webp', bars)
            tap(s['howto_show'])
            shot(out / 'howto.webp', bars)
            tap(s['less'], 1)
            tap(s['my_banners'], 4)
            shot(out / 'my-banners.webp', bars)
            tap(s['close'])
            # Step 3 is below the first screen
            adb('shell', 'input', 'swipe', '540', '1800', '540', '700', '400')
            time.sleep(1)
            # Only a page with rows to check has the button: else the picture from before stays
            if tap(s['check_them'], 4, required=False):
                shot(out / 'check.webp', bars)
                tap(s['review_done'])
            else:
                print(f'{lang}: no rows to check on the page, check.webp kept')
            adb('shell', 'input', 'swipe', '540', '700', '540', '1800', '400')
            tap(s['settings'])
            shot(out / 'settings.webp', bars)
            tap(s['close'])
    finally:
        for app in (APP, DEBUG_APP):
            adb('shell', 'cmd', 'locale', 'set-app-locales', app, '--locales', '')
            adb('shell', 'am', 'force-stop', app)


if __name__ == '__main__':
    main(sys.argv[1:] or ['en', 'de', 'es'])

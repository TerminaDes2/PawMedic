"""Prepare a Windows Argos worker and model in the veterinary build directory."""
import hashlib
import os
from pathlib import Path
import subprocess
import sys
import urllib.request
import zipfile

if sys.platform != "win32":
    raise SystemExit("This prototype currently supports building on Windows only.")

apps = Path(__file__).resolve().parents[2]
build = apps / "desktop-veterinary" / "build"
build.mkdir(parents=True, exist_ok=True)
venv = build / "argos-env"
python = venv / "Scripts" / "python.exe"
if not python.is_file():
    subprocess.run([sys.executable, "-m", "venv", str(venv)], check=True)
subprocess.run(
    [str(python), "-m", "pip", "install", "-r", str(Path(__file__).with_name("requirements.txt"))],
    check=True,
)

archive = build / "translate-es_en-1_9.argosmodel"
digest = "c54df2b62fceaf54a3ce5d97db6bf56efd7940063329f6778f4212d2acb370d4"


def sha256(path):
    result = hashlib.sha256()
    with path.open("rb") as source:
        for chunk in iter(lambda: source.read(1024 * 1024), b""):
            result.update(chunk)
    return result.hexdigest()


if not archive.is_file():
    temporary = archive.with_suffix(".download")
    try:
        urllib.request.urlretrieve("https://argos-net.com/v1/translate-es_en-1_9.argosmodel", temporary)
        if sha256(temporary) != digest:
            raise RuntimeError("The downloaded model did not match the expected SHA-256.")
        os.replace(temporary, archive)
    finally:
        if temporary.exists():
            temporary.unlink()
if sha256(archive) != digest:
    raise RuntimeError("The model archive did not match the expected SHA-256.")
models = (build / "argos-models").resolve()
models.mkdir(exist_ok=True)
with zipfile.ZipFile(archive) as source:
    for member in source.infolist():
        if not (models / member.filename).resolve().is_relative_to(models):
            raise RuntimeError("The model archive contains an unsafe path.")
    source.extractall(models)

worker = apps / "desktop-admin" / "src" / "main" / "resources" / "translation" / "argos_worker.py"
subprocess.run(
    [
        str(python), "-m", "PyInstaller", "--noconfirm", "--clean", "--onedir",
        "--name", "pawmedic-argos-worker",
        "--distpath", str(build / "argos-dist"),
        "--workpath", str(build / "argos-pyinstaller"),
        "--specpath", str(build),
        "--collect-all", "argostranslate", "--collect-all", "ctranslate2",
        "--collect-all", "sentencepiece", str(worker),
    ],
    check=True,
)
subprocess.run(
    [str(python), "-I", str(Path(__file__).with_name("offline_smoke_test.py")), str(models)],
    check=True,
)
print("Argos development bundle prepared.")

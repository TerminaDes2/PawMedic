"""Developer smoke test: run the real worker while denying Python socket connections."""
import io
import json
import os
from pathlib import Path
import runpy
import socket
import sys


def deny_network(*args, **kwargs):
    raise RuntimeError("Network disabled for offline smoke test")


if len(sys.argv) != 2:
    raise SystemExit("Pass the directory containing the extracted Argos model")

os.environ["ARGOS_PACKAGES_DIR"] = str(Path(sys.argv[1]).resolve())
socket.socket.connect = deny_network
socket.socket.connect_ex = deny_network
socket.create_connection = deny_network
socket.getaddrinfo = deny_network
request = {"text": "El perro tiene fiebre.", "source": "es", "target": "en"}
sys.stdin = io.TextIOWrapper(io.BytesIO(json.dumps(request).encode("utf-8")), encoding="utf-8")
result = io.BytesIO()
original_stdout = sys.stdout
sys.stdout = io.TextIOWrapper(result, encoding="utf-8")
worker = Path(__file__).parents[2] / "desktop-admin" / "src" / "main" / "resources" / "translation" / "argos_worker.py"
runpy.run_path(str(worker))["main"]()
sys.stdout.flush()
translation = json.loads(result.getvalue().decode("utf-8"))["translation"]
if "dog" not in translation.lower() or "fever" not in translation.lower():
    raise SystemExit("Unexpected translation")
original_stdout.write("Offline Argos smoke test passed\n")

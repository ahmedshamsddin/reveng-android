"""
Deep Dive CTF — Backend verification service.

This lightweight Flask app is spawned per-user by the CTF platform. It reads the
player's unique flag from the FLAG environment variable at startup and exposes a
single endpoint that the challenge APK calls once the intent-spoof is performed.
"""

import os

from flask import Flask, jsonify, request

app = Flask(__name__)

# The administrative token hardcoded inside the Android challenge. The player
# recovers this by decompiling the APK and replays it against this backend.
ADMIN_TOKEN = "m0b1l3_1nt3nt_sp00f_2026"

# Per-user flag, injected strictly via the OS environment at container runtime so
# that no two players ever share the same flag value.
FLAG = os.environ.get("FLAG", "FLAG{missing_flag_env_var}")


@app.route("/verify-intent", methods=["GET"])
def verify_intent():
    """Return the flag only when the correct administrative token is supplied."""
    token = request.args.get("token")

    if token != ADMIN_TOKEN:
        return jsonify({"status": "unauthorized"}), 401

    return jsonify({"status": "authorized", "flag": FLAG}), 200


@app.route("/health", methods=["GET"])
def health():
    """Simple liveness probe for the CTF platform orchestrator."""
    return jsonify({"status": "ok"}), 200


if __name__ == "__main__":
    # Local development entrypoint. In the container, Gunicorn serves the app.
    app.run(host="0.0.0.0", port=5000)

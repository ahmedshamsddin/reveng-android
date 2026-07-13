# Deep Dive — Mobile CTF Challenge

A containerized Android reverse-engineering challenge. Players decompile a
single static APK, discover an exported `AdminDashboardActivity` reachable via a
custom deep link, recover a hardcoded admin token, and use an ADB intent-spoof to
point the app at their own per-user backend container to retrieve the flag.

**Category:** Mobile / Android · **Difficulty:** Easy–Medium

## Architecture

```
.
├── docker-compose.yml          # Spawns the per-user backend
├── backend/                    # Flask container (one per player)
│   ├── Dockerfile
│   ├── app.py                  # /verify-intent endpoint, flag from $FLAG
│   └── requirements.txt
└── android-source/             # Android Studio project → static challenge APK
    ├── build.gradle
    ├── settings.gradle
    ├── gradle.properties
    └── app/src/main/
        ├── AndroidManifest.xml
        └── java/com/ctf/mobile/deepdive/
            ├── MainActivity.kt            # Decoy login screen
            └── AdminDashboardActivity.kt  # Exported, deep-link triggered
```

The **APK is static and identical for every player**. Only the backend container
— and its `FLAG` — is unique per user, which prevents flag sharing.

## Backend

The flag is injected strictly via the `FLAG` environment variable at runtime:

```bash
FLAG="FLAG{your_unique_flag}" docker compose up --build
```

Endpoint contract:

| Request | Response |
| --- | --- |
| `GET /verify-intent?token=m0b1l3_1nt3nt_sp00f_2026` | `200 {"status":"authorized","flag":"FLAG{...}"}` |
| `GET /verify-intent?token=<wrong>` or missing | `401 {"status":"unauthorized"}` |
| `GET /health` | `200 {"status":"ok"}` |

## Building the APK

Open `android-source/` in Android Studio (or run `./gradlew assembleRelease`
after adding the Gradle wrapper) to produce the distributable APK.

## Intended solution

1. Download and decompile the APK with `jadx-gui`.
2. In `AndroidManifest.xml`, note `AdminDashboardActivity` is
   `android:exported="true"` and handles the `ctf://deep-dive/unlock` deep link.
3. In the decompiled `AdminDashboardActivity`, notice the token is **not** a
   plaintext constant — it is stored XOR-obfuscated + Base64 (`DATA`/`SEED`) and
   rebuilt at runtime by `resolveAdminToken()`. Read that routine and replay it
   to recover the token `m0b1l3_1nt3nt_sp00f_2026`, and note the `backend` query
   parameter. (Grader shortcut to recover it statically:)

   ```python
   import base64
   DATA = "CQEUAjMCMUVeK0cGRwAAGARUVihtQgEC"
   SEED = "d1v3_1nt0_th3_sh4d0w_r34lm".encode()
   raw = base64.b64decode(DATA)
   print(bytes(b ^ SEED[i % len(SEED)] for i, b in enumerate(raw)).decode())
   # -> m0b1l3_1nt3nt_sp00f_2026
   ```

   A player could instead recover it dynamically — e.g. Frida-hook
   `resolveAdminToken` and print its return value, or read logcat/network.
4. Install the APK on an emulator/device and trigger the exported activity,
   pointing it at your own backend (use `10.0.2.2` for the emulator's host loopback):

   ```bash
   adb shell am start -W -a android.intent.action.VIEW \
     -d "ctf://deep-dive/unlock?token=m0b1l3_1nt3nt_sp00f_2026&backend=http://10.0.2.2:5000"
   ```

5. The admin dashboard calls your backend and renders the flag on screen.

The login screen in `MainActivity` is a deliberate rabbit hole — its credential
check leads nowhere.

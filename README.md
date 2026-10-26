# GorillaGram 🦍

An unofficial, powerful, and modern Telegram client for Android tailored with fluid iOS styling, custom typography, and advanced customizations.

---

## ✨ Features

- 💬 **iOS 3-Bubble Chat Bar**: Redesigned input layout featuring standalone circular action bubbles, unified AI pill integration, and tactile spring bounce physics.
- 🔤 **Apple SF Pro Typography**: Crisp, clean typography powered by integrated SF Pro font families.
- 🦍 **Gorilla Branding & Adaptive Icons**: Custom Gorilla launcher icons and tuned Gorilla themes (Dark and Light variants).
- ⚙️ **G-Settings Suite**: Granular privacy features, ghost mode options, chat bar reordering, and deep interface tweaks.

---

## 🛠️ Compilation Guide

1. **Clone the repository with submodules:**
   ```bash
   git clone --recursive --shallow-submodules https://github.com/RiderX9/GorillaGram.git GorillaGram
   cd GorillaGram
   ```

2. **Telegram API Credentials:**
   Obtain `TELEGRAM_APP_ID` and `TELEGRAM_APP_HASH` from the [Telegram Developer Portal](https://my.telegram.org/auth) and create a `local.properties` file in the project root:
   ```properties
   TELEGRAM_APP_ID=<your_telegram_app_id>
   TELEGRAM_APP_HASH=<your_telegram_app_hash>
   ```

3. **Fast Local Build (Optional):**
   To target your specific device architecture (e.g. 64-bit ARM) and avoid long WebRTC build times:
   ```bash
   export NATIVE_TARGET=arm64-v8a
   ./gradlew assembleNormalDebug
   ```

4. **Signing (Release Builds):**
   Add your keystore credentials in `local.properties`:
   ```properties
   KEYSTORE_PASS=<your_keystore_password>
   ALIAS_NAME=<your_alias_name>
   ALIAS_PASS=<your_alias_password>
   ```

---

## 📜 License & Acknowledgments

GorillaGram is licensed under the [GNU General Public License v3.0](LICENSE).

Special thanks to the upstream projects that make this client possible:
- [NagramXF](https://github.com/Keeperorowner/NagramXF)
- [NagramX](https://github.com/risin42/NagramX)
- [AyuGram](https://github.com/AyuGram/AyuGram4A)
- [Cherrygram](https://github.com/arsLan4k1390/Cherrygram)
- [exteraGram](https://github.com/exteraSquad/exteraGram)

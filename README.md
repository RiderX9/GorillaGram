# GorillaGram 🦍

**GorillaGram** is an enhanced, customized fork of [NagramXF](https://github.com/Keeperorowner/NagramXF) built for a fluid, modern iOS-style experience with custom typography and powerful enhancements.

---

## ✨ Features & Enhancements

### 💬 iOS-Style Floating Chat Bar
- **3-Bubble Layout**: Detached circular action bubbles for attachments and send/mic flanking the main input bar.
- **Dynamic AI & Formatting**: AI button and formatting options appear automatically when your text expands beyond 2 lines.
- **Tactile Physics**: Fluid spring bounce animations on the chat bar, action buttons, and pinned message bar.

### 🔤 Custom Typography
- Integrated **Apple SF Pro** font family.
- Integrated **Linotte** font family.

### 🦍 Branding & Themes
- Custom **Gorilla** adaptive launcher icons.
- Custom notification icons and matching status bar branding.
- Pre-installed Gorilla Dark & Light themes.

### 🛡️ Full NagramXF Power
- Comprehensive privacy settings (ghost mode, read receipts control).
- Built-in message translation, chat tabs customization, and deleted message logs.

---

## 🛠️ How to Build

1. **Clone the repo with submodules:**
   ```bash
   git clone --recursive https://github.com/RiderX9/GorillaGram.git
   cd GorillaGram
   ```

2. **Add Telegram API keys to `local.properties`:**
   ```properties
   TELEGRAM_APP_ID=your_app_id
   TELEGRAM_APP_HASH=your_app_hash
   ```

3. **Build the release APK:**
   ```bash
   ./gradlew assembleNormalRelease
   ```

---

## 📜 Credits & License

GorillaGram is licensed under the [GNU General Public License v3.0](LICENSE).

Based on and credited to:
- [NagramXF](https://github.com/Keeperorowner/NagramXF) (Upstream base)
- [NagramX](https://github.com/risin42/NagramX) & [Nekogram](https://github.com/Nekogram/Nekogram)
- [Telegram for Android](https://github.com/DrKLO/Telegram)

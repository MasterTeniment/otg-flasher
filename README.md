# FlashOdin - Mobile-to-Mobile OTG Firmware Flasher 📱⚡

**FlashOdin** is a lightweight, open-source Android utility designed to flash firmware packages onto Samsung devices directly from another Android phone using an OTG cable—eliminating the need for a PC.

---

## ✨ Features
* **PC-Free Flashing:** Flash firmware directly from one Android device to another via USB OTG.
* **No Root Required:** Operates entirely using standard Android USB Host APIs (`android.hardware.usb`) on the host device.
* **Standard Package Support:** Compatible with `.tar` and `.tar.md5` firmware archives (AP, BL, CP, CSC).
* **Safe File Selection:** Uses Android's native Storage Access Framework (SAF) without requiring invasive full-storage permissions.
* **Real-Time Progress & Logs:** Live transfer progress bar and detailed protocol logs during flashing.

---

## 📋 Prerequisites
1. **Host Device:** An Android phone/tablet (Android 7.0+) with **USB OTG** support.
2. **Target Device:** A compatible Samsung device booted into **Download Mode (Odin Mode)**.
3. **Hardware:** A reliable USB OTG adapter/cable (Type-C to Type-C or OTG adapter + USB data cable).
4. **Firmware:** Valid firmware files downloaded locally on the host device.

---

## 🚀 How to Use
1. Download the latest APK from the [Releases](https://github.com/MasterTeniment/otg-flasher/releases) section and install it on your **Host** device.
2. Boot your **Target** device into **Download Mode**.
3. Connect both devices using your OTG cable and grant USB permission when prompted.
4. Tap **Select Firmware** to load your `.tar` or `.tar.md5` file(s).
5. Verify the connection status and tap **Start Flash**.
6. Wait until the process finishes and the target device reboots automatically.

---

## 🛠️ Building from Source
This repository includes a configured **GitHub Actions** workflow that automatically compiles the debug/release APK on every push. 

To build locally:
```bash
git clone [https://github.com/MasterTeniment/otg-flasher.git](https://github.com/MasterTeniment/otg-flasher.git)
cd otg-flasher
./gradlew assembleDebug

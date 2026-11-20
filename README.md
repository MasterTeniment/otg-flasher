<div align="center">

# OTG FlashTool — Mobile-to-Mobile USB Firmware Flasher ⚡📱

### اپلیکیشن نیتیو فلش رام، ADB و Fastboot موبایل به موبایل از طریق کابل OTG (بدون نیاز به کامپیوتر و روت)

[![Latest Release](https://img.shields.io/github/v/release/MasterTeniment/otg-flasher?include_prereleases&label=Download%20APK&color=2563EB)](https://github.com/MasterTeniment/otg-flasher/releases)
[![Android API](https://img.shields.io/badge/Android-5.0%2B%20(API%2021%E2%80%9334)-3DDC84?logo=android&logoColor=white)](https://developer.android.com/)
[![Kotlin & Compose](https://img.shields.io/badge/Built%20with-Kotlin%20%26%20Jetpack%20Compose-7F52FF?logo=kotlin&logoColor=white)](https://developer.android.com/jetpack/compose)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

[🇺🇸 English](#english-docs) • [🇮🇷 فارسی](#persian-docs) • [🌐 Socials & Community](#connect-with-teniment)

</div>

---

<a id="english-docs"></a>
## 🇺🇸 English

**OTG FlashTool** (`com.otgflashtool.app`) is an independent, open-source native Android application built with **Kotlin** and **Jetpack Compose**. It enables developers and repair technicians to flash 4-file firmware packages, inspect `PIT` partition tables, and execute **ADB / Fastboot** commands on a second Android device directly over a **USB OTG cable** — with **zero PC and zero Root required**.

### ✨ Key Features

- **4-File Firmware Flasher (Download Mode v2 & v3)**
  - Full support for **BL**, **AP**, **CP**, and **CSC / HOME_CSC** slots (`.tar`, `.tar.md5`, `.lz4`, `.img`, `.bin`).
  - Automatic **PIT partition table** inspection and smart partition mapping.
  - Broad device compatibility from **Android 5.0 (API 21)** to **Android 14 (API 34)**, including legacy Micro-USB hosts and older v2 target devices.
  - Android `USBFS`-safe **16 KiB chunked bulk streaming** and $O(1)$ `FileChannel` TAR scanning to handle massive 3–5 GB `AP` images smoothly without `OutOfMemory` crashes.
- **ADB & Fastboot over USB OTG**
  - One-tap reboot to **Download Mode**, **Fastboot**, or **Recovery**, plus an interactive **ADB Shell**.
  - Direct **Fastboot** `.img` partition flashing (`boot`, `recovery`, `vbmeta`, etc.).
- **Native Bilingual UI (English + فارسی)**
  - First-launch language selector with live `LTR` / `RTL` layout switching anytime from the top bar or **Settings**.
- **Built-in Interactive Guide**
  - Visual OTG cable direction guide, legacy Micro-USB host tips, and an expandable tutorial for enabling **Developer Options** and **USB Debugging**.
- **Google Play Policy-Compliant Architecture**
  - Uses official **Android USB Host API (`UsbManager`)** exclusively — no Root required and zero host OS modifications.
  - Uses **Storage Access Framework (`SAF`)** — no invasive `MANAGE_EXTERNAL_STORAGE` permission.
  - Runs an Android **Foreground Service (`dataSync`)** with live progress notifications and `WakeLock` to prevent mid-flash interruption.
  - Hosts **zero** copyrighted firmware/ROM files — users select their own local files.

### 🔌 Correct OTG Cable Connection

| Device Role | Cable End to Connect |
| :--- | :--- |
| **Host Phone** (sending firmware / running app) | Connect the **OTG Adapter** end here |
| **Target Phone** (receiving firmware) | Connect the **standard USB data/charging cable** end here |

> **Tip:** Enable **Developer Options** and **USB Debugging** on the target phone beforehand (mandatory for ADB commands; recommended before Download Mode flashing).

### 📥 Download & Build

- **Pre-compiled APK:** Download the latest release directly from the **[GitHub Releases](https://github.com/MasterTeniment/otg-flasher/releases)** page.
- **Build from Source:**
      git clone https://github.com/MasterTeniment/otg-flasher.git
      cd otg-flasher
      ./gradlew assembleDebug

---

<a id="persian-docs"></a>
<div dir="rtl">

## 🇮🇷 فارسی

**فلش‌تول OTG** (`com.otgflashtool.app`) یک اپلیکیشن نیتیو، مستقل و متن‌باز برای اندروید (توسعه‌یافته با **Kotlin** و **Jetpack Compose**) است که به توسعه‌دهندگان و تعمیرکاران موبایل اجازه می‌دهد رام‌های ۴ فایله (`BL / AP / CP / CSC`) را فلش بزنند، جدول پارتیشن `PIT` را بررسی کنند و دستورات **ADB** و **Fastboot** را روی یک گوشی اندرویدی دیگر مستقیماً از طریق کابل **USB OTG** اجرا نمایند — **بدون نیاز به کامپیوتر و بدون نیاز به روت**.

### ✨ امکانات و ویژگی‌های کلیدی

- **فلشر کامل رام‌های ۴ فایله (پروتکل Download Mode نسخه v2 و v3)**
  - پشتیبانی از تمامی بخش‌های **BL**، **AP**، **CP** و **CSC / HOME_CSC** با فرمت‌های `.tar`، `.tar.md5`، `.lz4`، `.img` و `.bin`.
  - خواندن خودکار **جدول پارتیشن PIT** و نگاشت هوشمند پارتیشن‌ها.
  - سازگاری گسترده از **اندروید ۵.۰ (API 21)** تا **اندروید ۱۴ (API 34)**؛ شامل گوشی‌های قدیمی با پورت Micro-USB و دستگاه‌های جدید Type-C.
  - انتقال ایمن داده‌ها در بسته‌های **۱۶ کیلوبایتی** سازگار با `USBFS` اندروید و اسکن سریع فایل‌های TAR با `FileChannel` برای پردازش فایل‌های حجیم ۳ تا ۵ گیگابایتی `AP` بدون خطای پر شدن حافظه رم.
- **اجرای ADB و Fastboot از طریق کابل OTG**
  - ریبوت تک‌کلیکی دستگاه مقصد به حالت‌های **Download Mode**، **Fastboot** یا **Recovery** به همراه **ADB Shell** تعاملی.
  - فلش مستقیم فایل‌های پارتیشن `.img` در حالت **Fastboot** (مانند `boot`، `recovery`، `vbmeta` و...).
- **رابط کاربری دو‌زبانه نیتیو (فارسی و انگلیسی)**
  - انتخاب زبان در اولین اجرای برنامه با قابلیت تغییر آنی چیدمان راست‌به‌چپ (`RTL`) و چپ‌به‌راست (`LTR`) از نوار بالا یا بخش تنظیمات.
- **راهنمای جامع داخلی**
  - نمایش جهت صحیح اتصال کابل OTG، نکات مربوط به گوشی‌های قدیمی Micro-USB و آموزش گام‌به‌گام فعال‌سازی **Developer Options** و **USB Debugging**.
- **معماری کاملاً منطبق با قوانین گوگل‌پلی**
  - استفاده انحصاری از رابط رسمی **Android USB Host API (`UsbManager`)** بدون نیاز به روت و بدون دستکاری سیستم‌عامل گوشی مبدا.
  - انتخاب فایل‌ها از طریق **Storage Access Framework (`SAF`)** بدون درخواست دسترسی حساس به کل حافظه (`MANAGE_EXTERNAL_STORAGE`).
  - اجرای عملیات در قالب سرویس پس‌زمینه استاندارد (**Foreground Service**) همراه با نوتیفیکیشن پیشرفت زنده و `WakeLock` برای جلوگیری از قطع شدن ارتباط در حین فلش.
  - عدم میزبانی هیچ‌گونه فایل رام دارای کپی‌رایت (کاربر فایل‌های شخصی خود را از حافظه انتخاب می‌کند).

### 🔌 جهت صحیح اتصال کابل OTG

| نقش گوشی | سرِ کابل که باید متصل شود |
| :--- | :--- |
| **گوشی مبدا** (گوشی که برنامه روی آن نصب است و فایل را می‌فرستد) | سرِ **تبدیل OTG** باید به این گوشی وصل شود |
| **گوشی مقصد** (گوشی که قرار است فلش شود) | سرِ **کابل دیتای معمولی (کابل شارژر)** باید به این گوشی وصل شود |

> **توصیه:** ترجیحاً پیش از شروع کار، گزینه‌های **Developer Options** و **USB Debugging** را در تنظیمات گوشی مقصد فعال کنید (برای دستورات ADB ضروری و برای فلش در Download Mode توصیه‌شده است).

### 📥 دانلود آخرین نسخه APK

برای دانلود مستقیم فایل نصبی (APK) وارد صفحه **[GitHub Releases](https://github.com/MasterTeniment/otg-flasher/releases)** شوید.

</div>

---

<div align="center">

## Connect with Teniment 🌐

[![YouTube](https://img.shields.io/badge/YouTube-@teniment-FF0000?style=for-the-badge&logo=youtube&logoColor=white)](https://youtube.com/@teniment?si=7e8G_RFO-C4ffA--)
[![Telegram](https://img.shields.io/badge/Telegram-@TenimentApps-26A5E4?style=for-the-badge&logo=telegram&logoColor=white)](https://t.me/TenimentApps)
[![Instagram](https://img.shields.io/badge/Instagram-@tenimenttech-E4405F?style=for-the-badge&logo=instagram&logoColor=white)](https://www.instagram.com/tenimenttech?fqnt=MXV6MzltMmZ6eWlyYw==)
[![Facebook](https://img.shields.io/badge/Facebook-Teniment-1877F2?style=for-the-badge&logo=facebook&logoColor=white)](https://www.facebook.com/share/1A6BybCG6b/)

</div>

---

## ⚠️ Disclaimer / سلب مسئولیت

**EN:** This software is an independent, open-source technical utility intended for developers and repair technicians. Flashing firmware carries inherent risks (including data loss or device bricking if incorrect or corrupted firmware is used). Ensure your host device has at least 50% battery and use a reliable cable. Users assume full responsibility when using this tool on any external target device. This project is not affiliated with, endorsed by, or associated with Samsung Electronics or any other smartphone manufacturer.

<div dir="rtl">

**FA:** این نرم‌افزار یک ابزار فنی مستقل و متن‌باز است که برای توسعه‌دهندگان و تعمیرکاران طراحی شده است. عملیات فلش رام همواره با ریسک‌هایی (از جمله از دست رفتن اطلاعات یا بریک شدن دستگاه در صورت انتخاب رام اشتباه یا ناقص) همراه است. پیش از شروع، از شارژ حداقل ۵۰ درصدی گوشی مبدا و سالم بودن کابل رابط مطمئن شوید. مسئولیت کامل استفاده از این ابزار بر عهده کاربر است و این پروژه هیچ‌گونه وابستگی یا ارتباطی با شرکت سامسونگ یا سایر سازندگان موبایل ندارد.

</div>

---

## 📄 License
Distributed under the **MIT License**. See `LICENSE` for more information.

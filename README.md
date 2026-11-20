<div align="center">

# OTG FlashTool — Mobile-to-Mobile USB Firmware Flasher

### اپلیکیشن نیتیو فلش موبایل به موبایل از طریق کابل OTG (بدون نیاز به کامپیوتر و روت)

[![Latest Release](https://img.shields.io/github/v/release/MasterTeniment/otg-flasher?include_prereleases&label=Download%20APK&color=2563EB)](https://github.com/MasterTeniment/otg-flasher/releases)
[![Android API](https://img.shields.io/badge/Android-5.0%2B%20(API%2021%E2%80%9334)-3DDC84?logo=android&logoColor=white)](https://developer.android.com/)
[![Languages](https://img.shields.io/badge/Languages-English%20%7C%20%D9%81%D8%A7%D8%B1%D8%B3%DB%8C-0EA5E9)](#)

[English](#english) • [فارسی](#فارسی) • [Community & Socials](#connect-with-teniment)

</div>

---

## English

**OTG FlashTool** (`com.otgflashtool.app`) is an independent, open-source native Android application built with **Kotlin** and **Jetpack Compose** that allows developers and repair technicians to flash firmware, inspect PIT partition tables, and run **ADB / Fastboot** commands on a second Android device directly over a **USB OTG cable** — with **no PC and no Root required**.

### Key Features

- **4-File Firmware Flasher (Download Mode v2 & v3)**
  - Supports **BL**, **AP**, **CP**, and **CSC / HOME_CSC** slots (`.tar`, `.tar.md5`, `.lz4`, `.img`, `.bin`).
  - Automatic **PIT partition table** inspection and smart partition mapping.
  - Broad hardware compatibility from Android 5.0 (API 21) to Android 14 (API 34), including legacy Micro-USB hosts and older v2 targets.
  - Android `USBFS`-safe 16 KiB chunked bulk streaming and O(1) `FileChannel` TAR scanning for large 3–5 GB `AP` images.
- **ADB & Fastboot over USB OTG**
  - One-tap reboot to **Download Mode**, **Fastboot**, or **Recovery**, plus interactive **ADB Shell**.
  - Direct **Fastboot** `.img` partition flashing (`boot`, `recovery`, `vbmeta`, etc.).
- **Bilingual Interface (English + Persian / فارسی)**
  - First-launch language selector with live `LTR` / `RTL` switching anytime inside **Settings** or the top bar.
- **Built-in Step-by-Step Guide**
  - Correct OTG cable direction diagram, legacy Micro-USB host tips, and an expandable tutorial for enabling **Developer Options** and **USB Debugging**.
- **Google Play Policy-Compliant Design**
  - Uses official **Android USB Host API (`UsbManager`)** exclusively — no Root required and zero host OS modifications.
  - Uses **Storage Access Framework (`SAF`)** — no `MANAGE_EXTERNAL_STORAGE` permission.
  - Runs an Android **Foreground Service (`dataSync`)** with live progress notifications and `WakeLock`.
  - Hosts **zero** copyrighted firmware/ROM files — users select their own local files.
  - Mandatory first-launch Safety Warning & Liability Disclaimer.

### Correct OTG Cable Connection

| Phone | Cable End |
| :--- | :--- |
| **Host Phone** (sending firmware files) | Connect the **OTG Adapter** end here |
| **Target Phone** (receiving firmware) | Connect the **regular USB charging cable** end here |

> **Recommended:** Enable **Developer Options** and **USB Debugging** on the target phone beforehand (required for ADB; recommended before Download Mode flashing).

### Download

Download the latest compiled APK directly from the **[GitHub Releases](https://github.com/MasterTeniment/otg-flasher/releases)** page.

---

<div dir="rtl">

## فارسی

**فلش‌تول OTG** (`com.otgflashtool.app`) یک اپلیکیشن نیتیو و متن‌باز اندروید (ساخته‌شده با **Kotlin** و **Jetpack Compose**) برای فلش رام‌های ۴ فایله (`BL / AP / CP / CSC`)، خواندن جدول پارتیشن `PIT` و اجرای دستورات **ADB** و **Fastboot** روی گوشی دوم از طریق کابل **USB OTG** — **بدون نیاز به کامپیوتر و بدون نیاز به روت** است.

### امکانات کلیدی
- پشتیبانی کامل از **فلش ۴ فایله** (`.tar`، `.tar.md5` و `.lz4`) در پروتکل‌های **Download Mode نسخه v2 و v3**
- سازگار با اندروید ۵ تا اندروید ۱۴ (گوشی‌های قدیمی Micro-USB و مدل‌های جدید Type-C)
- بخش اختصاصی **ADB** (انتقال تک‌کلیکی به دانلود مود، ریکاوری و فست‌بوت + Shell) و **Fastboot**
- **دو‌زبانه کامل (فارسی و انگلیسی)** با انتخاب زبان در اولین اجرا و قابل تغییر در تنظیمات
- **راهنمای جامع داخلی** شامل جهت صحیح کابل OTG و کادر کشویی آموزش فعال‌سازی Developer Options و USB Debugging
- طراحی کاملاً منطبق با قوانین گوگل‌پلی (`UsbManager` رسمی، انتخاب فایل با `SAF`، سرویس پس‌زمینه `Foreground Service` و بدون میزبانی رام)

### جهت صحیح اتصال کابل OTG
- **گوشی مبدا (گوشی که فایل را می‌ریزد):** سر **تبدیل OTG** باید به این گوشی وصل شود.
- **گوشی مقصد (گوشی که فایل رویش نصب می‌شود):** سر **کابل شارژر عادی** باید به این گوشی وصل شود.

> **توصیه:** ترجیحاً قبل از شروع، گزینه‌های **Developer Options** و **USB Debugging** را در تنظیمات گوشی مقصد فعال کنید (برای ADB ضروری و برای Download Mode توصیه‌شده است).

### دانلود آخرین نسخه APK
برای دانلود مستقیم فایل نصبی اندروید وارد صفحه **[Releases](https://github.com/MasterTeniment/otg-flasher/releases)** شوید.

</div>

---

## Connect with Teniment

[![YouTube](https://img.shields.io/badge/YouTube-@teniment-FF0000?style=for-the-badge&logo=youtube&logoColor=white)](https://youtube.com/@teniment?si=7e8G_RFO-C4ffA--)
[![Telegram](https://img.shields.io/badge/Telegram-@TenimentApps-26A5E4?style=for-the-badge&logo=telegram&logoColor=white)](https://t.me/TenimentApps)
[![Instagram](https://img.shields.io/badge/Instagram-@tenimenttech-E4405F?style=for-the-badge&logo=instagram&logoColor=white)](https://www.instagram.com/tenimenttech?fqnt=MXV6MzltMmZ6eWlyYw==)
[![Facebook](https://img.shields.io/badge/Facebook-Teniment-1877F2?style=for-the-badge&logo=facebook&logoColor=white)](https://www.facebook.com/share/1A6BybCG6b/)

---

## Disclaimer / سلب مسئولیت

This software is an independent, open-source technical utility intended for developers and repair technicians. Flashing firmware carries inherent risks (including data loss or device bricking if incorrect firmware is used). Users assume full responsibility when using this tool on any external target device. This project is not affiliated with or endorsed by any smartphone manufacturer.

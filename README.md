<div align="center">

# OTG FlashTool — Mobile-to-Mobile USB Firmware Flasher 📱⚡
### اپلیکیشن نیتیو فلش موبایل به موبایل با کابل OTG (بدون کامپیوتر و بدون روت)

<p>
  <a href="#english"><img src="https://img.shields.io/badge/🇺🇸%20English-Read%20in%20English-2563EB?style=for-the-badge" alt="English"></a>
  <a href="#persian"><img src="https://img.shields.io/badge/🇮🇷%20فارسی-راهنمای%20فارسی-10B981?style=for-the-badge" alt="فارسی"></a>
</p>

[![Download Latest APK](https://img.shields.io/github/v/release/MasterTeniment/otg-flasher?include_prereleases&label=Download%20APK&color=2563EB)](https://github.com/MasterTeniment/otg-flasher/releases)
[![Android](https://img.shields.io/badge/Android-5.0%2B%20(API%2021%E2%80%9334)-3DDC84?logo=android&logoColor=white)](https://developer.android.com/)
[![License: MIT](https://img.shields.io/badge/License-MIT-F59E0B.svg)](LICENSE)

</div>

---

<a id="english"></a>

## 🇺🇸 English

**OTG FlashTool** (`com.otgflashtool.app`) is an independent, open-source native Android utility built with **Kotlin** and **Jetpack Compose** that lets you flash firmware packages, inspect PIT partition tables, and execute **ADB / Fastboot** commands on a second Android device directly over a **USB OTG cable** — eliminating the need for a PC.

### ✨ Features
* **PC-Free Flashing:** Flash firmware directly from one Android device to another via USB OTG.
* **No Root Required:** Operates exclusively through the official Android USB Host API (`android.hardware.usb`) with zero changes to the host OS.
* **4-File Package Support (Download Mode v2 & v3):** Supports **BL**, **AP**, **CP**, and **CSC / HOME_CSC** slots (`.tar`, `.tar.md5`, `.lz4`, `.img`, `.bin`) plus automatic **PIT** reading.
* **ADB & Fastboot Included:** One-tap reboot to Download Mode, Fastboot, or Recovery, interactive ADB Shell, and direct Fastboot `.img` partition flashing.
* **Safe File Selection (SAF):** Uses Android's native Storage Access Framework (`ACTION_OPEN_DOCUMENT`) without requesting `MANAGE_EXTERNAL_STORAGE`.
* **Foreground Service Protection:** Runs a `dataSync` Foreground Service with live progress notification and `WakeLock` to prevent interruptions mid-flash.
* **Bilingual App UI (English & Persian):** First-launch language selector with instant `LTR` / `RTL` switching anytime inside Settings.

### 📋 Prerequisites
1. **Host Device:** An Android phone/tablet (**Android 5.0+**) with **USB OTG Host** support and at least **40–50% battery**.
2. **Target Device:** An Android device booted into **Download Mode**, **Fastboot**, or with **USB Debugging** enabled (for ADB).
3. **Hardware:** A reliable **USB OTG adapter** + standard USB charging/data cable (or Type-C to Type-C OTG cable).
4. **Firmware:** Valid firmware files saved locally on the host device (this app does not host or distribute any firmware files).

### 🔌 Correct OTG Cable Direction
* **Host Phone** (sending the firmware files): Plug the **OTG Adapter** end into this phone.
* **Target Phone** (receiving the firmware): Plug the **regular USB charging cable** end into this phone.

> **Tip:** It is recommended to enable **Developer Options** and **USB Debugging** on the target phone beforehand (mandatory for ADB; recommended for Download Mode). A step-by-step expandable tutorial is built into the app's **Guide** tab.

### 🚀 How to Use
1. Download the latest APK from the **[Releases](https://github.com/MasterTeniment/otg-flasher/releases)** page and install it on your **Host** device.
2. Boot your **Target** device into **Download Mode** (press Volume Up at the warning screen until `Downloading...` appears).
3. Connect both devices following the cable direction above, tap **Scan & Connect**, and approve the USB permission dialog.
4. Select your local firmware slot(s) (**BL**, **AP**, **CP**, **CSC / HOME_CSC**).
5. Verify the connection and PIT table, then tap **Start Flash**.
6. Do **not** unplug the cable until `PASS!` appears and the target device reboots automatically.

### 🛠️ Building from Source
    git clone https://github.com/MasterTeniment/otg-flasher.git
    cd otg-flasher
    gradle assembleDebug

### ⚠️ Disclaimer
* **Use at your own risk.** Flashing firmware carries inherent risks, including data loss, soft/hard bricking, or voiding your device warranty.
* *This project is an independent open-source utility and is not affiliated with, endorsed by, or associated with any smartphone manufacturer.*

---

<a id="persian"></a>

<div dir="rtl">

## 🇮🇷 فارسی

**فلش‌تول OTG** (`com.otgflashtool.app`) یک ابزار نیتیو و متن‌باز (Open-Source) برای اندروید (ساخته‌شده با **Kotlin** و **Jetpack Compose**) است که به شما امکان می‌دهد فایل‌های رام را مستقیماً از یک گوشی اندرویدی دیگر با کابل **USB OTG** فلش بزنید، جدول پارتیشن `PIT` را بخوانید و دستورات **ADB** و **Fastboot** را اجرا کنید — بدون اینکه نیازی به کامپیوتر یا لپ‌تاپ داشته باشید.

### ✨ ویژگی‌ها و امکانات
* **فلش بدون نیاز به کامپیوتر:** انتقال و فلش مستقیم رام از موبایل به موبایل از طریق پورت USB OTG.
* **بدون نیاز به روت (No Root):** عملکرد کاملاً استاندارد با استفاده از رابط رسمی `UsbManager` اندروید و بدون هیچ تغییری در سیستم‌عامل گوشی مبدا.
* **پشتیبانی از رام‌های ۴ فایله (Download Mode v2 و v3):** دارای ۴ اسلات مجزای **BL**، **AP**، **CP** و **CSC / HOME_CSC** با پشتیبانی از `.tar`، `.tar.md5`، `.lz4`، `.img` و `.bin` به همراه خواندن خودکار **PIT**.
* **ابزار کامل ADB و Fastboot:** انتقال تک‌کلیکی به Download Mode، Fastboot و Recovery، اجرای دستورات ADB Shell و فلش مستقیم پارتیشن‌های `.img` در Fastboot.
* **انتخاب ایمن فایل‌ها (SAF):** استفاده از فایل‌پیکر استاندارد اندروید (`ACTION_OPEN_DOCUMENT`) بدون درخواست دسترسی حساس به کل حافظه.
* **محافظت در پس‌زمینه (Foreground Service):** دارای سرویس `dataSync` با نوتیفیکیشن زنده پیشرفت و `WakeLock` برای جلوگیری از قطع شدن وسط فلش.
* **دو‌زبانه کامل (فارسی و انگلیسی):** انتخاب زبان در اولین اجرا و امکان تغییر زبان در هر زمان از بخش تنظیمات برنامه.

### 📋 پیش‌نیازها
1. **گوشی مبدا (Host):** یک گوشی یا تبلت اندرویدی (**اندروید ۵.۰ به بالا**) با پشتیبانی از **USB OTG** و شارژ حداقل **۴۰ تا ۵۰ درصد**.
2. **گوشی مقصد (Target):** گوشی دوم در حالت **Download Mode**، **Fastboot** یا با **USB Debugging** روشن (برای ADB).
3. **کابل رابط:** تبدیل **OTG** سالم و باکیفیت + کابل دیتای شارژر استاندارد (یا کابل Type-C به Type-C).
4. **فایل رام:** فایل‌های رام معتبر و سازگار با مدل گوشی مقصد روی حافظه گوشی مبدا (این برنامه هیچ فایل رامی را میزبانی نمی‌کند).

### 🔌 جهت صحیح اتصال کابل OTG (بسیار مهم)
* **گوشی مبدا** (گوشی که فایل رام داخل آن است و عملیات فلش را انجام می‌دهد): سر **تبدیل OTG** باید به این گوشی وصل شود.
* **گوشی مقصد** (گوشی که فایل رویش نصب می‌شود): سر **کابل شارژر عادی** باید به این گوشی وصل شود.

> **توصیه:** ترجیحاً قبل از شروع، گزینه‌های **Developer Options** و **USB Debugging** را در تنظیمات گوشی مقصد فعال کنید (برای ADB ضروری و برای Download Mode توصیه‌شده است). آموزش گام‌به‌گام فعال‌سازی آن در تب **راهنما**ی خود برنامه قرار دارد.

### 🚀 راهنمای استفاده
1. آخرین نسخه فایل نصبی (APK) را از بخش **[Releases](https://github.com/MasterTeniment/otg-flasher/releases)** دانلود کرده و روی **گوشی مبدا** نصب کنید.
2. **گوشی مقصد** را خاموش کرده و وارد حالت **Download Mode** شوید (در صفحه هشدار، یک‌بار ولوم بالا را بزنید تا عبارت `Downloading...` نمایش داده شود).
3. دو گوشی را مطابق جهت بالا با کابل OTG به هم وصل کنید، دکمه **اسکن و اتصال** را بزنید و مجوز USB را تایید نمایید.
4. فایل‌های رام خود را در اسلات‌های مربوطه (**BL**، **AP**، **CP**، **CSC / HOME_CSC**) انتخاب کنید.
5. پس از تایید اتصال و خوانده شدن جدول PIT، دکمه **شروع فلش** را لمس کنید.
6. تا نمایش پیام `PASS!` و ریستارت خودکار گوشی مقصد، به هیچ عنوان کابل را جدا نکنید.

### ⚠️ سلب مسئولیت و نکات ایمنی
* **مسئولیت استفاده از این نرم‌افزار کاملاً بر عهده کاربر است.** عملیات فلش رام همواره با ریسک‌هایی مانند از دست رفتن اطلاعات، بریک شدن دستگاه (Brick) یا ابطال گارانتی همراه است.
* پیش از شروع کار، از شارژ کافی گوشی مبدا (حداقل ۴۰ تا ۵۰ درصد) و سالم بودن کابل رابط مطمئن شوید.
* *این پروژه یک ابزار مستقل و متن‌باز است و هیچ‌گونه وابستگی یا ارتباط رسمی با هیچ شرکت سازنده موبایلی ندارد.*

</div>

---

<div align="center">

### 🌐 Connect with Teniment · ارتباط با ما

[![YouTube](https://img.shields.io/badge/YouTube-@teniment-FF0000?style=for-the-badge&logo=youtube&logoColor=white)](https://youtube.com/@teniment?si=7e8G_RFO-C4ffA--)
[![Telegram](https://img.shields.io/badge/Telegram-@TenimentApps-26A5E4?style=for-the-badge&logo=telegram&logoColor=white)](https://t.me/TenimentApps)
[![Instagram](https://img.shields.io/badge/Instagram-@tenimenttech-E4405F?style=for-the-badge&logo=instagram&logoColor=white)](https://www.instagram.com/tenimenttech?fqnt=MXV6MzltMmZ6eWlyYw==)
[![Facebook](https://img.shields.io/badge/Facebook-Teniment-1877F2?style=for-the-badge&logo=facebook&logoColor=white)](https://www.facebook.com/share/1A6BybCG6b/)

</div>

---

## 📄 License
Distributed under the **MIT License**. See [`LICENSE`](LICENSE) for more information.

<div align="center">

# FlashOdin - Mobile-to-Mobile OTG Firmware Flasher 📱⚡
### اپلیکیشن فلش رام سامسونگ با موبایل (بدون نیاز به کامپیوتر)

[🇺🇸 English](#-english) | [🇮🇷 فارسی](#-فارسی)

</div>

---

## 🇺🇸 English

**FlashOdin** is a lightweight, open-source Android utility designed to flash firmware packages onto Samsung devices directly from another Android phone using an OTG cable—eliminating the need for a PC.

### ✨ Features
* **PC-Free Flashing:** Flash firmware directly from one Android device to another via USB OTG.
* **No Root Required:** Operates entirely using standard Android USB Host APIs (`android.hardware.usb`) on the host device.
* **Standard Package Support:** Compatible with `.tar` and `.tar.md5` firmware archives (AP, BL, CP, CSC).
* **Safe File Selection:** Uses Android's native Storage Access Framework (SAF) without requiring invasive full-storage permissions.
* **Real-Time Progress & Logs:** Live transfer progress bar and detailed protocol logs during flashing.

### 📋 Prerequisites
1. **Host Device:** An Android phone/tablet (Android 7.0+) with **USB OTG** support.
2. **Target Device:** A compatible Samsung device booted into **Download Mode (Odin Mode)**.
3. **Hardware:** A reliable USB OTG adapter/cable (Type-C to Type-C or OTG adapter + USB data cable).
4. **Firmware:** Valid firmware files downloaded locally on the host device.

### 🚀 How to Use
1. Download the latest APK from the [Releases](https://github.com/MasterTeniment/otg-flasher/releases) section and install it on your **Host** device.
2. Boot your **Target** device into **Download Mode**.
3. Connect both devices using your OTG cable and grant USB permission when prompted.
4. Tap **Select Firmware** to load your `.tar` or `.tar.md5` file(s).
5. Verify the connection status and tap **Start Flash**.
6. Wait until the process finishes and the target device reboots automatically.

### 🛠️ Building from Source
    git clone https://github.com/MasterTeniment/otg-flasher.git
    cd otg-flasher
    ./gradlew assembleDebug

### ⚠️ Disclaimer
* **Use at your own risk.** Flashing firmware carries inherent risks, including data loss, soft/hard bricking, or voiding your device warranty.
* *This project is an independent open-source tool and is not affiliated with, endorsed by, or associated with Samsung Electronics.*

---

<div dir="rtl">

## 🇮🇷 فارسی

**FlashOdin** یک ابزار سبک و متن‌باز (Open-Source) برای اندروید است که به شما امکان می‌دهد فایل‌های رام (Firmware) گوشی‌های سامسونگ را مستقیماً از طریق یک گوشی اندرویدی دیگر و با استفاده از کابل OTG فلش بزنید؛ بدون اینکه نیازی به کامپیوتر یا لپ‌تاپ داشته باشید.

### ✨ ویژگی‌ها و امکانات
* **فلش بدون نیاز به کامپیوتر:** انتقال و فلش مستقیم رام از موبایل به موبایل از طریق پورت USB OTG.
* **بدون نیاز به روت (No Root):** عملکرد کاملاً استاندارد با استفاده از رابط‌های رسمی USB اندروید روی گوشی مبدا.
* **پشتیبانی از فایل‌های استاندارد:** سازگار با پکیج‌های رام با فرمت `.tar` و `.tar.md5` (فایل‌های AP، BL، CP و CSC).
* **انتخاب ایمن فایل‌ها:** استفاده از سیستم فایل‌پیکر استاندارد اندروید (SAF) بدون نیاز به دسترسی‌های حساس به کل حافظه.
* **نمایش زنده روند فلش و لاگ‌ها:** نوار پیشرفت لحظه‌ای به همراه گزارش دقیق مراحل ارتباط با دستگاه.

### 📋 پیش‌نیازها
1. **گوشی مبدا (Host):** یک گوشی یا تبلت اندرویدی (اندروید ۷ به بالا) با پشتیبانی از قابلیت **USB OTG**.
2. **گوشی مقصد (Target):** دستگاه سامسونگ که در حالت **Download Mode (حالت ادین)** قرار گرفته باشد.
3. **کابل رابط:** کابل تبدیل OTG سالم و باکیفیت (تایپ‌سی به تایپ‌سی یا تبدیل OTG همراه با کابل دیتای استاندارد).
4. **فایل رام:** فایل‌های رام معتبر و سازگار با مدل گوشی مقصد که در حافظه گوشی مبدا ذخیره شده باشند.

### 🚀 راهنمای استفاده
1. آخرین نسخه فایل نصب (APK) را از بخش [Releases](https://github.com/MasterTeniment/otg-flasher/releases) دانلود کرده و روی **گوشی مبدا** نصب کنید.
2. **گوشی مقصد** را خاموش کرده و به حالت **Download Mode** ببرید.
3. دو گوشی را با کابل OTG به یکدیگر متصل کنید و دسترسی USB نمایش داده‌شده را تایید نمایید.
4. گزینه **Select Firmware** را بزنید و فایل(های) رام `.tar` یا `.tar.md5` را انتخاب کنید.
5. پس از اطمینان از شناسایی دستگاه، دکمه **Start Flash** را لمس کنید.
6. تا پایان کامل عملیات و ری‌استارت خودکار گوشی مقصد، اتصال کابل را قطع نکنید.

### ⚠️ سلب مسئولیت و نکات ایمنی
* **مسئولیت استفاده از این نرم‌افزار بر عهده کاربر است.** عملیات فلش رام همواره با ریسک‌هایی مانند از دست رفتن اطلاعات، خاموش شدن دستگاه (Brick) یا ابطال گارانتی همراه است.
* پیش از شروع کار، از شارژ کافی گوشی مبدا (حداقل ۵۰ درصد) و سالم بودن کابل رابط مطمئن شوید تا در میانه کار ارتباط قطع نشود.
* *این پروژه یک ابزار مستقل و متن‌باز است و هیچ‌گونه وابستگی یا ارتباطی با شرکت سامسونگ ندارد.*

</div>

---

## 📄 License
Distributed under the **MIT License**. See `LICENSE` for more information.

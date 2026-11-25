package com.otgflashtool.app.i18n

enum class AppLanguage(val code: String) {
    FA("fa"),
    EN("en");

    companion object {
        fun fromCode(code: String?): AppLanguage =
            if (code == "en") EN else FA
    }
}

data class UiStrings(
    val appTitle: String,
    val appSubtitle: String,
    // Bottom nav
    val navFlash: String,
    val navAdb: String,
    val navGuide: String,
    val navLogs: String,
    val navAbout: String,
    // Language dialog
    val langDialogTitle: String,
    val langDialogSubtitle: String,
    val langPersianLabel: String,
    val langEnglishLabel: String,
    // Disclaimer dialog
    val disclaimerTitle: String,
    val disclaimerLead: String,
    val disclaimerBullet1: String,
    val disclaimerBullet2: String,
    val disclaimerBullet3: String,
    val disclaimerCheckbox: String,
    val disclaimerConfirm: String,
    val closeBtn: String,
    // USB Connection card
    val usbConnectedTitle: String,
    val usbStatusTitle: String,
    val usbReadyDefault: String,
    val usbNoDevice: String,
    val usbNoHostWarning: String,
    val usbDevicesFound: (Int) -> String,
    val usbDisconnect: String,
    val usbScanConnect: String,
    val usbConnect: String,
    val usbDetectedProto: (String) -> String,
    val usbConnecting: String,
    val usbDisconnectedMsg: String,
    val usbPermDenied: String,
    val processing: String,
    // 4-file Download Mode tab
    val slotsTitle: String,
    val slotsDesc: String,
    val slotBlTitle: String,
    val slotBlHint: String,
    val slotApTitle: String,
    val slotApHint: String,
    val slotCpTitle: String,
    val slotCpHint: String,
    val slotCscTitle: String,
    val slotCscHint: String,
    val autoRebootLabel: String,
    val readPitBtn: String,
    val startFlashBtn: String,
    val startFlashMultiBtn: (Int) -> String,
    val pitTableTitle: (Int) -> String,
    val flashPassMsg: String,
    val notifFlashTitle: String,
    val notifFlashKeepConnected: String,
    val notifFastbootTitle: (String) -> String,
    // ADB & Fastboot tab
    val adbCardTitle: String,
    val adbCmdLabel: String,
    val adbRunShellBtn: String,
    val adbEmptyOutput: String,
    val adbRebootSent: (String) -> String,
    val fastbootCardTitle: String,
    val fastbootPartLabel: String,
    val fastbootPickImg: String,
    val fastbootFlashBtn: String,
    val fastbootRebootBtn: String,
    val fastbootFlashDone: (String) -> String,
    val fastbootOemSectionTitle: String,
    val fastbootOemSectionDesc: String,
    val fastbootCheckUnlockBtn: String,
    val fastbootUnlockBtn: String,
    val fastbootLockBtn: String,
    val fastbootOemConfirmTitle: String,
    val fastbootOemUnlockConfirmBody: String,
    val fastbootOemLockConfirmBody: String,
    val fastbootOemConfirmBtn: String,
    // Magisk & ADB Sideload toolkit
    val magiskCardTitle: String,
    val magiskCardDesc: String,
    val magiskDownloadBtn: String,
    val magiskConvertBtn: String,
    val magiskConvertDone: (String) -> String,
    val magiskInvalidApkError: String,
    val sideloadTitle: String,
    val sideloadPickZip: String,
    val sideloadFlashBtn: String,
    val sideloadDoneMsg: String,
    // Guide tab
    val guide1Title: String,
    val guide1Host: String,
    val guide1Target: String,
    val guide1Note: String,
    val guideDevTitle: String,
    val guideDevBody: String,
    val guideDevToggleTitle: String,
    val guideDevToggleSubtitle: String,
    val guideDevSteps: String,
    val guide2Title: String,
    val guide2Body: String,
    val guide3Title: String,
    val guide3Body: String,
    val guide4Title: String,
    val guide4Body: String,
    val guide5Title: String,
    val guide5Body: String,
    // Logs tab
    val logsTitle: (Int) -> String,
    val copyBtn: String,
    val clearBtn: String,
    val logsCopiedMsg: String,
    // About & Settings tab
    val settingsCardTitle: String,
    val settingsLangLabel: String,
    val aboutTitle: String,
    val aboutVersionLine: (String) -> String,
    val aboutDesc: String,
    val githubCardTitle: String,
    val githubOpenRepo: String,
    val githubOpenReleases: String,
    val socialCardTitle: String,
    val socialCardSubtitle: String,
    val contactEmailLabel: String,
    val contactEmailCopied: String,
    val complianceCardTitle: String,
    val complianceBody: String,
    val showDisclaimerAgain: String
)

object AppStrings {
    private val fa = UiStrings(
        appTitle = "فلش‌تول OTG",
        appSubtitle = "Mobile USB Firmware Flasher · ADB · Fastboot",
        navFlash = "فلش",
        navAdb = "ADB",
        navGuide = "راهنما",
        navLogs = "گزارش",
        navAbout = "تنظیمات",
        langDialogTitle = "انتخاب زبان / Choose Language",
        langDialogSubtitle = "لطفاً زبان برنامه را انتخاب کنید (بعداً در تنظیمات قابل تغییر است):\nPlease select your preferred app language:",
        langPersianLabel = "فارسی (Persian)",
        langEnglishLabel = "English (انگلیسی)",
        disclaimerTitle = "هشدار ایمنی و سلب مسئولیت حرفه‌ای",
        disclaimerLead = "این اپلیکیشن یک ابزار تخصصی USB OTG برای توسعه‌دهندگان و تعمیرکاران حرفه‌ای است.",
        disclaimerBullet1 = "• تمام مسئولیت بریک شدن (Brick) دستگاه مقصد، از دست رفتن اطلاعات، نصب فایل رام اشتباه یا ابطال گارانتی، کاملاً بر عهده کاربر است.",
        disclaimerBullet2 = "• این برنامه هیچ تغییری در سیستم‌عامل گوشی میزبان (Host) ایجاد نمی‌کند، نیاز به Root ندارد و هیچ فایل رامی را میزبانی یا دانلود نمی‌کند.",
        disclaimerBullet3 = "• تمام عملیات فقط از طریق رابط رسمی Android USB Host API (UsbManager) روی دستگاه خارجی متصل به کابل OTG انجام می‌شود.",
        disclaimerCheckbox = "شرایط بالا را خواندم و مسئولیت استفاده روی دستگاه خارجی را می‌پذیرم.",
        disclaimerConfirm = "تایید و ادامه",
        closeBtn = "بستن",
        usbConnectedTitle = "اتصال سخت‌افزاری USB برقرار است",
        usbStatusTitle = "وضعیت پورت USB OTG",
        usbReadyDefault = "آماده اتصال — سر OTG به گوشی مبدا و سر کابل شارژر به گوشی مقصد وصل شود",
        usbNoDevice = "دستگاهی روی پورت OTG یافت نشد — جهت کابل OTG را بررسی کنید",
        usbNoHostWarning = "هشدار: این گوشی میزبان ویژگی سخت‌افزاری USB OTG Host را گزارش نکرده است",
        usbDevicesFound = { n -> "$n دستگاه خارجی USB شناسایی شد — برای اتصال لمس کنید" },
        usbDisconnect = "قطع",
        usbScanConnect = "اسکن و اتصال",
        usbConnect = "اتصال",
        usbDetectedProto = { p -> "پروتکل تشخیص‌داده‌شده: $p" },
        usbConnecting = "در حال اتصال و تشخیص خودکار پروتکل...",
        usbDisconnectedMsg = "اتصال قطع شد",
        usbPermDenied = "مجوز دسترسی به پورت USB رد شد",
        processing = "در حال پردازش...",
        slotsTitle = "اسلات‌های فایل رام ۴ فایله (Download Mode)",
        slotsDesc = "فایل‌های محلی خود (.tar، .tar.md5 یا .lz4) را از حافظه انتخاب کنید. پشتیبانی کامل از پروتکل‌های v2 (مثل GT-N7100) و v3.",
        slotBlTitle = "بوت‌لودر",
        slotBlHint = "فایل BL_...tar.md5",
        slotApTitle = "سیستم و کرنل",
        slotApHint = "فایل AP_...tar.md5",
        slotCpTitle = "مودم و آنتن",
        slotCpHint = "فایل CP_...tar.md5",
        slotCscTitle = "منطقه و تنظیمات",
        slotCscHint = "فایل CSC یا HOME_CSC",
        autoRebootLabel = "ریبوت خودکار پس از پایان فلش",
        readPitBtn = "خواندن PIT",
        startFlashBtn = "شروع فلش",
        startFlashMultiBtn = { n -> "شروع فلش ($n)" },
        pitTableTitle = { n -> "جدول پارتیشن PIT گوشی مقصد ($n پارتیشن)" },
        flashPassMsg = "PASS! همه فایل‌ها با موفقیت فلش شدند",
        notifFlashTitle = "فلش USB OTG",
        notifFlashKeepConnected = "کابل OTG را تا پایان عملیات جدا نکنید",
        notifFastbootTitle = { p -> "فلش Fastboot ($p)" },
        adbCardTitle = "کنترل ADB (انتقال به دانلود مود و Shell)",
        adbCmdLabel = "دستور ADB Shell",
        adbRunShellBtn = "اجرای دستور Shell",
        adbEmptyOutput = "(خروجی خالی)",
        adbRebootSent = { t -> "دستور انتقال به $t ارسال شد" },
        fastbootCardTitle = "فلشر Fastboot (شیائومی، پیکسل، وان‌پلاس...)",
        fastbootPartLabel = "نام پارتیشن (مثلاً boot, recovery, vbmeta)",
        fastbootPickImg = "انتخاب فایل ایمیج (.img)",
        fastbootFlashBtn = "فلش پارتیشن",
        fastbootRebootBtn = "ریبوت",
        fastbootFlashDone = { p -> "پارتیشن $p با موفقیت فلش شد" },
        fastbootOemSectionTitle = "باز کردن رسمی بوت‌لودر (OEM Unlocking)",
        fastbootOemSectionDesc = "فقط زمانی کار می‌کند که مالک دستگاه قبلاً گزینه OEM unlocking را در Developer options روشن کرده باشد. این دستور رسمی اندروید باعث پاک شدن کامل اطلاعات (Factory Reset) می‌شود.",
        fastbootCheckUnlockBtn = "بررسی وضعیت قفل",
        fastbootUnlockBtn = "باز کردن (flashing unlock)",
        fastbootLockBtn = "قفل مجدد (flashing lock)",
        fastbootOemConfirmTitle = "تایید دستور رسمی بوت‌لودر",
        fastbootOemUnlockConfirmBody = "دستور رسمی «fastboot flashing unlock» به گوشی مقصد ارسال می‌شود.\n\n• ابتدا باید گزینه OEM unlocking توسط مالک دستگاه در Developer options فعال شده باشد.\n• تایید این عملیات روی صفحه گوشی مقصد باعث پاک شدن کامل اطلاعات (Factory Reset) می‌شود.",
        fastbootOemLockConfirmBody = "دستور رسمی «fastboot flashing lock» به گوشی مقصد ارسال می‌شود.\n\n• فقط روی رام رسمی (Stock) اجرا کنید؛ قفل کردن بوت‌لودر روی رام یا ریکاوری غیررسمی می‌تواند باعث بوت‌لوپ شود و اطلاعات را پاک می‌کند.",
        fastbootOemConfirmBtn = "ارسال دستور",
        magiskCardTitle = "ابزار روت با Magisk (دانلود رسمی، تبدیل به ZIP و Sideload)",
        magiskCardDesc = "Magisk یک ابزار متن‌باز مدیریت روت، ماژول‌ها و Zygisk است. آخرین نسخه رسمی را از گیت‌هاب دانلود کنید، فایل .apk را به .zip قابل فلش در ریکاوری کاستوم تبدیل کنید و از طریق ADB Sideload فلش بزنید (یا boot/init_boot پچ‌شده را با Fastboot فلش کنید).",
        magiskDownloadBtn = "دریافت آخرین نسخه Magisk از گیت‌هاب",
        magiskConvertBtn = "تبدیل Magisk.apk به فایل فلش‌شدنی (.zip)",
        magiskConvertDone = { name -> "فایل فلش‌شدنی $name با موفقیت ساخته شد" },
        magiskInvalidApkError = "فایل انتخاب‌شده یک APK/ZIP معتبر نیست (هدر PK یافت نشد).",
        sideloadTitle = "فلش ZIP در ریکاوری کاستوم (ADB Sideload)",
        sideloadPickZip = "انتخاب فایل زیپ (.zip) برای Sideload",
        sideloadFlashBtn = "ارسال و فلش (ADB Sideload)",
        sideloadDoneMsg = "ADB Sideload با موفقیت کامل شد",
        guide1Title = "۱. جهت صحیح وصل کردن کابل OTG (بسیار مهم)",
        guide1Host = "• گوشی مبدا (گوشی که فایل را می‌ریزد): سر آداپتور OTG باید به این گوشی وصل شود.",
        guide1Target = "• گوشی مقصد (گوشی که فایل رویش نصب می‌شود): سر کابل شارژر عادی باید به این گوشی وصل شود.",
        guide1Note = "نکته: اگر سر OTG را برعکس به گوشی مقصد بزنید، گوشی مبدا هیچ دستگاهی را روی پورت USB نخواهد دید.",
        guideDevTitle = "۲. فعال‌سازی Developer Options و USB Debugging",
        guideDevBody = "• برای استفاده از بخش ADB (انتقال به دانلود مود/ریکاورى با یک کلیک یا دستورات Shell)، فعال کردن گزینه‌های توسعه‌دهنده (Developer Options) و اشکال‌زدایی USB (USB Debugging) در تنظیمات گوشی مقصد ضروری است.\n" +
            "• برای فلش مستقیم در حالت Download Mode الزام قطعی نیست، اما ترجیحاً قبل از شروع این دو گزینه را در تنظیمات گوشی مقصد فعال کنید تا در صورت نیاز به ریبوت از طریق ADB یا بررسی قفل OEM به مشکل نخورید.",
        guideDevToggleTitle = "بلد نیستید Developer Options و USB Debugging را فعال کنید؟",
        guideDevToggleSubtitle = "برای مشاهده آموزش گام‌به‌گام لمس کنید",
        guideDevSteps = "۱) وارد تنظیمات (Settings) گوشی شوید و به پایین بروید و روی «درباره تلفن» (About phone) بزنید.\n" +
            "۲) وارد «اطلاعات نرم‌افزار» (Software information) شوید (در برخی گوشی‌ها گزینه بعدی همین صفحه است).\n" +
            "۳) روی «شماره ساخت» (Build number) ۷ بار پشت سر هم بزنید تا پیام «حالت توسعه‌دهنده فعال شد» ظاهر شود (در صورت درخواست، رمز یا الگوی گوشی را وارد کنید).\n" +
            "۴) به صفحه اصلی تنظیمات برگردید؛ در پایین‌ترین قسمت، منوی جدید «گزینه‌های توسعه‌دهنده» (Developer options) اضافه شده است. وارد آن شوید.\n" +
            "۵) گزینه «اشکال‌زدایی USB» (USB debugging) را روشن کنید و OK بزنید.\n" +
            "۶) هنگام اتصال کابل OTG در حالت روشن (ADB)، پیغام «Allow USB debugging?» روی صفحه گوشی مقصد ظاهر می‌شود؛ تیک Always allow را بزنید و Allow را لمس کنید.",
        guide2Title = "۳. نکات مهم برای گوشی‌های Micro-USB و مدل‌های قدیمی‌تر",
        guide2Body = "• برخی گوشی‌های Micro-USB (مثل J7 Prime / SM-G610F) هنگام میزبان بودن (Host)، جریان خروجی پورت USB ضعیف‌تری نسبت به گوشی‌های Type-C (مثل A13 / SM-A137F) دارند. اگر گوشی دوم شناسایی نشد:\n" +
            "  ۱) شارژ گوشی مبدا حداقل بالای ۵۰٪ باشد.\n" +
            "  ۲) در تنظیمات گوشی مبدا چک کنید گزینه OTG Connection روشن باشد.\n" +
            "  ۳) از تبدیل OTG سالم با پین ID استاندارد (یا کابل OTG برق‌دار Y-Cable) استفاده کنید.\n" +
            "• برای مدل‌های قدیمی‌تر (مثل Note 2 / GT-N7100 با پروتکل v2)، برنامه به‌طور خودکار اندازه سکانس را روی ۸ مگابایت تنظیم کرده و خطاهای بافر USB را پاک‌سازی می‌کند.",
        guide3Title = "۴. مراحل صحیح فلش ۴ فایله (Download Mode)",
        guide3Body = "۱) گوشی مقصد را خاموش کنید و وارد Download Mode شوید. پس از دیدن صفحه هشدار آبی، حتماً یک‌بار «ولوم بالا» (Continue) را بزنید تا عبارت Downloading... نمایش داده شود.\n" +
            "۲) کابل OTG را مطابق جهت مرحله ۱ وصل کنید و دکمه «اسکن و اتصال» را بزنید تا جدول پارتیشن PIT خوانده شود.\n" +
            "۳) فایل‌های BL (بوت‌لودر)، AP (سیستم)، CP (مودم) و CSC (منطقه) را انتخاب کنید. استفاده از HOME_CSC معمولاً اطلاعات کاربری را حفظ می‌کند ولی CSC عادی گوشی را ریست فکتوری می‌کند.\n" +
            "۴) تا پایان پیام PASS! کابل را جدا نکنید. اگر وسط فلش خطایی رخ داد، قبل از تلاش مجدد حتماً گوشی مقصد را با «ولوم پایین + پاور (۷ ثانیه)» ریستارت کرده و مجدد وارد Downloading... شوید.",
        guide4Title = "۵. راهنمای ADB، Fastboot و OEM Unlocking",
        guide4Body = "• برای ADB: در گوشی مقصد USB Debugging را روشن کنید و پس از اتصال، پیغام Allow USB Debugging را روی صفحه گوشی مقصد تایید کنید. سپس می‌توانید با یک کلیک گوشی را به Download Mode، Fastboot یا Recovery ببرید.\n" +
            "• برای Fastboot: گوشی مقصد را در حالت Fastboot قرار دهید، نام پارتیشن (مثلاً boot یا recovery) و فایل .img را انتخاب کرده و فلش کنید.\n" +
            "• برای OEM Unlocking رسمی: ابتدا در گوشی مقصد وارد Settings → Developer options شوید و گزینه «OEM unlocking» را با رمز خودتان روشن کنید، سپس گوشی را به Fastboot ببرید و از دکمه «بررسی وضعیت قفل» یا «flashing unlock» استفاده کنید (تایید نهایی روی صفحه گوشی مقصد انجام شده و باعث ریست فکتوری استاندارد اندروید می‌شود).",
        guide5Title = "۶. راهنمای روت با Magisk Manager (تبدیل APK به ZIP، Sideload و Fastboot)",
        guide5Body = "• ویژگی‌های Magisk: مدیریت دسترسی Root برنامه‌ها، نصب ماژول‌های Magisk و قابلیت Zygisk (متن‌باز در گیت‌هاب topjohnwu/Magisk).\n" +
            "• پیش‌نیاز مهم: نصب فایل APK به‌تنهایی گوشی را روت نمی‌کند! بوت‌لودر گوشی مقصد حتماً باید آنلاک باشد و روش نصب به ریکاوری یا نسخه اندروید دستگاه بستگی دارد.\n" +
            "• روش ۱ (دارای ریکاوری کاستوم مثل TWRP / OrangeFox):\n" +
            "  ۱) از تب ADB دکمه «دریافت آخرین نسخه Magisk از گیت‌هاب» را بزنید و فایل Magisk-vXX.X.apk را دانلود کنید.\n" +
            "  ۲) دکمه «تبدیل Magisk.apk به فایل فلش‌شدنی (.zip)» را بزنید تا نسخه .zip آماده شود.\n" +
            "  ۳) گوشی مقصد را به ریکاوری کاستوم ببرید (حالت ADB Sideload) و با کابل OTG از بخش «ADB Sideload» داخل همین برنامه فایل زیپ را فلش کنید (یا داخل TWRP انتخاب و فلش نمایید).\n" +
            "• روش ۲ (بدون ریکاوری کاستوم — گوشی‌های جدیدتر با Fastboot):\n" +
            "  فایل boot.img (یا init_boot.img در اندروید ۱۳ به بالا) را داخل برنامه Magisk پچ کنید و سپس فایل magisk_patched را در بخش Fastboot همین برنامه روی پارتیشن boot یا init_boot فلش بزنید.\n" +
            "⚠️ هشدار: روت کردن ممکن است برخی برنامه‌های بانکی را از کار بیندازد یا در صورت فایل اشتباه باعث مشکل بوت شود؛ حتماً قبل از شروع بکاپ تهیه کنید.",
        logsTitle = { n -> "گزارش زنده USB ($n)" },
        copyBtn = "کپی",
        clearBtn = "پاک",
        logsCopiedMsg = "گزارش در حافظه کپی شد",
        settingsCardTitle = "تنظیمات زبان (Language)",
        settingsLangLabel = "زبان رابط کاربری برنامه را انتخاب کنید:",
        aboutTitle = "فلش‌تول OTG — OTG FlashTool",
        aboutVersionLine = { v -> "نسخه $v · ابزار متن‌باز فلش موبایل به موبایل بدون نیاز به کامپیوتر" },
        aboutDesc = "توسعه‌یافته با Kotlin و Jetpack Compose برای ارتباط مستقیم با دستگاه دوم از طریق رابط استاندارد USB Host اندروید (Download Mode v2/v3، ADB و Fastboot).",
        githubCardTitle = "سورس‌کد و دریافت آپدیت‌ها (GitHub)",
        githubOpenRepo = "صفحه گیت‌هاب",
        githubOpenReleases = "نسخه‌های APK",
        socialCardTitle = "ارتباط با ما و شبکه‌های اجتماعی (Teniment)",
        socialCardSubtitle = "برای آموزش‌های ویدیویی، نسخه‌های جدید و پشتیبانی ما را دنبال کنید:",
        contactEmailLabel = "ایمیل پشتیبانی و توسعه‌دهنده:",
        contactEmailCopied = "ایمیل در حافظه کپی شد",
        complianceCardTitle = "استانداردهای امنیتی و انطباق با گوگل پلی",
        complianceBody = "• استفاده انحصاری از Android USB Host API رسمی (بدون نیاز به Root و بدون تغییر در سیستم‌عامل گوشی مبدا).\n" +
            "• انتخاب فایل‌ها از طریق Storage Access Framework (SAF) — بدون درخواست مجوز حساس MANAGE_EXTERNAL_STORAGE.\n" +
            "• مجهز به Foreground Service با نوتیفیکیشن شفاف پیشرفت برای جلوگیری از توقف وسط فلش.\n" +
            "• بدون میزبانی یا ارائه لینک دانلود رام‌های دارای کپیرایت — کاربر فایل محلی خود را انتخاب می‌کند.\n" +
            "• برندینگ مستقل و توصیفی بدون استفاده از علائم تجاری ثبت‌شده.",
        showDisclaimerAgain = "مشاهده مجدد هشدار ایمنی و سلب مسئولیت"
    )

    private val en = UiStrings(
        appTitle = "OTG FlashTool",
        appSubtitle = "Mobile USB Firmware Flasher · ADB · Fastboot",
        navFlash = "Flash",
        navAdb = "ADB",
        navGuide = "Guide",
        navLogs = "Logs",
        navAbout = "Settings",
        langDialogTitle = "Choose Language / انتخاب زبان",
        langDialogSubtitle = "Select your preferred app language (can be changed anytime in Settings):",
        langPersianLabel = "فارسی (Persian)",
        langEnglishLabel = "English",
        disclaimerTitle = "Safety Warning & Liability Disclaimer",
        disclaimerLead = "This application is an advanced USB OTG utility designed for developers and technicians.",
        disclaimerBullet1 = "• You accept full responsibility for any device bricking, data loss, wrong firmware selection, or voided warranty on the target device.",
        disclaimerBullet2 = "• This app makes zero modifications to the host device OS, requires no Root access, and neither hosts nor distributes any copyrighted firmware files.",
        disclaimerBullet3 = "• All operations run strictly over the official Android USB Host API (UsbManager) to an external target device connected via OTG.",
        disclaimerCheckbox = "I have read the terms above and accept full responsibility for flashing an external device.",
        disclaimerConfirm = "Accept & Continue",
        closeBtn = "Close",
        usbConnectedTitle = "USB Hardware Connected",
        usbStatusTitle = "USB OTG Port Status",
        usbReadyDefault = "Ready — connect OTG adapter to host phone and regular USB cable to target phone",
        usbNoDevice = "No USB device detected — verify OTG cable direction",
        usbNoHostWarning = "Warning: This host phone does not report Android USB OTG Host support",
        usbDevicesFound = { n -> "$n external USB device(s) detected — tap to connect" },
        usbDisconnect = "Disconnect",
        usbScanConnect = "Scan & Connect",
        usbConnect = "Connect",
        usbDetectedProto = { p -> "Detected protocol: $p" },
        usbConnecting = "Connecting & auto-detecting protocol...",
        usbDisconnectedMsg = "Disconnected",
        usbPermDenied = "USB permission was denied",
        processing = "Processing...",
        slotsTitle = "4-File Firmware Slots (Download Mode)",
        slotsDesc = "Pick local .tar, .tar.md5, or .lz4 firmware files via Android SAF. Supports both protocol v2 (e.g. GT-N7100) and v3.",
        slotBlTitle = "Bootloader",
        slotBlHint = "BL_...tar.md5 file",
        slotApTitle = "System & Kernel",
        slotApHint = "AP_...tar.md5 file",
        slotCpTitle = "Modem / Baseband",
        slotCpHint = "CP_...tar.md5 file",
        slotCscTitle = "Region & Carrier",
        slotCscHint = "CSC or HOME_CSC file",
        autoRebootLabel = "Auto Reboot after flashing completes",
        readPitBtn = "Read PIT",
        startFlashBtn = "Start Flash",
        startFlashMultiBtn = { n -> "Flash ($n files)" },
        pitTableTitle = { n -> "Target Device PIT Table ($n partitions)" },
        flashPassMsg = "PASS! All selected firmware slots flashed successfully",
        notifFlashTitle = "USB OTG Flashing",
        notifFlashKeepConnected = "Do not disconnect the OTG cable until flashing finishes",
        notifFastbootTitle = { p -> "Fastboot Flash ($p)" },
        adbCardTitle = "ADB Controls (Reboot Modes & Shell)",
        adbCmdLabel = "ADB Shell Command",
        adbRunShellBtn = "Run Shell Command",
        adbEmptyOutput = "(empty output)",
        adbRebootSent = { t -> "Reboot command sent to: $t" },
        fastbootCardTitle = "Fastboot Flasher (Pixel, Xiaomi, OnePlus...)",
        fastbootPartLabel = "Partition name (e.g. boot, recovery, vbmeta)",
        fastbootPickImg = "Select Image File (.img)",
        fastbootFlashBtn = "Flash Partition",
        fastbootRebootBtn = "Reboot",
        fastbootFlashDone = { p -> "Partition $p flashed successfully" },
        fastbootOemSectionTitle = "Official Bootloader / OEM Unlocking",
        fastbootOemSectionDesc = "Requires the device owner to first enable 'OEM unlocking' in Developer options. Official Android bootloader unlock performs a mandatory factory data wipe.",
        fastbootCheckUnlockBtn = "Check Lock Status",
        fastbootUnlockBtn = "Unlock (flashing unlock)",
        fastbootLockBtn = "Re-Lock (flashing lock)",
        fastbootOemConfirmTitle = "Confirm Official Bootloader Command",
        fastbootOemUnlockConfirmBody = "This sends the standard Android 'fastboot flashing unlock' command to the target device.\n\n• 'OEM unlocking' must already be enabled by the owner in Developer options.\n• Confirming on the target screen wipes all user data (factory reset).",
        fastbootOemLockConfirmBody = "This sends the standard Android 'fastboot flashing lock' command to the target device.\n\n• Only re-lock with stock signed firmware; locking with custom images may cause a bootloop and wipes user data.",
        fastbootOemConfirmBtn = "Send Command",
        magiskCardTitle = "Magisk Root Toolkit (Official GitHub, APK → ZIP & Sideload)",
        magiskCardDesc = "Magisk is an open-source root management suite supporting root permissions, modules, and Zygisk. Download the latest official release from GitHub, convert .apk into a custom-recovery flashable .zip, and flash via ADB Sideload (or flash patched boot/init_boot in Fastboot).",
        magiskDownloadBtn = "Get Latest Official Magisk (GitHub)",
        magiskConvertBtn = "Convert Magisk.apk to Flashable .zip",
        magiskConvertDone = { name -> "Flashable $name saved successfully" },
        magiskInvalidApkError = "Selected file is not a valid APK/ZIP archive (missing PK signature).",
        sideloadTitle = "Custom Recovery Flash (ADB Sideload .zip)",
        sideloadPickZip = "Select Flashable ZIP (.zip)",
        sideloadFlashBtn = "Flash via ADB Sideload",
        sideloadDoneMsg = "ADB Sideload completed successfully",
        guide1Title = "1. Correct OTG Cable Direction (Critical)",
        guide1Host = "• Host Phone (sending firmware files): Plug the OTG adapter end into THIS phone.",
        guide1Target = "• Target Phone (receiving firmware): Plug the regular USB charging cable end into THIS phone.",
        guide1Note = "Note: Plugging the OTG adapter into the target phone instead of the host phone will prevent USB detection.",
        guideDevTitle = "2. Enable Developer Options & USB Debugging",
        guideDevBody = "• For ADB features (1-click reboot to Download/Fastboot/Recovery or Shell), enabling Developer Options and USB Debugging in the target phone's Settings is required.\n" +
            "• For direct Download Mode flashing it is not strictly required once already in Download Mode, but it is strongly recommended to enable both beforehand so you can reboot via ADB or check OEM Unlocking if needed.",
        guideDevToggleTitle = "Don't know how to enable Developer Options & USB Debugging?",
        guideDevToggleSubtitle = "Tap here to expand the step-by-step tutorial",
        guideDevSteps = "1) Open Settings on the target phone and scroll down to 'About phone'.\n" +
            "2) Tap 'Software information' (on some phones Build number is directly inside About phone).\n" +
            "3) Tap 'Build number' 7 times in a row until 'Developer mode has been turned on' appears (enter your PIN/pattern if prompted).\n" +
            "4) Return to the main Settings screen — a new 'Developer options' menu now appears at the bottom. Open it.\n" +
            "5) Switch ON 'USB debugging' and confirm OK.\n" +
            "6) When connecting via OTG for ADB, tap 'Allow' on the 'Allow USB debugging?' dialog on the target phone.",
        guide2Title = "3. Tips for Micro-USB Hosts & Legacy Targets",
        guide2Body = "• Some Micro-USB host phones (e.g. SM-G610F / J7 Prime) supply lower USB OTG power than USB Type-C hosts (e.g. SM-A137F). If the target device is not detected:\n" +
            "  1) Keep host battery above 50%.\n" +
            "  2) Ensure OTG Connection is enabled in host Settings.\n" +
            "  3) Use a quality OTG adapter (or a powered Y-OTG cable).\n" +
            "• For legacy v2 targets (such as GT-N7100 / Note 2), the engine automatically switches to safe 8 MB sequences and clears USB endpoint halts.",
        guide3Title = "4. Flashing 4-File Firmware (Download Mode)",
        guide3Body = "1) Power off the target phone and boot into Download Mode. At the warning screen, press Volume Up (Continue) so 'Downloading...' is displayed.\n" +
            "2) Connect the OTG cable using the direction in Step 1, then tap 'Scan & Connect' to read the PIT table.\n" +
            "3) Select BL, AP, CP, and CSC files. HOME_CSC usually preserves user data, while regular CSC performs a factory wipe.\n" +
            "4) Never unplug until PASS! appears. If a flash fails midway, force-reboot the target phone (Volume Down + Power for 7s) and re-enter 'Downloading...' before retrying.",
        guide4Title = "5. ADB, Fastboot & OEM Unlocking Guide",
        guide4Body = "• ADB: Enable USB Debugging on the target phone, connect via OTG, and tap 'Allow USB Debugging' on the target screen. You can then reboot straight into Download Mode, Fastboot, or Recovery.\n" +
            "• Fastboot: Boot the target into Fastboot mode, enter the target partition (e.g. boot or recovery), pick the .img file, and flash.\n" +
            "• Official OEM Unlocking: On the target phone go to Settings → Developer options and switch ON 'OEM unlocking' with your PIN/password, then reboot to Fastboot and use 'Check Lock Status' or 'flashing unlock' (confirm on the target phone; Android automatically factory-resets user data for security).",
        guide5Title = "6. Magisk Root Guide (APK → ZIP, ADB Sideload & Fastboot)",
        guide5Body = "• About Magisk: Open-source root manager supporting root permissions, Magisk modules, and Zygisk (github.com/topjohnwu/Magisk).\n" +
            "• Important: Simply installing the Magisk APK does NOT root the phone. Your target bootloader must be unlocked, and installation depends on your device/recovery.\n" +
            "• Method 1 (Custom Recovery e.g. TWRP / OrangeFox):\n" +
            "  1) Tap 'Get Latest Official Magisk (GitHub)' in the ADB tab to download the latest Magisk-vXX.X.apk.\n" +
            "  2) Tap 'Convert Magisk.apk to Flashable .zip' to export a valid .zip archive.\n" +
            "  3) Boot the target phone into Custom Recovery → ADB Sideload mode, then flash the .zip directly from this app's ADB Sideload section (or install inside TWRP).\n" +
            "• Method 2 (No Custom Recovery — Modern Android via Fastboot):\n" +
            "  Patch your stock boot.img (or init_boot.img on Android 13+) inside the Magisk app, then flash magisk_patched.img to 'boot' or 'init_boot' in the Fastboot section above.\n" +
            "⚠️ Warning: Rooting may break banking/Play Integrity apps or cause boot issues if the wrong image is flashed. Always back up important data first.",
        logsTitle = { n -> "Live USB Hardware Logs ($n)" },
        copyBtn = "Copy",
        clearBtn = "Clear",
        logsCopiedMsg = "Logs copied to clipboard",
        settingsCardTitle = "Language Settings / تنظیمات زبان",
        settingsLangLabel = "Choose interface language:",
        aboutTitle = "OTG FlashTool",
        aboutVersionLine = { v -> "Version $v · Open-Source Mobile-to-Mobile USB Flasher" },
        aboutDesc = "Built with Kotlin & Jetpack Compose for direct communication with an external target device via official Android USB Host API (Download Mode v2/v3, ADB & Fastboot).",
        githubCardTitle = "Source Code & APK Updates (GitHub)",
        githubOpenRepo = "GitHub Repo",
        githubOpenReleases = "APK Releases",
        socialCardTitle = "Follow Us & Community (Teniment)",
        socialCardSubtitle = "Follow our official channels for tutorials, updates, and apps:",
        contactEmailLabel = "Developer & Support Email:",
        contactEmailCopied = "Email copied to clipboard",
        complianceCardTitle = "Security & Google Play Compliance",
        complianceBody = "• Uses official Android USB Host API (UsbManager) exclusively — no Root required and zero host OS changes.\n" +
            "• Uses Storage Access Framework (SAF / ACTION_OPEN_DOCUMENT) — no MANAGE_EXTERNAL_STORAGE permission.\n" +
            "• Runs an Android Foreground Service (dataSync) with live progress notification to prevent mid-flash termination.\n" +
            "• Does not host or link to any copyrighted ROM/firmware files — users select their own local files.\n" +
            "• Independent, descriptive naming with no third-party trademarks.",
        showDisclaimerAgain = "Review Safety Warning & Disclaimer"
    )

    fun of(lang: AppLanguage): UiStrings = if (lang == AppLanguage.EN) en else fa
}

package com.otgflashtool.app

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material.icons.filled.UsbOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.otgflashtool.app.i18n.AppLanguage
import com.otgflashtool.app.i18n.AppStrings
import com.otgflashtool.app.i18n.UiStrings
import com.otgflashtool.app.protocol.AdbProtocol
import com.otgflashtool.app.protocol.DownloadModeProtocol
import com.otgflashtool.app.protocol.DownloadModeSlotInput
import com.otgflashtool.app.protocol.FastbootProtocol
import com.otgflashtool.app.protocol.PitEntry
import com.otgflashtool.app.service.FlashForegroundService
import com.otgflashtool.app.usb.DetectedProtocol
import com.otgflashtool.app.usb.NativeUsbCore
import com.otgflashtool.app.usb.UsbBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val ACTION_USB_PERMISSION = "com.otgflashtool.app.USB_PERMISSION"
private const val PREFS_NAME = "otg_flashtool_prefs"
private const val KEY_DISCLAIMER_ACCEPTED = "disclaimer_accepted_v1"
private const val KEY_LANGUAGE = "app_language_code"
private const val KEY_LANGUAGE_CHOSEN = "app_language_chosen"
private const val GITHUB_REPO_URL = "https://github.com/MasterTeniment/otg-flasher"
private const val GITHUB_RELEASES_URL = "https://github.com/MasterTeniment/otg-flasher/releases"
private const val YOUTUBE_URL = "https://youtube.com/@teniment?si=7e8G_RFO-C4ffA--"
private const val INSTAGRAM_URL = "https://www.instagram.com/tenimenttech?fqnt=MXV6MzltMmZ6eWlyYw=="
private const val FACEBOOK_URL = "https://www.facebook.com/share/1A6BybCG6b/"
private const val TELEGRAM_URL = "https://t.me/TenimentApps"
private const val MAGISK_RELEASES_URL = "https://github.com/topjohnwu/Magisk/releases/latest"
private const val DEVELOPER_EMAIL = "masterteniment@gmail.com"
private const val APP_VERSION_LABEL = "v2.8.1"

data class PickedFile(
    val uri: Uri,
    val name: String,
    val size: Long,
    val enabled: Boolean = true
)

private data class FirmwareSlotMeta(
    val key: String,
    val accent: Color
)

private val FIRMWARE_SLOTS = listOf(
    FirmwareSlotMeta("BL", Color(0xFF60A5FA)),
    FirmwareSlotMeta("AP", Color(0xFF34D399)),
    FirmwareSlotMeta("CP", Color(0xFFFBBF24)),
    FirmwareSlotMeta("CSC", Color(0xFFC084FC))
)

class MainActivity : ComponentActivity() {
    private lateinit var usbManager: UsbManager
    private val logs = mutableStateListOf<String>()
    private val attachedDevices = mutableStateListOf<UsbDevice>()
    private var activeBinding by mutableStateOf<UsbBinding?>(null)
    private var activeAdb by mutableStateOf<AdbProtocol?>(null)
    private var activeDownloadMode by mutableStateOf<DownloadModeProtocol?>(null)
    private var activeFastboot by mutableStateOf<FastbootProtocol?>(null)
    private var pitEntries by mutableStateOf<List<PitEntry>>(emptyList())
    private var statusMessage by mutableStateOf("")
    private var isBusy by mutableStateOf(false)
    private var progressFraction by mutableFloatStateOf(0f)
    private var progressLabel by mutableStateOf("")
    private var hasUsbHostFeature by mutableStateOf(true)

    private var currentLanguage by mutableStateOf(AppLanguage.FA)
    private var showLanguagePickerDialog by mutableStateOf(false)
    private var showDisclaimerDialog by mutableStateOf(false)
    private var disclaimerAlreadyAccepted by mutableStateOf(false)

    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.US)

    private fun strings(): UiStrings = AppStrings.of(currentLanguage)

    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                ACTION_USB_PERMISSION -> {
                    val device = extractUsbDevice(intent)
                    val granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
                    if (granted && device != null) {
                        addLog("USB Host permission granted: ${NativeUsbCore.describeDevice(device)}")
                        connectToDevice(device)
                    } else {
                        addLog("USB permission denied by user.")
                        statusMessage = strings().usbPermDenied
                    }
                }

                UsbManager.ACTION_USB_DEVICE_ATTACHED -> {
                    val device = extractUsbDevice(intent)
                    refreshUsbDevices()
                    if (device != null) {
                        val alreadyBound = activeBinding?.device?.deviceId == device.deviceId
                        if (alreadyBound) {
                            addLog("External USB device attached (session already active): ${NativeUsbCore.describeDevice(device)}")
                        } else {
                            addLog("External USB device attached: ${NativeUsbCore.describeDevice(device)}")
                            requestPermissionAndConnect(device)
                        }
                    }
                }

                UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                    val device = extractUsbDevice(intent)
                    if (device != null && activeBinding?.device?.deviceId == device.deviceId) {
                        disconnectActive()
                        addLog("USB cable disconnected.")
                    }
                    refreshUsbDevices()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        usbManager = getSystemService(Context.USB_SERVICE) as UsbManager
        hasUsbHostFeature = packageManager.hasSystemFeature(PackageManager.FEATURE_USB_HOST)

        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        currentLanguage = AppLanguage.fromCode(prefs.getString(KEY_LANGUAGE, "fa"))
        val langChosen = prefs.getBoolean(KEY_LANGUAGE_CHOSEN, false)
        disclaimerAlreadyAccepted = prefs.getBoolean(KEY_DISCLAIMER_ACCEPTED, false)

        // First launch order: 1) Ask Language (FA / EN) -> 2) Show Safety Disclaimer in chosen language
        if (!langChosen) {
            showLanguagePickerDialog = true
            showDisclaimerDialog = false
        } else {
            showLanguagePickerDialog = false
            showDisclaimerDialog = !disclaimerAlreadyAccepted
        }

        statusMessage = strings().usbReadyDefault

        val filter = IntentFilter().apply {
            addAction(ACTION_USB_PERMISSION)
            addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
            addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        }
        ContextCompat.registerReceiver(this, usbReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)

        addLog("OTG FlashTool $APP_VERSION_LABEL ready (USB Host API · SAF · ForegroundService · Loke v2/v3 · ADB/Fastboot).")
        addLog("Host: ${Build.MANUFACTURER} ${Build.MODEL} (Android ${Build.VERSION.RELEASE} / API ${Build.VERSION.SDK_INT}) · USB Host=$hasUsbHostFeature")
        refreshUsbDevices()

        extractUsbDevice(intent)?.let { attached ->
            requestPermissionAndConnect(attached)
        }

        val scheme = darkColorScheme(
            primary = Color(0xFF3B82F6),
            onPrimary = Color.White,
            secondary = Color(0xFF10B981),
            background = Color(0xFF090D16),
            surface = Color(0xFF111827),
            surfaceVariant = Color(0xFF1E293B),
            onBackground = Color(0xFFF1F5F9),
            onSurface = Color(0xFFF1F5F9),
            error = Color(0xFFEF4444)
        )

        setContent {
            val dir = if (currentLanguage == AppLanguage.FA) LayoutDirection.Rtl else LayoutDirection.Ltr
            MaterialTheme(colorScheme = scheme) {
                CompositionLocalProvider(LocalLayoutDirection provides dir) {
                    OtgFlashToolApp(s = AppStrings.of(currentLanguage))
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        FlashForegroundService.stop(this)
        try {
            unregisterReceiver(usbReceiver)
        } catch (_: Exception) {
        }
        disconnectActive()
    }

    private fun setAppLanguage(lang: AppLanguage, markInitialChosen: Boolean = true) {
        currentLanguage = lang
        val editor = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
        editor.putString(KEY_LANGUAGE, lang.code)
        if (markInitialChosen) {
            editor.putBoolean(KEY_LANGUAGE_CHOSEN, true)
        }
        editor.apply()
        if (activeBinding == null) {
            refreshUsbDevices()
        }
    }

    private fun openExternalUrl(url: String) {
        runCatching {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
    }

    private fun openEmailSupport() {
        runCatching {
            startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$DEVELOPER_EMAIL")))
        }
    }

    private fun extractUsbDevice(intent: Intent?): UsbDevice? {
        if (intent == null) return null
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(UsbManager.EXTRA_DEVICE, UsbDevice::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
        }
    }

    private fun addLog(message: String) {
        val ts = timeFormat.format(Date())
        runOnUiThread {
            logs.add(0, "[$ts] $message")
            if (logs.size > 350) {
                logs.removeAt(logs.lastIndex)
            }
        }
    }

    private fun refreshUsbDevices() {
        val list = NativeUsbCore.listAttachedDevices(usbManager)
        val ui = strings()
        runOnUiThread {
            attachedDevices.clear()
            attachedDevices.addAll(list)
            if (list.isEmpty() && activeBinding == null) {
                statusMessage = if (hasUsbHostFeature) ui.usbNoDevice else ui.usbNoHostWarning
            } else if (activeBinding == null) {
                statusMessage = ui.usbDevicesFound(list.size)
            }
        }
    }

    private fun requestPermissionAndConnect(device: UsbDevice) {
        if (usbManager.hasPermission(device)) {
            connectToDevice(device)
        } else {
            addLog("Requesting USB Host permission for ${NativeUsbCore.describeDevice(device)}...")
            val explicitIntent = Intent(ACTION_USB_PERMISSION).setPackage(packageName)
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            val pendingIntent = PendingIntent.getBroadcast(this, 0, explicitIntent, flags)
            usbManager.requestPermission(device, pendingIntent)
        }
    }

    private fun disconnectActive() {
        activeBinding?.close()
        activeBinding = null
        activeDownloadMode = null
        activeFastboot = null
        activeAdb = null
        pitEntries = emptyList()
        progressFraction = 0f
        progressLabel = ""
        statusMessage = strings().usbDisconnectedMsg
    }

    private fun connectToDevice(device: UsbDevice) {
        if (isBusy) return
        isBusy = true
        progressFraction = 0.15f
        progressLabel = "1/3 · ${strings().usbConnecting}"
        statusMessage = strings().usbConnecting

        Thread {
            var pendingBinding: UsbBinding? = null
            var promoted = false
            try {
                // Close any previous binding silently on worker thread without overwriting statusMessage
                activeBinding?.close()
                runOnUiThread {
                    activeBinding = null
                    activeDownloadMode = null
                    activeFastboot = null
                    activeAdb = null
                    pitEntries = emptyList()
                }

                val binding = NativeUsbCore.bindDevice(usbManager, device) { addLog(it) }
                pendingBinding = binding

                when (binding.protocol) {
                    DetectedProtocol.DOWNLOAD_MODE -> {
                        runOnUiThread {
                            progressFraction = 0.50f
                            progressLabel = "2/3 · Handshake (Download Mode)..."
                        }
                        val downloader = DownloadModeProtocol(binding)
                        val hs = downloader.handshake { addLog(it) }

                        runOnUiThread {
                            progressFraction = 0.80f
                            progressLabel = "3/3 · PIT..."
                        }
                        val pit = runCatching { downloader.dumpPit { addLog(it) } }.getOrElse { err ->
                            addLog("PIT auto-read warning: ${err.message}")
                            emptyList()
                        }
                        promoted = true
                        runOnUiThread {
                            activeBinding = binding
                            activeDownloadMode = downloader
                            pitEntries = pit
                            statusMessage = "Download Mode v${hs.protocolVersion}.0 (PIT: ${pit.size}) — ${NativeUsbCore.describeDevice(device)}"
                        }
                    }

                    DetectedProtocol.ADB -> {
                        runOnUiThread {
                            progressFraction = 0.60f
                            progressLabel = "2/2 · ADB Auth..."
                        }
                        val adb = AdbProtocol(binding)
                        val banner = adb.connectAndAuth { addLog(it) }
                        promoted = true
                        runOnUiThread {
                            activeBinding = binding
                            activeAdb = adb
                            statusMessage = "ADB — ${banner.ifBlank { NativeUsbCore.describeDevice(device) }}"
                        }
                    }

                    DetectedProtocol.FASTBOOT -> {
                        runOnUiThread {
                            progressFraction = 0.60f
                            progressLabel = "2/2 · Fastboot..."
                        }
                        val fb = FastbootProtocol(binding)
                        val prod = runCatching { fb.sendCommand("getvar:product").message }.getOrDefault("")
                        addLog("Fastboot ready (product=${prod.ifBlank { "unknown" }}).")
                        promoted = true
                        runOnUiThread {
                            activeBinding = binding
                            activeFastboot = fb
                            statusMessage = "Fastboot — ${prod.ifBlank { NativeUsbCore.describeDevice(device) }}"
                        }
                    }

                    else -> {
                        binding.close()
                        throw IllegalStateException("No flashable protocol (Download Mode / ADB / Fastboot) found on device.")
                    }
                }
            } catch (e: Exception) {
                if (!promoted) {
                    pendingBinding?.close()
                }
                addLog("Connection error: ${e.message ?: "unknown"}")
                runOnUiThread {
                    statusMessage = e.message ?: "USB connection error"
                }
            } finally {
                runOnUiThread {
                    isBusy = false
                    progressFraction = 0f
                    progressLabel = ""
                }
            }
        }.start()
    }

    private fun resolvePickedFile(uri: Uri): PickedFile {
        var name = "firmware.bin"
        var size = -1L
        contentResolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE),
            null,
            null,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (nameIdx >= 0) name = cursor.getString(nameIdx) ?: name
                if (sizeIdx >= 0 && !cursor.isNull(sizeIdx)) size = cursor.getLong(sizeIdx)
            }
        }
        if (size <= 0L) {
            size = runCatching {
                contentResolver.openAssetFileDescriptor(uri, "r")?.use { it.length } ?: 0L
            }.getOrDefault(0L)
        }
        return PickedFile(uri = uri, name = name, size = maxOf(0L, size), enabled = true)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun OtgFlashToolApp(s: UiStrings) {
        val scope = rememberCoroutineScope()
        var selectedTab by remember { mutableIntStateOf(0) }

        val slotFiles = remember { mutableStateMapOf<String, PickedFile>() }
        var pickingSlotKey by remember { mutableStateOf("AP") }
        var autoReboot by remember { mutableStateOf(true) }

        var fastbootFile by remember { mutableStateOf<PickedFile?>(null) }
        var fastbootPartition by remember { mutableStateOf("boot") }
        var adbCommand by remember { mutableStateOf("getprop ro.product.model") }
        var adbOutput by remember { mutableStateOf("") }
        var sideloadZipFile by remember { mutableStateOf<PickedFile?>(null) }
        var pendingApkToZipSource by remember { mutableStateOf<PickedFile?>(null) }

        val notifPermissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) addLog("Foreground notification permission granted.")
        }

        LaunchedEffect(disclaimerAlreadyAccepted) {
            if (disclaimerAlreadyAccepted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val perm = ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.POST_NOTIFICATIONS)
                if (perm != PackageManager.PERMISSION_GRANTED) {
                    notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }

        val slotPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) {
                val picked = resolvePickedFile(uri)
                slotFiles[pickingSlotKey] = picked
                addLog("Slot $pickingSlotKey selected via SAF: ${picked.name} (${formatBytes(picked.size)})")
            }
        }

        val fastbootPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) {
                fastbootFile = resolvePickedFile(uri)
                addLog("Fastboot image selected: ${fastbootFile?.name} (${formatBytes(fastbootFile?.size ?: 0L)})")
            }
        }

        val sideloadZipPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) {
                val picked = resolvePickedFile(uri)
                sideloadZipFile = picked
                addLog("ADB Sideload ZIP selected: ${picked.name} (${formatBytes(picked.size)})")
            }
        }

        val createConvertedZipLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.CreateDocument("application/zip")
        ) { destUri ->
            val src = pendingApkToZipSource
            pendingApkToZipSource = null
            if (destUri != null && src != null) {
                scope.launch {
                    isBusy = true
                    progressFraction = 0.01f
                    progressLabel = s.magiskConvertBtn
                    try {
                        val targetName = resolvePickedFile(destUri).name
                        withContext(Dispatchers.IO) {
                            contentResolver.openInputStream(src.uri)?.use { input ->
                                contentResolver.openOutputStream(destUri)?.use { output ->
                                    val buffer = ByteArray(65536)
                                    var copied = 0L
                                    val total = maxOf(src.size, 1L)
                                    while (true) {
                                        val n = input.read(buffer)
                                        if (n <= 0) break
                                        output.write(buffer, 0, n)
                                        copied += n
                                        val frac = (copied.toFloat() / total.toFloat()).coerceIn(0.01f, 1f)
                                        runOnUiThread {
                                            progressFraction = frac
                                            progressLabel = "${src.name} -> $targetName (${formatBytes(copied)})"
                                        }
                                    }
                                    output.flush()
                                } ?: throw IllegalStateException("Unable to open destination ZIP file.")
                            } ?: throw IllegalStateException("Unable to read source APK file.")
                        }
                        val savedZip = resolvePickedFile(destUri)
                        sideloadZipFile = savedZip
                        progressFraction = 1f
                        statusMessage = s.magiskConvertDone(savedZip.name)
                        addLog("Converted Magisk APK -> Flashable ZIP: ${savedZip.name} (${formatBytes(savedZip.size)})")
                    } catch (e: Exception) {
                        addLog("APK -> ZIP error: ${e.message}")
                        statusMessage = e.message ?: "APK Conversion Error"
                    } finally {
                        isBusy = false
                    }
                }
            }
        }

        val apkToZipSourcePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) {
                scope.launch {
                    val picked = withContext(Dispatchers.IO) { resolvePickedFile(uri) }
                    val validPkHeader = withContext(Dispatchers.IO) {
                        runCatching {
                            contentResolver.openInputStream(uri)?.use { input ->
                                val header = ByteArray(2)
                                val read = input.read(header)
                                read == 2 && header[0] == 0x50.toByte() && header[1] == 0x4B.toByte()
                            } ?: false
                        }.getOrDefault(false)
                    }
                    if (!validPkHeader) {
                        addLog("Invalid APK/ZIP header for ${picked.name}")
                        statusMessage = s.magiskInvalidApkError
                    } else {
                        pendingApkToZipSource = picked
                        val baseName = picked.name.substringBeforeLast('.', picked.name)
                        val suggestedZipName = if (picked.name.endsWith(".zip", ignoreCase = true)) {
                            picked.name
                        } else {
                            "$baseName.zip"
                        }
                        addLog("Valid Magisk APK (${picked.name}) selected — choose save location for $suggestedZipName")
                        createConvertedZipLauncher.launch(suggestedZipName)
                    }
                }
            }
        }

        // Step 1 on first launch: Language Selection Dialog (Persian / English)
        if (showLanguagePickerDialog) {
            LanguagePickerDialog(
                s = s,
                onSelectLanguage = { chosen ->
                    setAppLanguage(chosen, markInitialChosen = true)
                    showLanguagePickerDialog = false
                    if (!disclaimerAlreadyAccepted) {
                        showDisclaimerDialog = true
                    }
                }
            )
        }

        // Step 2 on first launch (or from Settings): Safety Disclaimer Dialog
        if (!showLanguagePickerDialog && showDisclaimerDialog) {
            SafetyDisclaimerDialog(
                s = s,
                mandatory = !disclaimerAlreadyAccepted,
                onAccept = {
                    getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                        .edit()
                        .putBoolean(KEY_DISCLAIMER_ACCEPTED, true)
                        .apply()
                    disclaimerAlreadyAccepted = true
                    showDisclaimerDialog = false
                },
                onCloseOnly = {
                    if (disclaimerAlreadyAccepted) showDisclaimerDialog = false
                }
            )
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                            Text(
                                text = "${s.appTitle} ($APP_VERSION_LABEL)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = s.appSubtitle,
                                fontSize = 10.sp,
                                color = Color(0xFF94A3B8),
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    },
                    actions = {
                        OutlinedButton(
                            onClick = {
                                val next = if (currentLanguage == AppLanguage.FA) AppLanguage.EN else AppLanguage.FA
                                setAppLanguage(next, markInitialChosen = true)
                            },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (currentLanguage == AppLanguage.FA) "EN" else "FA",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF0F172A),
                        titleContentColor = Color.White
                    )
                )
            },
            bottomBar = {
                NavigationBar(containerColor = Color(0xFF0F172A)) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = { Icon(Icons.Default.Usb, contentDescription = null) },
                        label = { Text(s.navFlash, fontSize = 11.sp, maxLines = 1) }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = { Icon(Icons.Default.Build, contentDescription = null) },
                        label = { Text(s.navAdb, fontSize = 11.sp, maxLines = 1) }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = { Icon(Icons.Default.HelpOutline, contentDescription = null) },
                        label = { Text(s.navGuide, fontSize = 11.sp, maxLines = 1) }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        icon = { Icon(Icons.Default.Terminal, contentDescription = null) },
                        label = { Text(s.navLogs, fontSize = 11.sp, maxLines = 1) }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 4,
                        onClick = { selectedTab = 4 },
                        icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                        label = { Text(s.navAbout, fontSize = 11.sp, maxLines = 1) }
                    )
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (selectedTab <= 1) {
                    UsbConnectionCard(s = s)
                }

                if (progressFraction > 0f || isBusy) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = progressLabel.ifBlank { s.processing },
                                fontSize = 12.sp,
                                color = Color(0xFFCBD5E1),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${(progressFraction * 100).toInt()}%",
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF60A5FA)
                            )
                        }
                        LinearProgressIndicator(
                            progress = { if (progressFraction > 0f) progressFraction else 0.12f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                        )
                    }
                }

                when (selectedTab) {
                    0 -> DownloadModeFourFileTab(
                        s = s,
                        slotFiles = slotFiles,
                        autoReboot = autoReboot,
                        onAutoRebootChange = { autoReboot = it },
                        onPickSlot = { key ->
                            pickingSlotKey = key
                            slotPicker.launch(arrayOf("*/*"))
                        },
                        onToggleSlot = { key, enabled ->
                            slotFiles[key]?.let { slotFiles[key] = it.copy(enabled = enabled) }
                        },
                        onClearSlot = { key -> slotFiles.remove(key) },
                        onReadPit = {
                            val dl = activeDownloadMode ?: return@DownloadModeFourFileTab
                            scope.launch {
                                isBusy = true
                                progressLabel = s.readPitBtn
                                try {
                                    val entries = withContext(Dispatchers.IO) {
                                        dl.dumpPit { addLog(it) }
                                    }
                                    pitEntries = entries
                                    statusMessage = s.pitTableTitle(entries.size)
                                } catch (e: Exception) {
                                    addLog("PIT error: ${e.message}")
                                    statusMessage = e.message ?: "PIT Error"
                                } finally {
                                    isBusy = false
                                    progressLabel = ""
                                }
                            }
                        },
                        onStartFlash = {
                            val dl = activeDownloadMode ?: return@DownloadModeFourFileTab
                            val selectedInputs = FIRMWARE_SLOTS.mapNotNull { meta ->
                                val f = slotFiles[meta.key]
                                if (f != null && f.enabled) {
                                    DownloadModeSlotInput(meta.key, f.uri, f.name, f.size)
                                } else {
                                    null
                                }
                            }
                            if (selectedInputs.isEmpty()) return@DownloadModeFourFileTab

                            scope.launch {
                                isBusy = true
                                progressFraction = 0.01f
                                progressLabel = s.processing
                                FlashForegroundService.update(
                                    this@MainActivity,
                                    s.notifFlashTitle,
                                    s.notifFlashKeepConnected,
                                    1
                                )
                                var lastNotifPct = -1
                                try {
                                    withContext(Dispatchers.IO) {
                                        val pit = pitEntries.ifEmpty {
                                            dl.dumpPit { addLog(it) }.also { loaded ->
                                                runOnUiThread { pitEntries = loaded }
                                            }
                                        }
                                        dl.flashSlots(
                                            resolver = contentResolver,
                                            slots = selectedInputs,
                                            pit = pit,
                                            autoReboot = autoReboot,
                                            onLog = { addLog(it) },
                                            onProgress = { sent, total, label ->
                                                val frac = if (total > 0L) sent.toFloat() / total.toFloat() else 0f
                                                val pct = (frac * 100f).toInt().coerceIn(0, 100)
                                                if (pct != lastNotifPct) {
                                                    lastNotifPct = pct
                                                    FlashForegroundService.update(
                                                        this@MainActivity,
                                                        "${s.notifFlashTitle} ($pct%)",
                                                        "$label — ${s.notifFlashKeepConnected}",
                                                        pct
                                                    )
                                                }
                                                runOnUiThread {
                                                    progressFraction = frac.coerceIn(0f, 1f)
                                                    progressLabel = "$label (${formatBytes(sent)} / ${formatBytes(total)})"
                                                }
                                            }
                                        )
                                    }
                                    progressFraction = 1f
                                    statusMessage = s.flashPassMsg
                                } catch (e: Exception) {
                                    addLog("Flash error: ${e.message}")
                                    statusMessage = e.message ?: "Flash Error"
                                } finally {
                                    FlashForegroundService.stop(this@MainActivity)
                                    isBusy = false
                                }
                            }
                        }
                    )

                    1 -> AdbFastbootTab(
                        s = s,
                        adbCommand = adbCommand,
                        onAdbCommandChange = { adbCommand = it },
                        adbOutput = adbOutput,
                        onRunAdbShell = {
                            val adb = activeAdb ?: return@AdbFastbootTab
                            scope.launch {
                                isBusy = true
                                try {
                                    addLog("adb shell > $adbCommand")
                                    val res = withContext(Dispatchers.IO) { adb.shell(adbCommand) }
                                    adbOutput = res.ifBlank { s.adbEmptyOutput }
                                    addLog(adbOutput.take(240))
                                } catch (e: Exception) {
                                    addLog("ADB Shell error: ${e.message}")
                                } finally {
                                    isBusy = false
                                }
                            }
                        },
                        onAdbReboot = { target ->
                            val adb = activeAdb ?: return@AdbFastbootTab
                            scope.launch {
                                isBusy = true
                                try {
                                    addLog("ADB reboot -> $target")
                                    withContext(Dispatchers.IO) { adb.reboot(target) }
                                    statusMessage = s.adbRebootSent(target)
                                } catch (e: Exception) {
                                    addLog("ADB Reboot error: ${e.message}")
                                } finally {
                                    isBusy = false
                                }
                            }
                        },
                        fastbootFile = fastbootFile,
                        fastbootPartition = fastbootPartition,
                        onFastbootPartitionChange = { fastbootPartition = it },
                        onPickFastbootFile = { fastbootPicker.launch(arrayOf("*/*")) },
                        onFastbootFlash = {
                            val fb = activeFastboot ?: return@AdbFastbootTab
                            val file = fastbootFile ?: return@AdbFastbootTab
                            val part = fastbootPartition.trim().ifBlank { "boot" }
                            scope.launch {
                                isBusy = true
                                progressFraction = 0.01f
                                progressLabel = s.notifFastbootTitle(part)
                                FlashForegroundService.update(
                                    this@MainActivity,
                                    s.notifFastbootTitle(part),
                                    s.notifFlashKeepConnected,
                                    1
                                )
                                var lastPct = -1
                                try {
                                    withContext(Dispatchers.IO) {
                                        fb.flashUri(
                                            resolver = contentResolver,
                                            partition = part,
                                            uri = file.uri,
                                            fileSize = file.size,
                                            onLog = { addLog(it) },
                                            onProgress = { sent, total ->
                                                val frac = if (total > 0L) sent.toFloat() / total.toFloat() else 0f
                                                val pct = (frac * 100f).toInt().coerceIn(0, 100)
                                                if (pct != lastPct) {
                                                    lastPct = pct
                                                    FlashForegroundService.update(
                                                        this@MainActivity,
                                                        "${s.notifFastbootTitle(part)} ($pct%)",
                                                        s.notifFlashKeepConnected,
                                                        pct
                                                    )
                                                }
                                                runOnUiThread {
                                                    progressFraction = frac.coerceIn(0f, 1f)
                                                    progressLabel = "$part (${formatBytes(sent)} / ${formatBytes(total)})"
                                                }
                                            }
                                        )
                                    }
                                    progressFraction = 1f
                                    statusMessage = s.fastbootFlashDone(part)
                                } catch (e: Exception) {
                                    addLog("Fastboot error: ${e.message}")
                                    statusMessage = e.message ?: "Fastboot Error"
                                } finally {
                                    FlashForegroundService.stop(this@MainActivity)
                                    isBusy = false
                                }
                            }
                        },
                        onFastbootCommand = { cmd ->
                            val fb = activeFastboot ?: return@AdbFastbootTab
                            scope.launch {
                                isBusy = true
                                try {
                                    addLog("fastboot > $cmd")
                                    val r = withContext(Dispatchers.IO) { fb.sendCommand(cmd) }
                                    r.infoLines.forEach { addLog("INFO: $it") }
                                    addLog("${r.status}: ${r.message}")
                                } catch (e: Exception) {
                                    addLog("Fastboot command error: ${e.message}")
                                } finally {
                                    isBusy = false
                                }
                            }
                        },
                        sideloadZipFile = sideloadZipFile,
                        onDownloadMagisk = {
                            addLog("Opening official Magisk GitHub Releases...")
                            openExternalUrl(MAGISK_RELEASES_URL)
                        },
                        onConvertMagiskApkToZip = {
                            apkToZipSourcePicker.launch(
                                arrayOf(
                                    "application/vnd.android.package-archive",
                                    "application/octet-stream",
                                    "*/*"
                                )
                            )
                        },
                        onPickSideloadZip = {
                            sideloadZipPicker.launch(
                                arrayOf(
                                    "application/zip",
                                    "application/octet-stream",
                                    "*/*"
                                )
                            )
                        },
                        onStartSideload = {
                            val adb = activeAdb ?: return@AdbFastbootTab
                            val zipFile = sideloadZipFile ?: return@AdbFastbootTab
                            scope.launch {
                                isBusy = true
                                progressFraction = 0.01f
                                progressLabel = "ADB Sideload (${zipFile.name})"
                                FlashForegroundService.update(
                                    this@MainActivity,
                                    "ADB Sideload (${zipFile.name})",
                                    s.notifFlashKeepConnected,
                                    1
                                )
                                var lastPct = -1
                                try {
                                    withContext(Dispatchers.IO) {
                                        adb.sideloadUri(
                                            resolver = contentResolver,
                                            uri = zipFile.uri,
                                            fileSize = zipFile.size,
                                            onLog = { addLog(it) },
                                            onProgress = { sent, total ->
                                                val frac = if (total > 0L) sent.toFloat() / total.toFloat() else 0f
                                                val pct = (frac * 100f).toInt().coerceIn(0, 100)
                                                if (pct != lastPct) {
                                                    lastPct = pct
                                                    FlashForegroundService.update(
                                                        this@MainActivity,
                                                        "ADB Sideload ($pct%)",
                                                        s.notifFlashKeepConnected,
                                                        pct
                                                    )
                                                }
                                                runOnUiThread {
                                                    progressFraction = frac.coerceIn(0f, 1f)
                                                    progressLabel = "${zipFile.name} (${formatBytes(sent)} / ${formatBytes(total)})"
                                                }
                                            }
                                        )
                                    }
                                    progressFraction = 1f
                                    statusMessage = s.sideloadDoneMsg
                                } catch (e: Exception) {
                                    addLog("ADB Sideload error: ${e.message}")
                                    statusMessage = e.message ?: "ADB Sideload Error"
                                } finally {
                                    FlashForegroundService.stop(this@MainActivity)
                                    isBusy = false
                                }
                            }
                        }
                    )

                    2 -> GuideTab(s = s)
                    3 -> LogsTab(s = s)
                    else -> SettingsAndAboutTab(
                        s = s,
                        selectedLanguage = currentLanguage,
                        onSelectLanguage = { setAppLanguage(it, markInitialChosen = true) },
                        onShowDisclaimer = { showDisclaimerDialog = true },
                        onOpenRepo = { openExternalUrl(GITHUB_REPO_URL) },
                        onOpenReleases = { openExternalUrl(GITHUB_RELEASES_URL) }
                    )
                }
            }
        }
    }

    @Composable
    private fun LanguagePickerDialog(
        s: UiStrings,
        onSelectLanguage: (AppLanguage) -> Unit
    ) {
        AlertDialog(
            onDismissRequest = { /* Mandatory on first launch */ },
            containerColor = Color(0xFF111827),
            titleContentColor = Color.White,
            textContentColor = Color(0xFFCBD5E1),
            icon = {
                Icon(
                    Icons.Default.Language,
                    contentDescription = null,
                    tint = Color(0xFF60A5FA),
                    modifier = Modifier.size(34.dp)
                )
            },
            title = {
                Text(
                    text = s.langDialogTitle,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = s.langDialogSubtitle,
                        fontSize = 12.sp,
                        lineHeight = 19.sp
                    )
                    Button(
                        onClick = { onSelectLanguage(AppLanguage.FA) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(s.langPersianLabel, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    OutlinedButton(
                        onClick = { onSelectLanguage(AppLanguage.EN) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(s.langEnglishLabel, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            },
            confirmButton = {}
        )
    }

    @Composable
    private fun SafetyDisclaimerDialog(
        s: UiStrings,
        mandatory: Boolean,
        onAccept: () -> Unit,
        onCloseOnly: () -> Unit
    ) {
        var checked by remember { mutableStateOf(!mandatory) }

        AlertDialog(
            onDismissRequest = { if (!mandatory) onCloseOnly() },
            containerColor = Color(0xFF111827),
            titleContentColor = Color.White,
            textContentColor = Color(0xFFCBD5E1),
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFFBBF24),
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = s.disclaimerTitle,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = s.disclaimerLead,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Text(text = s.disclaimerBullet1, fontSize = 12.sp, lineHeight = 19.sp)
                    Text(text = s.disclaimerBullet2, fontSize = 12.sp, lineHeight = 19.sp)
                    Text(text = s.disclaimerBullet3, fontSize = 12.sp, lineHeight = 19.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0B1220), RoundedCornerShape(10.dp))
                            .padding(8.dp)
                    ) {
                        Checkbox(checked = checked, onCheckedChange = { checked = it })
                        Text(
                            text = s.disclaimerCheckbox,
                            fontSize = 12.sp,
                            color = Color(0xFFE2E8F0)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = onAccept,
                    enabled = checked,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                ) {
                    Text(s.disclaimerConfirm)
                }
            },
            dismissButton = if (!mandatory) {
                {
                    OutlinedButton(onClick = onCloseOnly) {
                        Text(s.closeBtn)
                    }
                }
            } else null
        )
    }

    @Composable
    private fun UsbConnectionCard(s: UiStrings) {
        val connected = activeBinding != null
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (connected) s.usbConnectedTitle else s.usbStatusTitle,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (connected) Color(0xFF34D399) else Color.White
                        )
                        Text(
                            text = statusMessage,
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    if (connected) {
                        OutlinedButton(onClick = { disconnectActive() }, enabled = !isBusy) {
                            Icon(Icons.Default.UsbOff, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(s.usbDisconnect, fontSize = 12.sp)
                        }
                    } else {
                        Button(
                            onClick = {
                                refreshUsbDevices()
                                val first = attachedDevices.firstOrNull()
                                if (first != null) {
                                    requestPermissionAndConnect(first)
                                } else {
                                    addLog("OTG scan: no device attached (plug OTG adapter into host phone, regular cable into target phone).")
                                }
                            },
                            enabled = !isBusy,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(s.usbScanConnect, fontSize = 12.sp, maxLines = 1)
                        }
                    }
                }

                if (!connected && attachedDevices.isNotEmpty()) {
                    HorizontalDivider(color = Color(0xFF1E293B))
                    attachedDevices.forEach { dev ->
                        val proto = NativeUsbCore.detectProtocol(dev)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = NativeUsbCore.describeDevice(dev),
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFFE2E8F0)
                                )
                                Text(
                                    text = s.usbDetectedProto(proto.name),
                                    fontSize = 11.sp,
                                    color = Color(0xFF60A5FA)
                                )
                            }
                            OutlinedButton(
                                onClick = { requestPermissionAndConnect(dev) },
                                enabled = !isBusy
                            ) {
                                Text(s.usbConnect, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun DownloadModeFourFileTab(
        s: UiStrings,
        slotFiles: Map<String, PickedFile>,
        autoReboot: Boolean,
        onAutoRebootChange: (Boolean) -> Unit,
        onPickSlot: (String) -> Unit,
        onToggleSlot: (String, Boolean) -> Unit,
        onClearSlot: (String) -> Unit,
        onReadPit: () -> Unit,
        onStartFlash: () -> Unit
    ) {
        val activeSlotCount = FIRMWARE_SLOTS.count { slotFiles[it.key]?.enabled == true }
        val dlReady = activeDownloadMode != null && !isBusy

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(text = s.slotsTitle, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(text = s.slotsDesc, fontSize = 12.sp, color = Color(0xFF94A3B8))

                    FIRMWARE_SLOTS.forEach { slotMeta ->
                        val picked = slotFiles[slotMeta.key]
                        val slotTitle = when (slotMeta.key) {
                            "BL" -> s.slotBlTitle
                            "AP" -> s.slotApTitle
                            "CP" -> s.slotCpTitle
                            else -> s.slotCscTitle
                        }
                        val slotHint = when (slotMeta.key) {
                            "BL" -> s.slotBlHint
                            "AP" -> s.slotApHint
                            "CP" -> s.slotCpHint
                            else -> s.slotCscHint
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF0B1220), RoundedCornerShape(12.dp))
                                .border(
                                    width = 1.dp,
                                    color = if (picked != null && picked.enabled) slotMeta.accent.copy(alpha = 0.6f) else Color(0xFF1E293B),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = picked?.enabled == true,
                                enabled = picked != null && !isBusy,
                                onCheckedChange = { checked -> onToggleSlot(slotMeta.key, checked) }
                            )

                            OutlinedButton(
                                onClick = { onPickSlot(slotMeta.key) },
                                enabled = !isBusy,
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                                modifier = Modifier.width(62.dp)
                            ) {
                                Text(
                                    text = slotMeta.key,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = slotMeta.accent,
                                    fontSize = 13.sp,
                                    maxLines = 1
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                if (picked != null) {
                                    Text(
                                        text = picked.name,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = Color(0xFFF1F5F9),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "$slotTitle · ${formatBytes(picked.size)}",
                                        fontSize = 11.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                } else {
                                    Text(
                                        text = slotTitle,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFFCBD5E1)
                                    )
                                    Text(
                                        text = slotHint,
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }

                            if (picked != null && !isBusy) {
                                IconButton(onClick = { onClearSlot(slotMeta.key) }) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = s.clearBtn,
                                        tint = Color(0xFF94A3B8),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = s.autoRebootLabel,
                            fontSize = 13.sp,
                            color = Color(0xFFE2E8F0),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(checked = autoReboot, onCheckedChange = onAutoRebootChange)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onReadPit,
                            enabled = dlReady,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(s.readPitBtn, fontSize = 13.sp, maxLines = 1)
                        }
                        Button(
                            onClick = onStartFlash,
                            enabled = dlReady && activeSlotCount > 0,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                            modifier = Modifier.weight(1.4f)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (activeSlotCount > 1) s.startFlashMultiBtn(activeSlotCount) else s.startFlashBtn,
                                fontSize = 13.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            if (pitEntries.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = s.pitTableTitle(pitEntries.size),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        pitEntries.take(25).forEach { entry ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "#${entry.partitionId} ${entry.partitionName}",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = Color(0xFFE2E8F0)
                                )
                                Text(
                                    text = entry.flashFilename.ifBlank { "-" },
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = Color(0xFF60A5FA)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun AdbFastbootTab(
        s: UiStrings,
        adbCommand: String,
        onAdbCommandChange: (String) -> Unit,
        adbOutput: String,
        onRunAdbShell: () -> Unit,
        onAdbReboot: (String) -> Unit,
        fastbootFile: PickedFile?,
        fastbootPartition: String,
        onFastbootPartitionChange: (String) -> Unit,
        onPickFastbootFile: () -> Unit,
        onFastbootFlash: () -> Unit,
        onFastbootCommand: (String) -> Unit,
        sideloadZipFile: PickedFile?,
        onDownloadMagisk: () -> Unit,
        onConvertMagiskApkToZip: () -> Unit,
        onPickSideloadZip: () -> Unit,
        onStartSideload: () -> Unit
    ) {
        val adbReady = activeAdb != null && !isBusy
        val fbReady = activeFastboot != null && !isBusy

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(text = s.adbCardTitle, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = { onAdbReboot("download") },
                            enabled = adbReady,
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Download", fontSize = 11.sp, maxLines = 1)
                        }
                        OutlinedButton(
                            onClick = { onAdbReboot("bootloader") },
                            enabled = adbReady,
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Fastboot", fontSize = 11.sp, maxLines = 1)
                        }
                        OutlinedButton(
                            onClick = { onAdbReboot("recovery") },
                            enabled = adbReady,
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Recovery", fontSize = 11.sp, maxLines = 1)
                        }
                    }

                    OutlinedTextField(
                        value = adbCommand,
                        onValueChange = onAdbCommandChange,
                        label = { Text(s.adbCmdLabel) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = onRunAdbShell,
                        enabled = adbReady && adbCommand.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(s.adbRunShellBtn)
                    }

                    if (adbOutput.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF020617), RoundedCornerShape(10.dp))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = adbOutput,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = Color(0xFF34D399)
                            )
                        }
                    }
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(text = s.fastbootCardTitle, fontWeight = FontWeight.Bold, fontSize = 14.sp)

                    OutlinedTextField(
                        value = fastbootPartition,
                        onValueChange = onFastbootPartitionChange,
                        label = { Text(s.fastbootPartLabel) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedButton(
                        onClick = onPickFastbootFile,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = fastbootFile?.let { "${it.name} (${formatBytes(it.size)})" } ?: s.fastbootPickImg,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onFastbootFlash,
                            enabled = fbReady && fastbootFile != null,
                            modifier = Modifier.weight(1.4f)
                        ) {
                            Text(s.fastbootFlashBtn)
                        }
                        OutlinedButton(
                            onClick = { onFastbootCommand("reboot") },
                            enabled = fbReady,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(s.fastbootRebootBtn)
                        }
                    }

                    HorizontalDivider(color = Color(0xFF1F2937))

                    var pendingOemCmd by remember { mutableStateOf<String?>(null) }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.LockOpen,
                            contentDescription = null,
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = s.fastbootOemSectionTitle,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                    }

                    Text(
                        text = s.fastbootOemSectionDesc,
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8),
                        lineHeight = 17.sp
                    )

                    OutlinedButton(
                        onClick = { onFastbootCommand("flashing get_unlock_ability") },
                        enabled = fbReady,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(s.fastbootCheckUnlockBtn, fontSize = 12.sp)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { pendingOemCmd = "flashing unlock" },
                            enabled = fbReady,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(s.fastbootUnlockBtn, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        OutlinedButton(
                            onClick = { pendingOemCmd = "flashing lock" },
                            enabled = fbReady,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(s.fastbootLockBtn, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }

                    if (pendingOemCmd != null) {
                        val cmdToRun = pendingOemCmd!!
                        AlertDialog(
                            onDismissRequest = { pendingOemCmd = null },
                            title = { Text(s.fastbootOemConfirmTitle, fontWeight = FontWeight.Bold, fontSize = 15.sp) },
                            text = {
                                Text(
                                    text = if (cmdToRun == "flashing unlock") s.fastbootOemUnlockConfirmBody else s.fastbootOemLockConfirmBody,
                                    fontSize = 12.sp,
                                    lineHeight = 19.sp
                                )
                            },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        pendingOemCmd = null
                                        onFastbootCommand(cmdToRun)
                                    }
                                ) {
                                    Text(s.fastbootOemConfirmBtn)
                                }
                            },
                            dismissButton = {
                                OutlinedButton(onClick = { pendingOemCmd = null }) {
                                    Text(s.closeBtn)
                                }
                            }
                        )
                    }
                }
            }

            // Magisk Root Toolkit & ADB Sideload Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF10B981).copy(alpha = 0.45f), RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Security,
                            contentDescription = null,
                            tint = Color(0xFF34D399),
                            modifier = Modifier.size(19.dp)
                        )
                        Text(
                            text = s.magiskCardTitle,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }

                    Text(
                        text = s.magiskCardDesc,
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8),
                        lineHeight = 18.sp
                    )

                    Button(
                        onClick = onDownloadMagisk,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(17.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(s.magiskDownloadBtn, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }

                    OutlinedButton(
                        onClick = onConvertMagiskApkToZip,
                        enabled = !isBusy,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Transform, contentDescription = null, modifier = Modifier.size(17.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(s.magiskConvertBtn, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }

                    HorizontalDivider(color = Color(0xFF1F2937))

                    Text(
                        text = s.sideloadTitle,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF60A5FA)
                    )

                    OutlinedButton(
                        onClick = onPickSideloadZip,
                        enabled = !isBusy,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(17.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = sideloadZipFile?.let { "${it.name} (${formatBytes(it.size)})" } ?: s.sideloadPickZip,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Button(
                        onClick = onStartSideload,
                        enabled = adbReady && sideloadZipFile != null,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(17.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(s.sideloadFlashBtn, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }

    @Composable
    private fun GuideTab(s: UiStrings) {
        var devGuideExpanded by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF2563EB).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = s.guide1Title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF60A5FA)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0B1220), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = s.guide1Host,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF34D399),
                                lineHeight = 20.sp
                            )
                            Text(
                                text = s.guide1Target,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFFBBF24),
                                lineHeight = 20.sp
                            )
                        }
                    }
                    Text(
                        text = s.guide1Note,
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8),
                        lineHeight = 18.sp
                    )
                }
            }

            // Section 2: Developer Options & USB Debugging recommendation
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = s.guideDevTitle, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                    Text(text = s.guideDevBody, fontSize = 12.sp, color = Color(0xFFCBD5E1), lineHeight = 20.sp)
                }
            }

            // Separate expandable card with arrow for "How to enable Developer Options & USB Debugging"
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        if (devGuideExpanded) Color(0xFF10B981).copy(alpha = 0.65f) else Color(0xFF334155),
                        RoundedCornerShape(16.dp)
                    )
                    .clickable { devGuideExpanded = !devGuideExpanded }
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = s.guideDevToggleTitle,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF34D399)
                            )
                            Text(
                                text = s.guideDevToggleSubtitle,
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color(0xFF1E293B), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (devGuideExpanded) {
                                    Icons.Default.KeyboardArrowUp
                                } else {
                                    Icons.Default.KeyboardArrowDown
                                },
                                contentDescription = null,
                                tint = Color(0xFF34D399),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    AnimatedVisibility(visible = devGuideExpanded) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            HorizontalDivider(color = Color(0xFF1E293B))
                            Text(
                                text = s.guideDevSteps,
                                fontSize = 12.sp,
                                color = Color(0xFFE2E8F0),
                                lineHeight = 21.sp
                            )
                        }
                    }
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = s.guide2Title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                    Text(text = s.guide2Body, fontSize = 12.sp, color = Color(0xFFCBD5E1), lineHeight = 20.sp)
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = s.guide3Title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                    Text(text = s.guide3Body, fontSize = 12.sp, color = Color(0xFFCBD5E1), lineHeight = 20.sp)
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = s.guide4Title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                    Text(text = s.guide4Body, fontSize = 12.sp, color = Color(0xFFCBD5E1), lineHeight = 20.sp)
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF10B981).copy(alpha = 0.45f), RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = s.guide5Title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF34D399)
                    )
                    Text(text = s.guide5Body, fontSize = 12.sp, color = Color(0xFFCBD5E1), lineHeight = 20.sp)
                }
            }
        }
    }

    @Composable
    private fun LogsTab(s: UiStrings) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(s.logsTitle(logs.size), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = {
                            val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            cm.setPrimaryClip(ClipData.newPlainText("OTG FlashTool Logs", logs.joinToString("\n")))
                            statusMessage = s.logsCopiedMsg
                        },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(s.copyBtn, fontSize = 12.sp)
                    }
                    OutlinedButton(
                        onClick = { logs.clear() },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(s.clearBtn, fontSize = 12.sp)
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF020617), RoundedCornerShape(14.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(logs) { line ->
                    Text(
                        text = line,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = Color(0xFFCBD5E1),
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }

    @Composable
    private fun SettingsAndAboutTab(
        s: UiStrings,
        selectedLanguage: AppLanguage,
        onSelectLanguage: (AppLanguage) -> Unit,
        onShowDisclaimer: () -> Unit,
        onOpenRepo: () -> Unit,
        onOpenReleases: () -> Unit
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Language Settings Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF2563EB).copy(alpha = 0.45f), RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Language,
                            contentDescription = null,
                            tint = Color(0xFF60A5FA),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = s.settingsCardTitle,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                    }
                    Text(
                        text = s.settingsLangLabel,
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (selectedLanguage == AppLanguage.FA) {
                            Button(
                                onClick = { onSelectLanguage(AppLanguage.FA) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("فارسی (FA)", fontSize = 13.sp, maxLines = 1)
                            }
                            OutlinedButton(
                                onClick = { onSelectLanguage(AppLanguage.EN) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("English (EN)", fontSize = 13.sp, maxLines = 1)
                            }
                        } else {
                            OutlinedButton(
                                onClick = { onSelectLanguage(AppLanguage.FA) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("فارسی (FA)", fontSize = 13.sp, maxLines = 1)
                            }
                            Button(
                                onClick = { onSelectLanguage(AppLanguage.EN) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("English (EN)", fontSize = 13.sp, maxLines = 1)
                            }
                        }
                    }
                }
            }

            // About Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = s.aboutTitle, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color.White)
                    Text(text = s.aboutVersionLine(APP_VERSION_LABEL), fontSize = 12.sp, color = Color(0xFF60A5FA))
                    Text(text = s.aboutDesc, fontSize = 12.sp, color = Color(0xFFCBD5E1), lineHeight = 19.sp)
                }
            }

            // Developer GitHub Repository Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(text = s.githubCardTitle, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                    Text(
                        text = GITHUB_REPO_URL,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = Color(0xFF60A5FA)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onOpenRepo,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(s.githubOpenRepo, fontSize = 12.sp, maxLines = 1)
                        }
                        OutlinedButton(
                            onClick = onOpenReleases,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(s.githubOpenReleases, fontSize = 12.sp, maxLines = 1)
                        }
                    }
                }
            }

            // Social Media & Community Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = s.socialCardTitle,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                    Text(
                        text = s.socialCardSubtitle,
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { openExternalUrl(YOUTUBE_URL) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("YouTube", fontSize = 12.sp, maxLines = 1)
                        }
                        Button(
                            onClick = { openExternalUrl(TELEGRAM_URL) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Telegram", fontSize = 12.sp, maxLines = 1)
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { openExternalUrl(INSTAGRAM_URL) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDB2777)),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Instagram", fontSize = 12.sp, maxLines = 1)
                        }
                        Button(
                            onClick = { openExternalUrl(FACEBOOK_URL) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Facebook", fontSize = 12.sp, maxLines = 1)
                        }
                    }

                    HorizontalDivider(color = Color(0xFF1F2937))

                    Text(
                        text = s.contactEmailLabel,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFCBD5E1)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { openEmailSupport() },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = DEVELOPER_EMAIL,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        OutlinedButton(
                            onClick = {
                                val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                cm.setPrimaryClip(ClipData.newPlainText("Developer Email", DEVELOPER_EMAIL))
                                statusMessage = s.contactEmailCopied
                                addLog("${s.contactEmailCopied}: $DEVELOPER_EMAIL")
                            },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(15.dp))
                        }
                    }
                }
            }

            // Google Play Compliance Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Security,
                            contentDescription = null,
                            tint = Color(0xFF34D399),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = s.complianceCardTitle,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                    Text(text = s.complianceBody, fontSize = 12.sp, color = Color(0xFFCBD5E1), lineHeight = 20.sp)
                    OutlinedButton(
                        onClick = onShowDisclaimer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(s.showDisclaimerAgain, fontSize = 12.sp)
                    }
                }
            }
        }
    }

    private fun formatBytes(bytes: Long): String {
        if (bytes < 1024L) return "$bytes B"
        val kb = bytes / 1024.0
        if (kb < 1024.0) return "%.1f KB".format(Locale.US, kb)
        val mb = kb / 1024.0
        if (mb < 1024.0) return "%.1f MB".format(Locale.US, mb)
        return "%.2f GB".format(Locale.US, mb / 1024.0)
    }
}

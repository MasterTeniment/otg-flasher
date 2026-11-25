package com.otgflashtool.app.usb

import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbEndpoint
import android.hardware.usb.UsbInterface
import android.hardware.usb.UsbManager
import android.os.Build

enum class DetectedProtocol {
    DOWNLOAD_MODE,
    ADB,
    FASTBOOT,
    MTP_ONLY,
    UNKNOWN
}

data class UsbBinding(
    val device: UsbDevice,
    val connection: UsbDeviceConnection,
    val protocol: DetectedProtocol,
    val dataInterface: UsbInterface,
    var controlInterface: UsbInterface?,
    val epIn: UsbEndpoint,
    val epOut: UsbEndpoint
) {
    fun resetDataInterface(onLog: ((String) -> Unit)? = null): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                var ok = connection.setInterface(dataInterface)
                if (!ok) {
                    clearEndpointHalts()
                    connection.claimInterface(dataInterface, true)
                    ok = connection.setInterface(dataInterface)
                }
                onLog?.invoke("تنظیم alt-setting اینترفیس داده #${dataInterface.id} (SET_INTERFACE=$ok).")
                ok
            } else {
                true
            }
        } catch (e: Exception) {
            onLog?.invoke("هشدار SET_INTERFACE: ${e.message}")
            false
        }
    }

    fun enableCdcControlFallback(onLog: ((String) -> Unit)? = null): Boolean {
        val ctrl = controlInterface ?: return false
        return try {
            val claimed = connection.claimInterface(ctrl, true)
            if (!claimed) {
                // Never send CDC DTR/RTS to wIndex=0 on an unclaimed interface (avoids -1 / control pipe stall)
                onLog?.invoke("تلاش جایگزین CDC-ACM صرف‌نظر شد (اینترفیس کنترلی #${ctrl.id} تصاحب نشد).")
                return false
            }
            val dtrRes = connection.controlTransfer(0x21, 0x22, 0x0003, ctrl.id, null, 0, 350)
            onLog?.invoke("تلاش جایگزین CDC-ACM (Ctrl #${ctrl.id} · claimed=true · DTR/RTS=$dtrRes).")
            dtrRes >= 0
        } catch (e: Exception) {
            onLog?.invoke("هشدار CDC-ACM جایگزین: ${e.message}")
            false
        }
    }

    fun clearEndpointHalts() {
        try {
            // USB Standard Request: CLEAR_FEATURE (0x01), Feature ENDPOINT_HALT (0x00), Recipient Endpoint (0x02)
            connection.controlTransfer(0x02, 0x01, 0, epIn.address, null, 0, 500)
            connection.controlTransfer(0x02, 0x01, 0, epOut.address, null, 0, 500)
        } catch (_: Exception) {
        }
    }

    fun drainStaleRx(maxPackets: Int = 4) {
        val buf = ByteArray(maxOf(64, epIn.maxPacketSize))
        for (i in 0 until maxPackets) {
            val r = try {
                connection.bulkTransfer(epIn, buf, buf.size, 40)
            } catch (_: Exception) {
                -1
            }
            if (r <= 0) break
        }
    }

    fun close() {
        runCatching { controlInterface?.let { connection.releaseInterface(it) } }
        runCatching { connection.releaseInterface(dataInterface) }
        runCatching { connection.close() }
    }
}

object NativeUsbCore {
    const val TARGET_VENDOR_ID = 0x04E8
    private val DOWNLOAD_MODE_PIDS = setOf(0x685D, 0x6601, 0x68C3, 0x68A9, 0x685E, 0x6860)

    private const val ADB_CLASS = 0xFF
    private const val ADB_SUBCLASS = 0x42
    private const val ADB_PROTOCOL = 0x01
    private const val FASTBOOT_PROTOCOL = 0x03

    private const val CDC_CTRL_CLASS = 0x02
    private const val CDC_DATA_CLASS = 0x0A

    fun listAttachedDevices(usbManager: UsbManager): List<UsbDevice> {
        return usbManager.deviceList.values.toList()
    }

    fun detectProtocol(device: UsbDevice): DetectedProtocol {
        var hasAdb = false
        var hasFastboot = false
        var hasCdc = false
        var hasMtp = false

        for (i in 0 until device.interfaceCount) {
            val iface = device.getInterface(i)
            if (iface.interfaceClass == ADB_CLASS && iface.interfaceSubclass == ADB_SUBCLASS) {
                if (iface.interfaceProtocol == FASTBOOT_PROTOCOL) hasFastboot = true
                if (iface.interfaceProtocol == ADB_PROTOCOL) hasAdb = true
            }
            if (iface.interfaceClass == CDC_DATA_CLASS || iface.interfaceClass == CDC_CTRL_CLASS) {
                hasCdc = true
            }
            if (iface.interfaceClass == 0x06 || iface.interfaceClass == 0x08) {
                hasMtp = true
            }
        }

        val isDownloadVendor = device.vendorId == TARGET_VENDOR_ID
        val isDownloadPid = isDownloadVendor && device.productId in DOWNLOAD_MODE_PIDS

        return when {
            hasFastboot -> DetectedProtocol.FASTBOOT
            isDownloadPid && !hasMtp -> DetectedProtocol.DOWNLOAD_MODE
            isDownloadVendor && hasCdc && !hasAdb && !hasMtp -> DetectedProtocol.DOWNLOAD_MODE
            hasAdb -> DetectedProtocol.ADB
            isDownloadVendor && hasCdc -> DetectedProtocol.DOWNLOAD_MODE
            hasMtp -> DetectedProtocol.MTP_ONLY
            else -> DetectedProtocol.UNKNOWN
        }
    }

    fun describeDevice(device: UsbDevice): String {
        val vidHex = "0x%04X".format(device.vendorId)
        val pidHex = "0x%04X".format(device.productId)
        val name = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            device.productName ?: device.manufacturerName ?: "USB Device"
        } else {
            device.deviceName
        }
        return "$name ($vidHex:$pidHex)"
    }

    /**
     * Opens the external USB device via official Android UsbManager (No Root required),
     * sets configuration cleanly for legacy Android 8.1 hosts (e.g. SM-G610F) and legacy targets
     * (e.g. GT-N7100), force-claims interfaces, clears endpoint halts, initializes CDC-ACM,
     * and drains stale RX bytes before protocol handshake.
     */
    fun bindDevice(
        usbManager: UsbManager,
        device: UsbDevice,
        onLog: (String) -> Unit
    ): UsbBinding {
        val protocol = detectProtocol(device)
        onLog("تشخیص خودکار هسته USB: $protocol — ${describeDevice(device)} (Interfaces=${device.interfaceCount})")

        if (protocol == DetectedProtocol.MTP_ONLY) {
            throw IllegalStateException(
                "کابل OTG سالم است و گوشی شناسایی شد، اما فقط در حالت MTP است. گوشی مقصد را وارد Download Mode یا Fastboot کنید یا USB Debugging را روشن نمایید."
            )
        }

        val conn = usbManager.openDevice(device)
            ?: throw IllegalStateException("دسترسی به پورت USB صادر نشد. مجوز USB را تایید کنید.")

        // IMPORTANT: Do NOT call conn.setConfiguration(0) on an already-configured device!
        // Android's kernel already selects configuration 1 on plug-in; sending a second SET_CONFIGURATION
        // freezes Exynos S-Boot bootloaders (e.g. GT-N7100, SM-A510FD on Gadget Serial 0x04E8:0x685D).

        var ctrlIface: UsbInterface? = null
        var dataIface: UsbInterface? = null
        var epIn: UsbEndpoint? = null
        var epOut: UsbEndpoint? = null

        for (i in 0 until device.interfaceCount) {
            val iface = device.getInterface(i)
            val matchesTarget = when (protocol) {
                DetectedProtocol.FASTBOOT ->
                    iface.interfaceClass == ADB_CLASS &&
                        iface.interfaceSubclass == ADB_SUBCLASS &&
                        iface.interfaceProtocol == FASTBOOT_PROTOCOL

                DetectedProtocol.ADB ->
                    iface.interfaceClass == ADB_CLASS &&
                        iface.interfaceSubclass == ADB_SUBCLASS &&
                        iface.interfaceProtocol == ADB_PROTOCOL

                DetectedProtocol.DOWNLOAD_MODE -> {
                    if (iface.interfaceClass == CDC_CTRL_CLASS && ctrlIface == null) {
                        ctrlIface = iface
                    }
                    iface.interfaceClass == CDC_DATA_CLASS
                }

                else -> true
            }

            if (matchesTarget && dataIface == null) {
                val (inEp, outEp) = findBulkEndpoints(iface)
                if (inEp != null && outEp != null) {
                    dataIface = iface
                    epIn = inEp
                    epOut = outEp
                }
            }
        }

        // Fallback: any interface with Bulk IN + Bulk OUT (important for legacy GT-N7100 / composite descriptors)
        if (dataIface == null) {
            for (i in 0 until device.interfaceCount) {
                val iface = device.getInterface(i)
                val (inEp, outEp) = findBulkEndpoints(iface)
                if (inEp != null && outEp != null) {
                    dataIface = iface
                    epIn = inEp
                    epOut = outEp
                    break
                }
            }
        }

        if (dataIface == null || epIn == null || epOut == null) {
            conn.close()
            throw IllegalStateException("کانال Bulk IN/OUT روی دستگاه یافت نشد.")
        }

        // Claim data interface first (detaches kernel cdc_acm driver cleanly — exact Heimdall BridgeManager order)
        if (!conn.claimInterface(dataIface, true)) {
            conn.close()
            throw IllegalStateException("تصاحب اینترفیس داده #${dataIface.id} ناموفق بود.")
        }

        val binding = UsbBinding(
            device = device,
            connection = conn,
            protocol = protocol,
            dataInterface = dataIface,
            controlInterface = ctrlIface,
            epIn = epIn,
            epOut = epOut
        )

        // Exact Heimdall BridgeManager::SetupDeviceInterface() for Download Mode:
        // Call SET_INTERFACE(dataIface) (libusb_set_interface_alt_setting) WITHOUT sending CDC DTR/RTS first!
        // Legacy Exynos 4412 S-Boot (GT-N7100) stalls when DTR/RTS (0x21, 0x22) is sent before handshake,
        // whereas SET_INTERFACE cleanly arms S3C-HSOTG endpoints and syncs DATA0/DATA1 toggles on both sides.
        if (protocol == DetectedProtocol.DOWNLOAD_MODE) {
            binding.resetDataInterface(onLog)
            try {
                Thread.sleep(80)
            } catch (_: InterruptedException) {
            }
        }

        onLog(
            "اتصال سخت‌افزاری برقرار شد: Interface #${dataIface.id} · OUT=0x%02X · IN=0x%02X (%dB)"
                .format(epOut.address, epIn.address, epIn.maxPacketSize)
        )

        return binding
    }

    private fun findBulkEndpoints(iface: UsbInterface): Pair<UsbEndpoint?, UsbEndpoint?> {
        var inEp: UsbEndpoint? = null
        var outEp: UsbEndpoint? = null
        for (j in 0 until iface.endpointCount) {
            val ep = iface.getEndpoint(j)
            if (ep.type == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                if (ep.direction == UsbConstants.USB_DIR_IN) inEp = ep
                if (ep.direction == UsbConstants.USB_DIR_OUT) outEp = ep
            }
        }
        return inEp to outEp
    }
}

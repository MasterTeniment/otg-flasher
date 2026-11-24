package com.otgflashtool.app.protocol

import android.content.ContentResolver
import android.net.Uri
import com.otgflashtool.app.usb.UsbBinding
import java.io.BufferedInputStream
import kotlin.math.min

data class FastbootResult(
    val status: String,
    val message: String,
    val infoLines: List<String>
)

class FastbootProtocol(private val binding: UsbBinding) {

    private fun readPacket(timeoutMs: Int = 15000): String {
        val len = maxOf(64, binding.epIn.maxPacketSize)
        val buf = ByteArray(len)
        val n = binding.connection.bulkTransfer(binding.epIn, buf, buf.size, timeoutMs)
        if (n <= 0) throw IllegalStateException("پاسخی از Fastboot دریافت نشد (Timeout/کد: $n)")
        return String(buf, 0, n, Charsets.US_ASCII).trimEnd('\u0000')
    }

    fun sendCommand(command: String, timeoutMs: Int = 20000): FastbootResult {
        val payload = command.toByteArray(Charsets.US_ASCII)
        val sent = binding.connection.bulkTransfer(binding.epOut, payload, payload.size, timeoutMs)
        if (sent != payload.size) {
            throw IllegalStateException("ارسال دستور Fastboot ناموفق بود.")
        }

        val infos = mutableListOf<String>()
        while (true) {
            val reply = readPacket(timeoutMs)
            val prefix = reply.take(4)
            val body = reply.drop(4)
            when (prefix) {
                "INFO" -> infos.add(body)
                "OKAY", "FAIL", "DATA" -> return FastbootResult(prefix, body, infos)
                else -> throw IllegalStateException("پاسخ ناشناخته Fastboot: $reply")
            }
        }
    }

    fun flashUri(
        resolver: ContentResolver,
        partition: String,
        uri: Uri,
        fileSize: Long,
        onLog: (String) -> Unit,
        onProgress: (sent: Long, total: Long) -> Unit
    ) {
        val hexSize = "%08x".format(fileSize)
        onLog("fastboot > download:$hexSize")
        val dlResp = sendCommand("download:$hexSize")
        if (dlResp.status != "DATA") {
            throw IllegalStateException("بوت‌لودر درخواست download را رد کرد: ${dlResp.message}")
        }

        resolver.openInputStream(uri)?.use { rawIn ->
            val input = BufferedInputStream(rawIn, 256 * 1024)
            val buf = ByteArray(16384) // Safe Android USBFS max buffer size (16 KiB)
            var sentTotal = 0L
            while (sentTotal < fileSize) {
                val want = min(buf.size.toLong(), fileSize - sentTotal).toInt()
                val read = input.read(buf, 0, want)
                if (read <= 0) break
                var offset = 0
                while (offset < read) {
                    val written = binding.connection.bulkTransfer(binding.epOut, buf, offset, read - offset, 15000)
                    if (written <= 0) throw IllegalStateException("خطای ارسال داده Fastboot ($written)")
                    offset += written
                    sentTotal += written
                }
                onProgress(sentTotal, fileSize)
            }
        } ?: throw IllegalStateException("امکان باز کردن فایل ایمیج وجود ندارد.")

        val dlDone = readPacket(30000)
        if (!dlDone.startsWith("OKAY")) {
            throw IllegalStateException("تایید دریافت داده ناموفق بود: $dlDone")
        }

        onLog("fastboot > flash:$partition")
        val flashResp = sendCommand("flash:$partition", timeoutMs = 120000)
        flashResp.infoLines.forEach { onLog("INFO: $it") }
        if (flashResp.status != "OKAY") {
            throw IllegalStateException("خطای نوشتن $partition: ${flashResp.message}")
        }
        onLog("OKAY — پارتیشن $partition با موفقیت فلش شد.")
    }
}

package com.otgflashtool.app.protocol

import android.content.ContentResolver
import android.net.Uri
import android.util.Base64
import com.otgflashtool.app.usb.UsbBinding
import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import java.math.BigInteger
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import kotlin.math.ceil
import kotlin.math.min

class AdbProtocol(private val binding: UsbBinding) {
    companion object {
        private const val A_CNXN = 0x4E584E43
        private const val A_AUTH = 0x48545541
        private const val A_OPEN = 0x4E45504F
        private const val A_OKAY = 0x59414B4F
        private const val A_CLSE = 0x45534C43
        private const val A_WRTE = 0x45545257

        private const val AUTH_TOKEN = 1
        private const val AUTH_SIGNATURE = 2
        private const val AUTH_RSAPUBLICKEY = 3

        private const val VERSION = 0x01000001
        private const val MAX_DATA = 256 * 1024
    }

    private data class Packet(val cmd: Int, val arg0: Int, val arg1: Int, val payload: ByteArray)

    private var localIdCounter = 1

    private fun checksum(data: ByteArray): Int {
        var sum = 0
        for (b in data) sum += (b.toInt() and 0xFF)
        return sum
    }

    private fun sendPacket(cmd: Int, arg0: Int, arg1: Int, payload: ByteArray = ByteArray(0)) {
        val header = ByteBuffer.allocate(24).order(ByteOrder.LITTLE_ENDIAN).apply {
            putInt(cmd)
            putInt(arg0)
            putInt(arg1)
            putInt(payload.size)
            putInt(checksum(payload))
            putInt(cmd xor -1)
        }.array()

        val hOut = binding.connection.bulkTransfer(binding.epOut, header, 24, 10000)
        if (hOut != 24) throw IllegalStateException("خطای ارسال هدر ADB ($hOut)")
        if (payload.isNotEmpty()) {
            val pOut = binding.connection.bulkTransfer(binding.epOut, payload, payload.size, 10000)
            if (pOut != payload.size) throw IllegalStateException("خطای ارسال داده ADB ($pOut)")
        }
    }

    private fun readExact(length: Int, timeoutMs: Int): ByteArray {
        val unit = maxOf(64, binding.epIn.maxPacketSize)
        val out = ByteArray(length)
        var got = 0
        while (got < length) {
            val remain = length - got
            val req = ceil(remain.toDouble() / unit).toInt() * unit
            val tmp = ByteArray(req)
            val n = binding.connection.bulkTransfer(binding.epIn, tmp, tmp.size, timeoutMs)
            if (n <= 0) throw IllegalStateException("Timeout دریافت پاسخ ADB ($n)")
            val copy = min(n, remain)
            System.arraycopy(tmp, 0, out, got, copy)
            got += copy
        }
        return out
    }

    private fun readPacket(timeoutMs: Int = 15000): Packet {
        val header = ByteBuffer.wrap(readExact(24, timeoutMs)).order(ByteOrder.LITTLE_ENDIAN)
        val cmd = header.getInt(0)
        val arg0 = header.getInt(4)
        val arg1 = header.getInt(8)
        val len = header.getInt(12)
        val payload = if (len > 0) readExact(len, timeoutMs) else ByteArray(0)
        return Packet(cmd, arg0, arg1, payload)
    }

    private fun buildAndroidPublicKey(pub: RSAPublicKey): ByteArray {
        val words = 64
        val n = pub.modulus
        val e = pub.publicExponent
        val mod32 = BigInteger.ONE.shiftLeft(32)
        val n0 = n.and(mod32.subtract(BigInteger.ONE))
        val n0Inv = mod32.subtract(n0.modInverse(mod32)).mod(mod32)
        val r = BigInteger.ONE.shiftLeft(2048).mod(n)
        val rr = r.multiply(r).mod(n)

        val bb = ByteBuffer.allocate(4 + 4 + words * 4 + words * 4 + 4).order(ByteOrder.LITTLE_ENDIAN)
        bb.putInt(words)
        bb.putInt(n0Inv.toInt())

        var tmpN = n
        for (i in 0 until words) {
            bb.putInt(tmpN.and(BigInteger.valueOf(0xFFFFFFFFL)).toInt())
            tmpN = tmpN.shiftRight(32)
        }
        var tmpRr = rr
        for (i in 0 until words) {
            bb.putInt(tmpRr.and(BigInteger.valueOf(0xFFFFFFFFL)).toInt())
            tmpRr = tmpRr.shiftRight(32)
        }
        bb.putInt(e.toInt())

        val b64 = Base64.encodeToString(bb.array(), Base64.NO_WRAP)
        return "$b64 otgflashtool@android\u0000".toByteArray(Charsets.US_ASCII)
    }

    private fun signToken(token: ByteArray, priv: RSAPrivateKey): ByteArray {
        val sha1Prefix = byteArrayOf(
            0x30, 0x21, 0x30, 0x09, 0x06, 0x05, 0x2B, 0x0E,
            0x03, 0x02, 0x1A, 0x05, 0x00, 0x04, 0x14
        )
        val keyBytes = 256
        val em = ByteArray(keyBytes)
        val psLen = keyBytes - sha1Prefix.size - token.size - 3
        em[0] = 0x00
        em[1] = 0x01
        for (i in 0 until psLen) em[2 + i] = 0xFF.toByte()
        em[2 + psLen] = 0x00
        System.arraycopy(sha1Prefix, 0, em, 3 + psLen, sha1Prefix.size)
        System.arraycopy(token, 0, em, 3 + psLen + sha1Prefix.size, token.size)

        val m = BigInteger(1, em)
        val sigInt = m.modPow(priv.privateExponent, priv.modulus)
        val raw = sigInt.toByteArray()
        val out = ByteArray(keyBytes)
        val srcStart = maxOf(0, raw.size - keyBytes)
        val copyLen = min(raw.size, keyBytes)
        System.arraycopy(raw, srcStart, out, keyBytes - copyLen, copyLen)
        return out
    }

    fun connectAndAuth(onLog: (String) -> Unit): String {
        val kpg = KeyPairGenerator.getInstance("RSA")
        kpg.initialize(2048)
        val pair = kpg.generateKeyPair()
        val pub = pair.public as RSAPublicKey
        val priv = pair.private as RSAPrivateKey

        onLog("ارسال CNXN به adbd گوشی مقصد...")
        sendPacket(A_CNXN, VERSION, MAX_DATA, "host::features=cmd,shell_v2\u0000".toByteArray(Charsets.US_ASCII))

        var sentSig = false
        for (i in 0 until 6) {
            val pkt = readPacket(if (sentSig) 45000 else 12000)
            if (pkt.cmd == A_CNXN) {
                val banner = String(pkt.payload, Charsets.UTF_8).trimEnd('\u0000')
                onLog("اتصال ADB برقرار شد: $banner")
                return banner
            }
            if (pkt.cmd == A_AUTH && pkt.arg0 == AUTH_TOKEN) {
                if (!sentSig) {
                    sendPacket(A_AUTH, AUTH_SIGNATURE, 0, signToken(pkt.payload, priv))
                    sentSig = true
                } else {
                    onLog("روی صفحه گوشی مقصد گزینه «Allow USB debugging» را تایید کنید...")
                    sendPacket(A_AUTH, AUTH_RSAPUBLICKEY, 0, buildAndroidPublicKey(pub))
                }
            }
        }
        throw IllegalStateException("احراز هویت ADB تایید نشد.")
    }

    fun shell(command: String): String {
        val localId = localIdCounter++
        sendPacket(A_OPEN, localId, 0, "shell:$command\u0000".toByteArray(Charsets.UTF_8))
        val out = ByteArrayOutputStream()
        while (true) {
            val pkt = readPacket(20000)
            if (pkt.cmd == A_WRTE && pkt.arg1 == localId) {
                out.write(pkt.payload)
                sendPacket(A_OKAY, localId, pkt.arg0)
            } else if (pkt.cmd == A_CLSE && pkt.arg1 == localId) {
                sendPacket(A_CLSE, localId, pkt.arg0)
                break
            }
        }
        return out.toString("UTF-8")
    }

    fun reboot(target: String) {
        val localId = localIdCounter++
        sendPacket(A_OPEN, localId, 0, "reboot:$target\u0000".toByteArray(Charsets.US_ASCII))
    }

    /**
     * Sends a flashable .zip (e.g. Magisk-.zip or OTA/ROM .zip) to a target device in
     * Recovery ADB Sideload mode using AOSP sideload-v2 (random-access 64 KiB blocks)
     * with automatic fallback to legacy streaming sideload.
     */
    fun sideloadUri(
        resolver: ContentResolver,
        uri: Uri,
        fileSize: Long,
        onLog: (String) -> Unit,
        onProgress: (sent: Long, total: Long) -> Unit
    ) {
        require(fileSize > 0L) { "حجم فایل زیپ نامعتبر است." }
        val blockSize = 65536
        val localId = localIdCounter++
        onLog("شروع ADB Sideload (sideload-v2:$fileSize)...")
        sendPacket(A_OPEN, localId, 0, "sideload-v2:$fileSize\u0000".toByteArray(Charsets.US_ASCII))

        val openResp = readPacket(30000)
        if (openResp.cmd == A_CLSE) {
            // Fallback to legacy streaming sideload:<size>
            onLog("ریکاورى از sideload-v2 پشتیبانی نکرد — تلاش با حالت کلاسیک sideload:$fileSize...")
            val legacyId = localIdCounter++
            sendPacket(A_OPEN, legacyId, 0, "sideload:$fileSize\u0000".toByteArray(Charsets.US_ASCII))
            val legResp = readPacket(20000)
            if (legResp.cmd != A_OKAY) {
                throw IllegalStateException("گوشی مقصد در حالت ADB Sideload نیست (پاسخ: 0x%X).".format(legResp.cmd))
            }
            val remoteId = legResp.arg0
            resolver.openInputStream(uri)?.use { input ->
                val buf = ByteArray(blockSize)
                var sentTotal = 0L
                while (sentTotal < fileSize) {
                    val n = input.read(buf)
                    if (n <= 0) break
                    val chunk = if (n == buf.size) buf else buf.copyOf(n)
                    sendPacket(A_WRTE, legacyId, remoteId, chunk)
                    val ack = readPacket(60000)
                    if (ack.cmd == A_CLSE) break
                    sentTotal += n
                    onProgress(min(sentTotal, fileSize), fileSize)
                }
            } ?: throw IllegalStateException("امکان خواندن فایل زیپ وجود ندارد.")
            sendPacket(A_CLSE, legacyId, remoteId)
            onLog("ارسال فایل ADB Sideload کامل شد.")
            return
        }

        if (openResp.cmd != A_OKAY) {
            throw IllegalStateException("گوشی مقصد آماده دریافت Sideload نیست (کد پاسخ: 0x%X).".format(openResp.cmd))
        }

        val remoteId = openResp.arg0
        val pendingMessages = ArrayDeque<String>()
        var sentBlocksBytes = 0L

        fun waitForOkayAndQueueWrte(timeoutMs: Int = 60000) {
            while (true) {
                val pkt = readPacket(timeoutMs)
                if (pkt.cmd == A_OKAY && pkt.arg1 == localId) {
                    return
                } else if (pkt.cmd == A_WRTE && pkt.arg1 == localId) {
                    sendPacket(A_OKAY, localId, remoteId)
                    pendingMessages.addLast(String(pkt.payload, Charsets.US_ASCII).trim('\u0000', ' ', '\r', '\n'))
                } else if (pkt.cmd == A_CLSE && pkt.arg1 == localId) {
                    pendingMessages.addLast("DONEDONE")
                    return
                }
            }
        }

        fun nextControlMessage(): String {
            if (pendingMessages.isNotEmpty()) return pendingMessages.removeFirst()
            while (true) {
                val pkt = readPacket(120000)
                if (pkt.cmd == A_WRTE && pkt.arg1 == localId) {
                    sendPacket(A_OKAY, localId, remoteId)
                    return String(pkt.payload, Charsets.US_ASCII).trim('\u0000', ' ', '\r', '\n')
                } else if (pkt.cmd == A_CLSE && pkt.arg1 == localId) {
                    return "DONEDONE"
                }
            }
        }

        val pfd = runCatching { resolver.openFileDescriptor(uri, "r") }.getOrNull()
        val buf = ByteArray(blockSize)

        try {
            val fis = pfd?.let { FileInputStream(it.fileDescriptor) }
            val channel = fis?.channel
            try {
                while (true) {
                    val msg = nextControlMessage()
                    if (msg == "DONEDONE") {
                        runCatching { sendPacket(A_CLSE, localId, remoteId) }
                        onProgress(fileSize, fileSize)
                        onLog("ADB Sideload با موفقیت روی ریکاورى تمام شد (DONEDONE).")
                        return
                    }
                    if (msg == "FAILFAIL") {
                        runCatching { sendPacket(A_CLSE, localId, remoteId) }
                        throw IllegalStateException("ریکاورى مقصد فایل را رد کرد (FAILFAIL).")
                    }
                    val blockIdx = msg.toLongOrNull()
                        ?: throw IllegalStateException("پیام نامعتبر از Sideload ریکاوری: $msg")
                    val offset = blockIdx * blockSize.toLong()
                    val want = min(blockSize.toLong(), maxOf(0L, fileSize - offset)).toInt()
                    val payload: ByteArray
                    if (channel != null) {
                        channel.position(offset)
                        var readTotal = 0
                        while (readTotal < want) {
                            val r = fis.read(buf, readTotal, want - readTotal)
                            if (r <= 0) break
                            readTotal += r
                        }
                        payload = if (readTotal == buf.size) buf.copyOf() else buf.copyOf(readTotal)
                    } else {
                        payload = resolver.openInputStream(uri)?.use { stream ->
                            var skipped = 0L
                            while (skipped < offset) {
                                val s = stream.skip(offset - skipped)
                                if (s <= 0) {
                                    if (stream.read() == -1) break
                                    skipped++
                                } else {
                                    skipped += s
                                }
                            }
                            var readTotal = 0
                            while (readTotal < want) {
                                val r = stream.read(buf, readTotal, want - readTotal)
                                if (r <= 0) break
                                readTotal += r
                            }
                            buf.copyOf(readTotal)
                        } ?: throw IllegalStateException("خطا در خواندن بلاک $blockIdx از فایل زیپ.")
                    }

                    sendPacket(A_WRTE, localId, remoteId, payload)
                    waitForOkayAndQueueWrte()
                    sentBlocksBytes = min(fileSize, sentBlocksBytes + payload.size.toLong())
                    onProgress(sentBlocksBytes, fileSize)
                }
            } finally {
                runCatching { fis?.close() }
            }
        } finally {
            runCatching { pfd?.close() }
        }
    }
}

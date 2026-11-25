package com.otgflashtool.app.protocol

import android.content.ContentResolver
import android.net.Uri
import com.otgflashtool.app.usb.UsbBinding
import java.io.BufferedInputStream
import java.io.FileInputStream
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.min

data class PitEntry(
    val binaryType: Int,
    val deviceType: Int,
    val partitionId: Int,
    val blockCount: Int,
    val partitionName: String,
    val flashFilename: String
)

data class DownloadModeHandshakeInfo(
    val protocolVersion: Int,
    val maxPacketSize: Int,
    val supportsCustomPartSize: Boolean
)

data class DownloadModeSlotInput(
    val slotKey: String, // BL, AP, CP, CSC
    val uri: Uri,
    val fileName: String,
    val fileSize: Long
)

private data class PlannedMember(
    val slot: DownloadModeSlotInput,
    val memberName: String,
    val dataOffset: Long,
    val tarDataSize: Long,
    val uncompressedSize: Long,
    val isLz4: Boolean,
    val pitEntry: PitEntry
)

/**
 * Pure-Kotlin streaming LZ4 Frame decoder (magic 0x184D2204) for modern Samsung .img.lz4 / .bin.lz4 firmware.
 */
private class Lz4FrameInputStream(private val source: InputStream) : InputStream() {
    private var contentChecksumFlag = false
    private var blockChecksumFlag = false
    private var eof = false
    private var outBuf = ByteArray(0)
    private var outPos = 0
    private var outLen = 0
    private val history = ByteArray(65536)
    private var historyPos = 0
    private var historySize = 0
    var declaredContentSize: Long? = null
        private set

    init {
        parseHeader()
    }

    private fun readByteStrict(): Int {
        val b = source.read()
        if (b < 0) throw IllegalStateException("پایان غیرمنتظره استریم LZ4")
        return b
    }

    private fun readFullyStrict(dst: ByteArray, len: Int) {
        var off = 0
        while (off < len) {
            val r = source.read(dst, off, len - off)
            if (r < 0) throw IllegalStateException("پایان غیرمنتظره داده در فایل LZ4")
            off += r
        }
    }

    private fun readUInt32LE(): Int {
        val b0 = readByteStrict()
        val b1 = readByteStrict()
        val b2 = readByteStrict()
        val b3 = readByteStrict()
        return b0 or (b1 shl 8) or (b2 shl 16) or (b3 shl 24)
    }

    private fun readUInt64LE(): Long {
        val lo = readUInt32LE().toLong() and 0xFFFFFFFFL
        val hi = readUInt32LE().toLong() and 0xFFFFFFFFL
        return lo or (hi shl 32)
    }

    private fun parseHeader() {
        val magic = readUInt32LE()
        if (magic != 0x184D2204) {
            throw IllegalStateException("فرمت فایل LZ4 معتبر نیست (Magic: 0x%08X)".format(magic))
        }
        val flg = readByteStrict()
        val bd = readByteStrict()
        val blockMaxCode = (bd ushr 4) and 0x07
        val maxBlockSize = when (blockMaxCode) {
            4 -> 64 * 1024
            5 -> 256 * 1024
            6 -> 1024 * 1024
            7 -> 4 * 1024 * 1024
            else -> 4 * 1024 * 1024
        }
        outBuf = ByteArray(maxBlockSize)
        blockChecksumFlag = (flg and 0x10) != 0
        contentChecksumFlag = (flg and 0x04) != 0
        val hasContentSize = (flg and 0x08) != 0
        val hasDictId = (flg and 0x01) != 0
        if (hasContentSize) {
            declaredContentSize = readUInt64LE()
        }
        if (hasDictId) {
            readUInt32LE()
        }
        readByteStrict() // Header checksum
    }

    private fun pushHistory(byteVal: Byte) {
        history[historyPos] = byteVal
        historyPos = (historyPos + 1) and 0xFFFF
        if (historySize < 65536) historySize++
    }

    private fun fillNextBlock(): Boolean {
        if (eof) return false
        val blockSizeWord = readUInt32LE()
        if (blockSizeWord == 0) {
            if (contentChecksumFlag) {
                readUInt32LE()
            }
            eof = true
            return false
        }
        val isUncompressed = (blockSizeWord and Int.MIN_VALUE) != 0
        val compSize = blockSizeWord and 0x7FFFFFFF
        val compBuf = ByteArray(compSize)
        readFullyStrict(compBuf, compSize)
        if (blockChecksumFlag) {
            readUInt32LE()
        }

        outPos = 0
        if (isUncompressed) {
            if (compSize > outBuf.size) outBuf = ByteArray(compSize)
            System.arraycopy(compBuf, 0, outBuf, 0, compSize)
            outLen = compSize
            for (i in 0 until compSize) pushHistory(outBuf[i])
            return true
        }

        var ip = 0
        var op = 0
        while (ip < compSize) {
            val token = compBuf[ip++].toInt() and 0xFF
            var litLen = token ushr 4
            if (litLen == 15) {
                var s: Int
                do {
                    s = compBuf[ip++].toInt() and 0xFF
                    litLen += s
                } while (s == 255)
            }
            if (op + litLen > outBuf.size) {
                outBuf = outBuf.copyOf(maxOf(outBuf.size * 2, op + litLen + 65536))
            }
            System.arraycopy(compBuf, ip, outBuf, op, litLen)
            for (i in 0 until litLen) pushHistory(outBuf[op + i])
            ip += litLen
            op += litLen
            if (ip >= compSize) break

            val offset = (compBuf[ip].toInt() and 0xFF) or ((compBuf[ip + 1].toInt() and 0xFF) shl 8)
            ip += 2
            var matchLen = (token and 0x0F) + 4
            if ((token and 0x0F) == 15) {
                var s: Int
                do {
                    s = compBuf[ip++].toInt() and 0xFF
                    matchLen += s
                } while (s == 255)
            }
            if (op + matchLen > outBuf.size) {
                outBuf = outBuf.copyOf(maxOf(outBuf.size * 2, op + matchLen + 65536))
            }
            for (i in 0 until matchLen) {
                val srcIdx = (historyPos - offset) and 0xFFFF
                val b = history[srcIdx]
                outBuf[op++] = b
                pushHistory(b)
            }
        }
        outLen = op
        return true
    }

    override fun read(): Int {
        val one = ByteArray(1)
        val r = read(one, 0, 1)
        return if (r <= 0) -1 else (one[0].toInt() and 0xFF)
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        if (len == 0) return 0
        if (outPos >= outLen) {
            if (!fillNextBlock()) return -1
        }
        val avail = outLen - outPos
        val take = min(avail, len)
        System.arraycopy(outBuf, outPos, b, off, take)
        outPos += take
        return take
    }
}

private class BoundedInputStream(
    private val delegate: InputStream,
    private var remaining: Long
) : InputStream() {
    override fun read(): Int {
        if (remaining <= 0L) return -1
        val b = delegate.read()
        if (b >= 0) remaining--
        return b
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        if (remaining <= 0L) return -1
        val toRead = min(len.toLong(), remaining).toInt()
        val r = delegate.read(b, off, toRead)
        if (r > 0) remaining -= r
        return r
    }

    fun drainRemaining() {
        val skipBuf = ByteArray(16384)
        while (remaining > 0L) {
            val s = delegate.skip(remaining)
            if (s > 0L) {
                remaining -= s
            } else {
                val r = delegate.read(skipBuf, 0, min(skipBuf.size.toLong(), remaining).toInt())
                if (r < 0) break
                remaining -= r
            }
        }
    }
}

class DownloadModeProtocol(private val binding: UsbBinding) {
    companion object {
        // 4-byte wire-level Download Mode bootloader handshake magic
        private val HANDSHAKE_MAGIC = byteArrayOf(0x4F, 0x44, 0x49, 0x4E)
        private const val PACKET_SIZE = 1024
        private const val CMD_SESSION = 0x64
        private const val CMD_PIT = 0x65
        private const val CMD_FLASH = 0x66
        private const val CMD_END = 0x67

        private const val SESSION_BEGIN = 0x00
        private const val SESSION_TOTAL_BYTES = 0x02
        private const val SESSION_FILE_PART_SIZE = 0x05

        private const val PIT_DUMP_REQ = 0x01
        private const val PIT_DUMP_PART = 0x02
        private const val PIT_DUMP_END = 0x03

        private const val FLASH_START = 0x00
        private const val FLASH_SEQ_START = 0x02
        private const val FLASH_SEQ_END = 0x03

        private const val END_SESSION = 0x00
        private const val END_REBOOT = 0x01

        private const val PIT_MAGIC = 0x12349876
        private const val TAR_BLOCK = 512

        // Android Linux USBFS MAX_USBFS_BUFFER_SIZE is 16384 bytes (16 KiB = 32 * 512B).
        // Slicing every bulkTransfer to <=16 KiB avoids kernel -1 (EINVAL/ENOMEM) failures on Android hosts!
        private const val ANDROID_USBFS_MAX_CHUNK = 16384

        // Safe OTG file-part size (128 KiB = 8 * 16 KiB)
        private const val OTG_FILE_PART_SIZE = 131072
        private const val SEQUENCE_MAX_PARTS_V3 = 240 // 30 MiB per sequence (Loke v3 e.g. SM-G610F, SM-A137F)
        private const val SEQUENCE_MAX_PARTS_V2 = 64  // 8 MiB per sequence (Loke v2 e.g. GT-N7100, GT-I9300)
    }

    private var supportsCustomPartSize = false
    private var sessionActive = false
    private var cachedHandshakeInfo: DownloadModeHandshakeInfo? = null

    private fun buildCmd(cmd: Int, sub: Int, a1: Int = 0, a2: Int = 0, a3: Int = 0): ByteArray {
        val buf = ByteBuffer.allocate(PACKET_SIZE).order(ByteOrder.LITTLE_ENDIAN)
        buf.putInt(cmd)
        buf.putInt(sub)
        buf.putInt(a1)
        buf.putInt(a2)
        buf.putInt(a3)
        return buf.array()
    }

    private fun writeAll(data: ByteArray, length: Int = data.size, timeoutMs: Int = 30000) {
        var sent = 0
        var retriedInitialHalt = false
        while (sent < length) {
            val sliceLen = min(ANDROID_USBFS_MAX_CHUNK, length - sent)
            val written = binding.connection.bulkTransfer(binding.epOut, data, sent, sliceLen, timeoutMs)
            if (written <= 0) {
                if (sent == 0 && !retriedInitialHalt) {
                    retriedInitialHalt = true
                    binding.clearEndpointHalts()
                    try {
                        Thread.sleep(60)
                    } catch (_: InterruptedException) {
                    }
                    continue
                }
                throw IllegalStateException(
                    "خطا در ارسال داده Bulk OUT (کد: $written در بایت $sent/$length). اگر قبلاً خطای فلش رخ داده، گوشی مقصد را با «ولوم پایین + پاور» ریستارت کرده و مجدد وارد Download Mode کنید."
                )
            }
            sent += written
        }
    }

    private fun readReply(minLen: Int = 8, timeoutMs: Int = 15000): ByteArray {
        val unit = maxOf(64, binding.epIn.maxPacketSize)
        val reqLen = ceil(maxOf(minLen, unit).toDouble() / unit).toInt() * unit
        val buf = ByteArray(reqLen)

        // Samsung Exynos S-Boot (GT-N7100 / SM-A510FD) occasionally sends a 0-byte ZLP before the real payload.
        for (zlpTry in 1..3) {
            val read = binding.connection.bulkTransfer(binding.epIn, buf, buf.size, timeoutMs)
            if (read > 0) {
                return buf.copyOf(read)
            }
            if (read < 0) {
                throw IllegalStateException("پاسخی از گوشی در Download Mode دریافت نشد (Timeout/کد: $read)")
            }
            // read == 0 (ZLP): retry immediately
            Thread.sleep(30)
        }
        throw IllegalStateException("پاسخ خالی (0 بایت ZLP) از گوشی در Download Mode دریافت شد.")
    }

    private fun tryBeginSession(): ByteArray? {
        return runCatching {
            writeAll(buildCmd(CMD_SESSION, SESSION_BEGIN, 0), timeoutMs = 4500)
            readReply(8, timeoutMs = 6000)
        }.getOrNull()
    }

    /**
     * Heimdall-accurate session handshake supporting both Loke v2 (GT-N7100, SM-A510FD v2)
     * and Loke v3/v4 (SM-G610F, SM-A137F, modern devices):
     * 1) Wire Handshake -> LOKE (drain stale RX first)
     * 2) If target timed out on LOKE (e.g. already past LOKE from an earlier session), clear halts & probe SESSION_BEGIN BEFORE touching CDC DTR/RTS
     * 3) If still unresponsive, reset data interface, safely attempt claimed CDC-ACM fallback, and retry LOKE -> SESSION_BEGIN
     */
    fun handshake(onLog: (String) -> Unit): DownloadModeHandshakeInfo {
        var lokeOk = false
        var lastReply = ""
        var cachedSessionRep: ByteArray? = null

        binding.drainStaleRx()

        onLog("ارسال بسته Handshake (تلاش 1 از ۲)...")
        try {
            writeAll(HANDSHAKE_MAGIC, timeoutMs = 2200)
            val rep = readReply(4, timeoutMs = 2200)
            lastReply = String(rep, Charsets.US_ASCII).trim('\u0000', ' ', '\r', '\n')
            if (lastReply.startsWith("LOKE")) {
                lokeOk = true
            }
        } catch (e: Exception) {
            onLog("تلاش 1 Handshake ناموفق: ${e.message}")
        }

        // Before altering CDC control lines (which can stall Exynos S-Boot), check if target is already in an active session
        if (!lokeOk) {
            onLog("بررسی نشست فعال قبلی با SESSION_BEGIN پیش از بازنشانی CDC...")
            binding.clearEndpointHalts()
            binding.drainStaleRx()
            val probeRep = tryBeginSession()
            if (probeRep != null && probeRep.size >= 8) {
                val probeCmd = ByteBuffer.wrap(probeRep).order(ByteOrder.LITTLE_ENDIAN).getInt(0)
                if (probeCmd == CMD_SESSION) {
                    cachedSessionRep = probeRep
                    onLog("نشست قبلی Download Mode شناسایی و بازیابی شد!")
                }
            }
        }

        if (!lokeOk && cachedSessionRep == null) {
            onLog("بازنشانی اینترفیس داده و بررسی CDC-ACM برای تلاش دوم...")
            binding.clearEndpointHalts()
            binding.resetDataInterface(onLog)
            binding.enableCdcControlFallback(onLog)
            Thread.sleep(120)
            binding.drainStaleRx()

            onLog("ارسال بسته Handshake (تلاش 2 از ۲)...")
            try {
                writeAll(HANDSHAKE_MAGIC, timeoutMs = 2500)
                val rep = readReply(4, timeoutMs = 2500)
                lastReply = String(rep, Charsets.US_ASCII).trim('\u0000', ' ', '\r', '\n')
                if (lastReply.startsWith("LOKE")) {
                    lokeOk = true
                }
            } catch (e: Exception) {
                onLog("تلاش 2 Handshake ناموفق: ${e.message}")
            }
        }

        if (lokeOk) {
            onLog("پاسخ LOKE دریافت شد! شروع نشست Download Mode...")
        } else if (cachedSessionRep == null) {
            onLog("پاسخ LOKE دریافت نشد (\"$lastReply\") — تلاش نهایی با SESSION_BEGIN...")
        }

        // BeginSession (0x64, 0x00, 0)
        val sessionRep = cachedSessionRep ?: run {
            writeAll(buildCmd(CMD_SESSION, SESSION_BEGIN, 0), timeoutMs = 5000)
            readReply(8, timeoutMs = 8000)
        }
        if (sessionRep.size < 8) {
            throw IllegalStateException("پاسخ SESSION_BEGIN ناقص است (${sessionRep.size} بایت). گوشی مقصد را با ولوم پایین+پاور ریستارت و مجدد وارد Download Mode کنید.")
        }
        val view = ByteBuffer.wrap(sessionRep).order(ByteOrder.LITTLE_ENDIAN)
        val cmd = view.getInt(0)
        val sessionResult = view.getInt(4)
        if (cmd != CMD_SESSION) {
            throw IllegalStateException("خطا در شروع نشست Download Mode (پاسخ: 0x%X). گوشی مقصد را ریستارت کرده و مجدد وارد Downloading... شوید.".format(cmd))
        }

        val upperVer = (sessionResult ushr 16) and 0xFFFF
        val protoVer = when {
            upperVer >= 3 -> upperVer
            upperVer == 2 -> 2
            sessionResult == 0 -> 2
            else -> 3
        }
        supportsCustomPartSize = protoVer >= 3
        onLog("نسخه پروتکل بوت‌لودر: v$protoVer.0 (کد خام: 0x%08X)".format(sessionResult))

        if (supportsCustomPartSize) {
            onLog("تنظیم اندازه بسته انتقال روی 128KB (پروتکل v$protoVer)...")
            runCatching {
                writeAll(buildCmd(CMD_SESSION, SESSION_FILE_PART_SIZE, OTG_FILE_PART_SIZE), timeoutMs = 4000)
                val partRep = ByteBuffer.wrap(readReply(8, timeoutMs = 4000)).order(ByteOrder.LITTLE_ENDIAN)
                val partRes = partRep.getInt(4)
                if (partRes != 0) {
                    onLog("هشدار کد پاسخ FilePartSize: $partRes (ادامه با 128KB)")
                }
            }.onFailure { err ->
                onLog("دستگاه پاسخ FilePartSize نداد (${err.message}) — ادامه در حالت سازگار 128KB")
                supportsCustomPartSize = false
            }
        }

        onLog("نشست Download Mode v$protoVer.0 آماده است (بسته: 128KB · USBFS: 16KB).")
        val info = DownloadModeHandshakeInfo(protoVer, OTG_FILE_PART_SIZE, supportsCustomPartSize)
        sessionActive = true
        cachedHandshakeInfo = info
        return info
    }

    fun ensureHandshake(onLog: (String) -> Unit): DownloadModeHandshakeInfo {
        val cached = cachedHandshakeInfo
        if (sessionActive && cached != null) {
            return cached
        }
        return handshake(onLog)
    }

    fun endSessionOnly(onLog: (String) -> Unit) {
        if (!sessionActive) return
        try {
            writeAll(buildCmd(CMD_END, END_SESSION), timeoutMs = 4000)
            runCatching { readReply(8, timeoutMs = 4000) }
            onLog("نشست Download Mode به‌صورت ایمن بسته شد (آماده اتصال مجدد بدون ریبوت).")
        } catch (e: Exception) {
            onLog("هشدار پایان نشست: ${e.message}")
        } finally {
            sessionActive = false
        }
    }

    fun dumpPit(onLog: (String) -> Unit): List<PitEntry> {
        ensureHandshake(onLog)
        onLog("درخواست خواندن جدول پارتیشن (PIT)...")
        writeAll(buildCmd(CMD_PIT, PIT_DUMP_REQ))
        val sizeRep = ByteBuffer.wrap(readReply(8)).order(ByteOrder.LITTLE_ENDIAN)
        val pitSize = sizeRep.getInt(4)
        if (pitSize <= 28 || pitSize > 1024 * 1024) {
            throw IllegalStateException("اندازه PIT نامعتبر است: $pitSize")
        }

        val pitBytes = ByteArray(pitSize)
        val partSize = 500
        val totalParts = ceil(pitSize.toDouble() / partSize).toInt()

        for (i in 0 until totalParts) {
            writeAll(buildCmd(CMD_PIT, PIT_DUMP_PART, i))
            val remain = pitSize - i * partSize
            val chunk = readReply(min(partSize, remain))
            val copyLen = min(chunk.size, remain)
            System.arraycopy(chunk, 0, pitBytes, i * partSize, copyLen)
        }

        writeAll(buildCmd(CMD_PIT, PIT_DUMP_END))
        readReply(8)

        val entries = parsePit(pitBytes)
        onLog("جدول PIT خوانده شد (${entries.size} پارتیشن).")
        return entries
    }

    private fun parsePit(bytes: ByteArray): List<PitEntry> {
        val bb = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
        val magic = bb.getInt(0)
        if (magic != PIT_MAGIC) {
            throw IllegalStateException("امضای باینری PIT نامعتبر است.")
        }
        val count = bb.getInt(4)
        val list = mutableListOf<PitEntry>()
        for (i in 0 until count) {
            val base = 28 + i * 132
            if (base + 132 > bytes.size) break
            val binaryType = bb.getInt(base)
            val deviceType = bb.getInt(base + 4)
            val id = bb.getInt(base + 8)
            val blocks = bb.getInt(base + 24)
            val partName = readCString(bytes, base + 36, 32)
            val flashName = readCString(bytes, base + 68, 32)
            list.add(PitEntry(binaryType, deviceType, id, blocks, partName, flashName))
        }
        return list
    }

    private fun readCString(bytes: ByteArray, offset: Int, maxLen: Int): String {
        var end = offset
        val limit = min(offset + maxLen, bytes.size)
        while (end < limit && bytes[end].toInt() != 0) end++
        return String(bytes, offset, end - offset, Charsets.US_ASCII).trim()
    }

    private fun parseLz4ContentSizeFromBytes(header: ByteArray, got: Int): Long? {
        if (got < 7) return null
        val bb = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN)
        if (bb.getInt(0) != 0x184D2204) return null
        val flg = header[4].toInt() and 0xFF
        val hasContentSize = (flg and 0x08) != 0
        if (!hasContentSize || got < 15) return null
        return bb.getLong(6)
    }

    /**
     * Ultra-fast O(1) seek TAR header scanner using ParcelFileDescriptor + FileChannel.position().
     * Scans a 5 GB AP.tar.md5 in ~15 milliseconds instead of reading 5 GB sequentially!
     */
    private fun scanSlots(
        resolver: ContentResolver,
        slots: List<DownloadModeSlotInput>,
        pit: List<PitEntry>,
        onLog: (String) -> Unit
    ): List<PlannedMember> {
        val planned = mutableListOf<PlannedMember>()
        for (slot in slots) {
            val lower = slot.fileName.lowercase()
            val isTar = lower.endsWith(".tar") || lower.endsWith(".tar.md5")

            val pfd = runCatching { resolver.openFileDescriptor(slot.uri, "r") }.getOrNull()
            if (pfd != null) {
                pfd.use { fd ->
                    FileInputStream(fd.fileDescriptor).use { fis ->
                        val ch = fis.channel
                        val fileLen = ch.size()
                        if (isTar) {
                            val headerBuf = ByteBuffer.allocate(TAR_BLOCK)
                            var offset = 0L
                            while (offset + TAR_BLOCK <= fileLen) {
                                ch.position(offset)
                                headerBuf.clear()
                                var read = 0
                                while (read < TAR_BLOCK) {
                                    val r = ch.read(headerBuf)
                                    if (r <= 0) break
                                    read += r
                                }
                                if (read < TAR_BLOCK) break
                                val header = headerBuf.array()
                                if (header.all { it.toInt() == 0 }) break

                                val memberName = readCString(header, 0, 100).removePrefix("./")
                                val sizeOct = readCString(header, 124, 12)
                                val memberSize = sizeOct.toLongOrNull(8) ?: 0L
                                val typeFlag = header[156].toInt() and 0xFF
                                val dataStart = offset + TAR_BLOCK
                                val nextOffset = dataStart + memberSize + tarPadding(memberSize)

                                if (memberName.isEmpty()) break
                                if (memberSize > 0L && (typeFlag == 0 || typeFlag == '0'.code)) {
                                    val pitMatch = matchPit(memberName, pit)
                                    if (pitMatch == null) {
                                        onLog("[${slot.slotKey}] رد فایل غیرپارتیشنی: $memberName")
                                    } else {
                                        val isLz4 = memberName.lowercase().endsWith(".lz4")
                                        var uncompSize = memberSize
                                        var valid = true
                                        if (isLz4) {
                                            val lz4Buf = ByteBuffer.allocate(15)
                                            ch.position(dataStart)
                                            val lzRead = ch.read(lz4Buf)
                                            val declared = parseLz4ContentSizeFromBytes(lz4Buf.array(), lzRead)
                                            if (declared != null && declared > 0L) {
                                                uncompSize = declared
                                            } else {
                                                onLog("[${slot.slotKey}] هشدار: $memberName هدر Content-Size ندارد و رد شد.")
                                                valid = false
                                            }
                                        }
                                        if (valid) {
                                            planned.add(
                                                PlannedMember(
                                                    slot = slot,
                                                    memberName = memberName,
                                                    dataOffset = dataStart,
                                                    tarDataSize = memberSize,
                                                    uncompressedSize = uncompSize,
                                                    isLz4 = isLz4,
                                                    pitEntry = pitMatch
                                                )
                                            )
                                        }
                                    }
                                }
                                offset = nextOffset
                            }
                        } else {
                            val pitMatch = matchPit(slot.fileName, pit)
                                ?: throw IllegalStateException("فایل ${slot.fileName} با هیچ پارتیشنی در PIT گوشی مطابقت نداشت.")
                            val isLz4 = lower.endsWith(".lz4")
                            val uncompSize = if (isLz4) {
                                val lz4Buf = ByteBuffer.allocate(15)
                                ch.position(0L)
                                val lzRead = ch.read(lz4Buf)
                                parseLz4ContentSizeFromBytes(lz4Buf.array(), lzRead)
                                    ?: throw IllegalStateException("فایل LZ4 بدون هدر Content-Size پشتیبانی نمی‌شود.")
                            } else {
                                fileLen
                            }
                            planned.add(
                                PlannedMember(
                                    slot = slot,
                                    memberName = slot.fileName,
                                    dataOffset = 0L,
                                    tarDataSize = fileLen,
                                    uncompressedSize = uncompSize,
                                    isLz4 = isLz4,
                                    pitEntry = pitMatch
                                )
                            )
                        }
                    }
                }
            } else {
                // Fallback if provider doesn't expose seekable FileDescriptor
                resolver.openInputStream(slot.uri)?.use { rawIn ->
                    val input = BufferedInputStream(rawIn, 256 * 1024)
                    if (isTar) {
                        val header = ByteArray(TAR_BLOCK)
                        var offset = 0L
                        while (true) {
                            val read = readFully(input, header, TAR_BLOCK)
                            if (read < TAR_BLOCK || header.all { it.toInt() == 0 }) break
                            val memberName = readCString(header, 0, 100).removePrefix("./")
                            val sizeOct = readCString(header, 124, 12)
                            val memberSize = sizeOct.toLongOrNull(8) ?: 0L
                            val typeFlag = header[156].toInt() and 0xFF
                            val dataStart = offset + TAR_BLOCK
                            val pad = tarPadding(memberSize)
                            if (memberName.isEmpty()) break

                            if (memberSize > 0L && (typeFlag == 0 || typeFlag == '0'.code)) {
                                val pitMatch = matchPit(memberName, pit)
                                if (pitMatch != null) {
                                    val isLz4 = memberName.lowercase().endsWith(".lz4")
                                    var uncompSize = memberSize
                                    var valid = true
                                    if (isLz4) {
                                        val bounded = BoundedInputStream(input, memberSize)
                                        val lzHdr = ByteArray(15)
                                        val got = readFully(bounded, lzHdr, min(15L, memberSize).toInt())
                                        val declared = parseLz4ContentSizeFromBytes(lzHdr, got)
                                        bounded.drainRemaining()
                                        if (declared != null && declared > 0L) {
                                            uncompSize = declared
                                        } else {
                                            valid = false
                                        }
                                    } else {
                                        skipExact(input, memberSize)
                                    }
                                    if (valid) {
                                        planned.add(
                                            PlannedMember(slot, memberName, dataStart, memberSize, uncompSize, isLz4, pitMatch)
                                        )
                                    }
                                } else {
                                    skipExact(input, memberSize)
                                }
                            } else if (memberSize > 0L) {
                                skipExact(input, memberSize)
                            }
                            if (pad > 0) skipExact(input, pad)
                            offset = dataStart + memberSize + pad
                        }
                    } else {
                        val pitMatch = matchPit(slot.fileName, pit)
                            ?: throw IllegalStateException("فایل ${slot.fileName} با PIT گوشی مطابقت نداشت.")
                        planned.add(
                            PlannedMember(slot, slot.fileName, 0L, slot.fileSize, slot.fileSize, false, pitMatch)
                        )
                    }
                } ?: throw IllegalStateException("امکان خواندن فایل ${slot.fileName} وجود ندارد.")
            }
        }
        return planned
    }

    /**
     * Flashes 1 to 4 Download Mode firmware slots (BL, AP, CP, CSC) sequentially in a single session.
     * - O(1) instant FileChannel seek scanning (no USB idle timeout on large 3-5 GB AP files)
     * - 16 KiB Android USBFS bulkTransfer slicing (fixes Bulk OUT -1 on Android hosts!)
     * - Exact Heimdall padded file parts + padded FLASH_SEQ_START + PIT binaryType/deviceType + 120s commit timeout
     */
    fun flashSlots(
        resolver: ContentResolver,
        slots: List<DownloadModeSlotInput>,
        pit: List<PitEntry>,
        autoReboot: Boolean,
        onLog: (String) -> Unit,
        onProgress: (sent: Long, total: Long, label: String) -> Unit
    ) {
        if (slots.isEmpty()) {
            throw IllegalStateException("هیچ فایلی در اسلات‌های BL / AP / CP / CSC انتخاب نشده است.")
        }
        ensureHandshake(onLog)
        if (pit.isEmpty()) {
            throw IllegalStateException("جدول PIT گوشی خوانده نشده است. ابتدا PIT را بخوانید یا دوباره متصل شوید.")
        }

        onLog("اسکن سریع پارتیشن‌های ${slots.size} فایل با جدول PIT گوشی...")
        val plan = scanSlots(resolver, slots, pit, onLog)
        if (plan.isEmpty()) {
            throw IllegalStateException("هیچ پارتیشن منطبقی با PIT گوشی در فایل‌های انتخاب‌شده پیدا نشد.")
        }

        val totalUncompressedBytes = plan.sumOf { it.uncompressedSize }
        onLog(
            "تایید شد: ${plan.size} پارتیشن آماده فلش (حجم کل: ${"%.2f".format(Locale.US, totalUncompressedBytes / (1024.0 * 1024.0))} MB)."
        )

        // Send Total Bytes (0x64, 0x02) — Note: FilePartSize (0x64, 0x05) was already set during handshake()!
        val totalLo = (totalUncompressedBytes and 0xFFFFFFFFL).toInt()
        val totalHi = (totalUncompressedBytes ushr 32).toInt()
        writeAll(buildCmd(CMD_SESSION, SESSION_TOTAL_BYTES, totalLo, totalHi))
        readReply(8, timeoutMs = 15000)

        var totalSent = 0L

        for (item in plan) {
            val pitEntry = item.pitEntry
            val label = "[${item.slot.slotKey}] ${pitEntry.partitionName}"
            // Follow Heimdall FlashAction.cpp strictly: destination comes from the phone's own PIT binaryType!
            val isModemDestination = pitEntry.binaryType == 1

            onLog(
                "شروع فلش ${item.memberName} ← $label (ID=#${pitEntry.partitionId}, DevType=${pitEntry.deviceType}, BinType=${pitEntry.binaryType}, حجم=${"%.2f".format(Locale.US, item.uncompressedSize / (1024.0 * 1024.0))} MB)..."
            )

            val pfd = runCatching { resolver.openFileDescriptor(item.slot.uri, "r") }.getOrNull()
            if (pfd != null) {
                pfd.use { fd ->
                    FileInputStream(fd.fileDescriptor).use { fis ->
                        fis.channel.position(item.dataOffset)
                        val buffered = BufferedInputStream(fis, 256 * 1024)
                        val bounded = BoundedInputStream(buffered, item.tarDataSize)
                        val payloadStream: InputStream = if (item.isLz4) Lz4FrameInputStream(bounded) else bounded
                        flashSinglePartition(
                            input = payloadStream,
                            exactByteSize = item.uncompressedSize,
                            pitEntry = pitEntry,
                            isModemDestination = isModemDestination,
                            onLog = onLog
                        ) { added ->
                            totalSent += added
                            onProgress(min(totalSent, totalUncompressedBytes), totalUncompressedBytes, label)
                        }
                    }
                }
            } else {
                resolver.openInputStream(item.slot.uri)?.use { rawIn ->
                    val buffered = BufferedInputStream(rawIn, 256 * 1024)
                    if (item.dataOffset > 0L) skipExact(buffered, item.dataOffset)
                    val bounded = BoundedInputStream(buffered, item.tarDataSize)
                    val payloadStream: InputStream = if (item.isLz4) Lz4FrameInputStream(bounded) else bounded
                    flashSinglePartition(
                        input = payloadStream,
                        exactByteSize = item.uncompressedSize,
                        pitEntry = pitEntry,
                        isModemDestination = isModemDestination,
                        onLog = onLog
                    ) { added ->
                        totalSent += added
                        onProgress(min(totalSent, totalUncompressedBytes), totalUncompressedBytes, label)
                    }
                } ?: throw IllegalStateException("امکان باز کردن فایل ${item.slot.fileName} وجود ندارد.")
            }

            onLog("پارتیشن $label با موفقیت نوشته و تایید شد.")
        }

        // Exact Heimdall two-step EndSession (0x67, 0x00) + Optional Reboot (0x67, 0x01)
        onLog("ارسال دستور پایان نشست Download Mode (EndSession)...")
        writeAll(buildCmd(CMD_END, END_SESSION))
        readReply(8, timeoutMs = 15000)
        sessionActive = false

        if (autoReboot) {
            onLog("ارسال فرمان راه‌اندازی مجدد گوشی (Auto Reboot)...")
            writeAll(buildCmd(CMD_END, END_REBOOT))
            runCatching { readReply(8, timeoutMs = 10000) }
        }
        onLog("PASS! عملیات فلش با موفقیت کامل شد.")
    }

    private fun flashSinglePartition(
        input: InputStream,
        exactByteSize: Long,
        pitEntry: PitEntry,
        isModemDestination: Boolean,
        onLog: (String) -> Unit,
        onBytesSent: (Long) -> Unit
    ) {
        writeAll(buildCmd(CMD_FLASH, FLASH_START))
        readReply(8, timeoutMs = 15000)

        val partSize = OTG_FILE_PART_SIZE
        val maxParts = if (supportsCustomPartSize) SEQUENCE_MAX_PARTS_V3 else SEQUENCE_MAX_PARTS_V2
        val maxSeqBytes = maxParts.toLong() * partSize.toLong()
        var remaining = exactByteSize
        val partBuf = ByteArray(partSize)

        while (remaining > 0L) {
            val effectiveSeqBytes = min(maxSeqBytes, remaining).toInt()
            val isLastSequence = (remaining - effectiveSeqBytes) <= 0L
            val partCount = ceil(effectiveSeqBytes.toDouble() / partSize.toDouble()).toInt()
            val paddedSeqBytes = partCount * partSize

            writeAll(buildCmd(CMD_FLASH, FLASH_SEQ_START, paddedSeqBytes))
            readReply(8, timeoutMs = 15000)

            var seqRemaining = effectiveSeqBytes
            for (partIndex in 0 until partCount) {
                val want = min(partSize, seqRemaining)
                if (want < partSize) {
                    partBuf.fill(0.toByte(), want, partSize)
                }
                val got = readFully(input, partBuf, want)
                if (got != want) {
                    throw IllegalStateException("پایان غیرمنتظره فایل حین خواندن پارتیشن ${pitEntry.partitionName} (خوانده: $got از $want)")
                }

                var partAckOk = false
                for (attempt in 1..3) {
                    writeAll(partBuf, partSize, timeoutMs = 30000)
                    try {
                        val rep = readReply(8, timeoutMs = 30000)
                        val bb = ByteBuffer.wrap(rep).order(ByteOrder.LITTLE_ENDIAN)
                        val repPart = bb.getInt(4)
                        if (repPart == partIndex) {
                            partAckOk = true
                            break
                        } else {
                            onLog("هشدار همگام‌سازی بسته (انتظار $partIndex، دریافت $repPart)...")
                        }
                    } catch (e: Exception) {
                        if (attempt == 3) throw e
                        binding.clearEndpointHalts()
                        onLog("پاک‌سازی خطای USB و تلاش مجدد بسته #$partIndex...")
                        Thread.sleep(200)
                    }
                }
                if (!partAckOk) {
                    throw IllegalStateException("تایید بسته #$partIndex از گوشی دریافت نشد.")
                }

                seqRemaining -= want
                onBytesSent(want.toLong())
            }

            val destination = if (isModemDestination) 1 else 0
            val endSeq = ByteBuffer.allocate(PACKET_SIZE).order(ByteOrder.LITTLE_ENDIAN).apply {
                putInt(CMD_FLASH)                 // offset 0: 0x66
                putInt(FLASH_SEQ_END)             // offset 4: 0x03
                putInt(destination)               // offset 8: 0 = Phone, 1 = Modem
                putInt(effectiveSeqBytes)         // offset 12: ACTUAL UNPADDED bytes in this sequence
                putInt(0)                         // offset 16: unknown1 = 0
                putInt(pitEntry.deviceType)       // offset 20: PIT deviceType (e.g. 2=eMMC, 8=UFS)
                if (destination == 0) {
                    putInt(pitEntry.partitionId)  // offset 24: partitionIdentifier
                    putInt(if (isLastSequence) 1 else 0) // offset 28: endOfFile
                } else {
                    putInt(if (isLastSequence) 1 else 0) // offset 24: endOfFile (Modem)
                }
            }.array()

            writeAll(endSeq, PACKET_SIZE, timeoutMs = 30000)
            readReply(8, timeoutMs = 120000)

            remaining -= effectiveSeqBytes
        }
    }

    private fun matchPit(fileName: String, pit: List<PitEntry>): PitEntry? {
        val clean = fileName.substringAfterLast('/').lowercase()
        if (clean.isEmpty() || clean.endsWith(".pit") || fileName.contains("meta-data/", ignoreCase = true)) {
            return null
        }
        val noLz4 = clean.removeSuffix(".lz4")
        val stem = noLz4.substringBeforeLast('.')

        pit.firstOrNull {
            val pf = it.flashFilename.lowercase()
            pf.isNotEmpty() && (pf == clean || pf == noLz4)
        }?.let { return it }

        pit.firstOrNull {
            val pn = it.partitionName.lowercase()
            pn.isNotEmpty() && (pn == stem || pn == noLz4)
        }?.let { return it }

        val alias = when (noLz4) {
            "sboot.bin" -> "bootloader"
            "modem.bin" -> "radio"
            "hidden.img" -> "hidden"
            "cache.img" -> "cache"
            "system.img" -> "system"
            "boot.img" -> "boot"
            "recovery.img" -> "recovery"
            else -> null
        }
        if (alias != null) {
            pit.firstOrNull { it.partitionName.equals(alias, ignoreCase = true) }?.let { return it }
        }
        return null
    }

    private fun tarPadding(size: Long): Long {
        val rem = size % TAR_BLOCK
        return if (rem == 0L) 0L else TAR_BLOCK - rem
    }

    private fun readFully(input: InputStream, buf: ByteArray, len: Int): Int {
        var off = 0
        while (off < len) {
            val r = input.read(buf, off, len - off)
            if (r < 0) break
            off += r
        }
        return off
    }

    private fun skipExact(input: InputStream, count: Long) {
        var left = count
        val discard = ByteArray(16384)
        while (left > 0L) {
            val s = input.skip(left)
            if (s <= 0L) {
                val r = input.read(discard, 0, min(discard.size.toLong(), left).toInt())
                if (r < 0) break
                left -= r
            } else {
                left -= s
            }
        }
    }
}

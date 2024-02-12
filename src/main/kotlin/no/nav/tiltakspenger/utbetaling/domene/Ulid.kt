package no.nav.tiltakspenger.utbetaling.domene

import ulid.ULID
import java.nio.ByteBuffer
import java.util.*

interface Ulid : Comparable<Ulid> {

    fun prefixPart(): String
    fun ulidPart(): String
    fun uuid(): UUID = ulidToUuid(ulidPart())
    override fun toString(): String
}

data class UlidBase(private val stringValue: String) : Ulid {

    companion object {
        fun random(prefix: String): UlidBase {
            require(prefix.isNotEmpty()) { "Prefiks er tom" }
            return UlidBase("${prefix}_${ULID.randomULID()}")
        }

        fun fromDb(stringValue: String) = UlidBase(stringValue)
    }

    init {
        require(stringValue.contains("_")) { "Ikke gyldig Id, skal bestå av to deler skilt med _" }
        require(stringValue.split("_").size == 2) { "Ikke gyldig Id, skal bestå av prefiks + ulid" }
        require(stringValue.split("_").first().isNotEmpty()) { "Ikke gyldig Id, prefiks er tom" }
        try {
            ULID.parseULID(stringValue.split("_").last())
        } catch (e: Exception) {
            throw IllegalArgumentException("Ikke gyldig Id, ulid er ugyldig")
        }
    }

    override fun prefixPart(): String = stringValue.split("_").first()
    override fun ulidPart(): String = stringValue.split("_").last()
    override fun toString() = stringValue
    override fun compareTo(other: Ulid) = this.toString().compareTo(other.toString())
}

data class VedtakId private constructor(private val ulid: UlidBase) : Ulid by ulid {
    companion object {
        private const val PREFIX = "ved"
        fun random() = VedtakId(ulid = UlidBase("${PREFIX}_${ULID.randomULID()}"))

        fun fromDb(stringValue: String) = VedtakId(ulid = UlidBase(stringValue))

        fun fromUUID(uuid: UUID) = VedtakId(ulid = UlidBase("${PREFIX}_${uuidToUlid(uuid)}"))
    }
}

data class SakId private constructor(private val ulid: UlidBase) : Ulid by ulid {
    companion object {
        private const val PREFIX = "sak"
        fun random() = SakId(ulid = UlidBase("${PREFIX}_${ULID.randomULID()}"))

        fun fromDb(stringValue: String) = SakId(ulid = UlidBase(stringValue))

        fun fromUUID(uuid: UUID) = SakId(ulid = UlidBase("${PREFIX}_${uuidToUlid(uuid)}"))
    }
}

data class BehandlingId private constructor(private val ulid: UlidBase) : Ulid by ulid {
    companion object {
        private const val PREFIX = "beh"
        fun random() = BehandlingId(ulid = UlidBase("${PREFIX}_${ULID.randomULID()}"))

        fun fromDb(stringValue: String) = BehandlingId(ulid = UlidBase(stringValue))

        fun fromUUID(uuid: UUID) = BehandlingId(ulid = UlidBase("${PREFIX}_${uuidToUlid(uuid)}"))
    }
}

private fun ulidToUuid(ulid: String): UUID {
    val (most, least) = ulidStringToLongs(ulid)
    return UUID(most, least)
}

private fun uuidToUlid(uuid: UUID): ULID {
    val buffer = ByteBuffer.allocate(16)
    buffer.putLong(uuid.mostSignificantBits)
    buffer.putLong(uuid.leastSignificantBits)
    return ULID.Factory().fromBytes(buffer.array())
}

private fun ulidStringToLongs(s: String): Pair<Long, Long> {
    val charMapBase32 = byteArrayOf(
        -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1,
        0, 1, 2, 3, 4, 5, 6, 7,
        8, 9, -1, -1, -1, -1, -1, -1,
        -1, 10, 11, 12, 13, 14, 15, 16,
        17, 1, 18, 19, 1, 20, 21, 0,
        22, 23, 24, 25, 26, -1, 27, 28,
        29, 30, 31, -1, -1, -1, -1, -1,
        -1, 10, 11, 12, 13, 14, 15, 16,
        17, 1, 18, 19, 1, 20, 21, 0,
        22, 23, 24, 25, 26, -1, 27, 28,
        29, 30, 31,
    )
    fun base32CharToByte(ch: Char): Byte = charMapBase32[ch.code]

    var mostSig = 0L
    var leastSig = 0L
    for (i in 0..25) {
        val v = base32CharToByte(s[i])
        val carry = leastSig ushr 59 // 64 - 5
        leastSig = leastSig shl 5
        leastSig = leastSig or v.toLong()
        mostSig = mostSig shl 5
        mostSig = mostSig or carry.toLong()
    }
    return Pair(mostSig, leastSig)
}

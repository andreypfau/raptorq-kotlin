package io.github.andreypfau.raptorq.math

import kotlin.experimental.xor

public interface GF256 {
    public operator fun get(index: Int): Octet

    public operator fun set(index: Int, value: Octet)

    public operator fun plus(other: GF256): GF256

    public operator fun times(x: Octet): GF256

    public fun plusTimes(other: GF256, x: Octet): GF256
}

public class ByteGF256 constructor(
    private val data: ByteArray,
    private val offset: Int = 0,
    public val size: Int = data.size
) : GF256 {
    public constructor(size: Int) : this(ByteArray(size))
    override fun get(index: Int): Octet = data[index + offset].toOctet()

    override fun set(index: Int, value: Octet) {
        data[index + offset] = value.toByte()
    }

    public fun fillZero() {
        data.fill(0, offset, offset + size)
    }

    override fun plus(other: GF256): ByteGF256 = plus(other as ByteGF256)

    public operator fun plus(other: ByteGF256): ByteGF256 = apply {
        for (i in 0 until size) {
            data[i + offset] = (data[i + offset] xor other.data[i])
        }
    }

    override fun times(x: Octet): ByteGF256 = apply {
        val table = Octet.MUL_PRE_CALC[x.toInt()]
        for (i in 0 until size) {
            data[i + offset] = table[data[i + offset].toInt()]
        }
    }

    override fun plusTimes(other: GF256, x: Octet): GF256 = plusTimes(other as ByteGF256, x)

    public fun plusTimes(other: ByteGF256, x: Octet): ByteGF256 = apply {
        val table = Octet.MUL_PRE_CALC[x.toInt()]
        for (i in 0 until size) {
            data[i + offset] = data[i + offset] xor table[other.data[i + offset].toInt()]
        }
    }

    public fun withoutPrefix(len: Int): ByteGF256 = ByteGF256(data, offset + len, size - len)

    public fun copyInto(destination: ByteGF256) {
        data.copyInto(destination.data, 0, offset, offset + size)
    }
}

package io.github.andreypfau.raptorq.math

import kotlin.experimental.and
import kotlin.experimental.or

private const val alignment = 256 / Byte.SIZE_BITS

public class MatrixGF2(
    public val rows: Int,
    public val cols: Int
) {
    private val stride = ((cols + 7) / 8 + alignment - 1) / alignment * alignment
    private val data = ByteArray(stride * rows + alignment - 1)

    public operator fun get(row: Int): ByteGF256 = ByteGF256(data, row * stride, stride)

    public operator fun get(row: Int, col: Int): Boolean =
        data[row * stride + col / 8] and (1 shl (col % 8)).toByte() != 0.toByte()

    public fun setOne(row: Int, col: Int) {
        data[row * stride + col / 8] = data[row * stride + col / 8] or (1 shl (col % 8)).toByte()
    }

   public fun toGf256(): MatrixGF256 {
        val newData = Array(rows) { ByteGF256(ByteArray(cols)) }
        for (r in 0 until rows) {
            for (c in 0 until ((cols + 7) / 8 + 3) / 4 * 4) {
                newData[r][c] = (data[c / 8].toInt() ushr (c % 8) and 1).toByte().toOctet()
            }
        }
        return MatrixGF256(newData)
    }
}

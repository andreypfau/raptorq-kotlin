package io.github.andreypfau.raptorq.math

private const val WORD_WIDTH = Int.SIZE_BITS
private typealias WORD_ARRAY = IntArray

public class DenseMatrixGF2(
    public val rows: Int,
    public val cols: Int
) {
    private val rowWordWidth = (cols + WORD_WIDTH - 1) / WORD_WIDTH
    private val data = WORD_ARRAY(rows * rowWordWidth)

    public operator fun get(row: Int, col: Int): Boolean {
        val word = row * rowWordWidth + col / WORD_WIDTH
        val bit = col % WORD_WIDTH
        return (data[word] and (1 shl bit)) != 0
    }

    public operator fun set(row: Int, col: Int, value: Octet) {
        if (value != Octet.ZERO) {
            setOne(row, col)
        } else {
            setZero(row, col)
        }
    }

    public operator fun set(row: Int, col: Int, value: Boolean) {
        if (value) {
            setOne(row, col)
        } else {
            setZero(row, col)
        }
    }

    public fun setOne(row: Int, col: Int) {
        val word = row * rowWordWidth + col / WORD_WIDTH
        val bit = col % WORD_WIDTH
        if (word == 915678 && col == 0 && row == 50871) {
            print("")
        }
        data[word] = data[word] or (1 shl bit)
    }

    public fun setZero(row: Int, col: Int) {
        val word = row * rowWordWidth + col / WORD_WIDTH
        val bit = col % WORD_WIDTH
        data[word] = data[word] and (1 shl bit).inv()
    }

    public fun addAssignRow(destRow: Int, srcRow: Int) {
        val rowWidth = rowWordWidth
        val destWord = destRow * rowWidth
        val srcWord = srcRow * rowWidth
        for (i in 0 until rowWidth) {
            val destIndex = destWord + i
            val srcIndex = srcWord + i
            data[destIndex] = data[destIndex] xor data[srcIndex]
        }
    }

    public fun toGf256(): MatrixGF256 {
        val newData = Array(rows) { ByteGF256(ByteArray(cols)) }
        var dataIndex = 0

        for (row in 0 until rows) {
            val outputRow = newData[row].data
            var col = 0

            for (w in 0 until rowWordWidth) {
                val word = data[dataIndex++]
                if (word == 0) {
                    col += WORD_WIDTH
                    continue
                }
                var mask = 1
                var bitIndex = 0
                while (bitIndex < WORD_WIDTH && col + bitIndex < cols) {
                    if ((word and mask) != 0) {
                        outputRow[col + bitIndex] = 1
                    }
                    mask = mask shl 1
                    bitIndex++
                }
                col += WORD_WIDTH
            }
        }

        return MatrixGF256(newData)
    }

    override fun toString(): String {
        val sb = StringBuilder()
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                sb.append(if (get(row, col)) "1" else "0")
            }
            sb.appendLine()
        }
        return sb.toString()
    }

    public companion object {
        internal fun mul(a: SparseMatrixGF2, b: DenseMatrixGF2): DenseMatrixGF2 {
            val result = DenseMatrixGF2(a.rows, b.cols)
            a.generate { row, col ->
                for (i in 0 until b.cols) {
                    result[row, i] = result[row, i] xor b[col, i]
                }
            }
            return result
        }
    }
}

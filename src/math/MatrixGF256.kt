package io.github.andreypfau.raptorq.math

import io.github.andreypfau.raptorq.math.Octet.Companion.MUL_PRE_CALC
import kotlin.experimental.xor

public interface MatrixGF256View {
    public val cols: Int
    public val rows: Int

    public operator fun get(row: Int): ByteGF256
}

public class MatrixGF256(
    private val data: Array<ByteGF256>
) : MatrixGF256View {
    public constructor(rows: Int, cols: Int) : this(Array(rows) { ByteGF256(cols) })

    public override val rows: Int get() = data.size
    public override val cols: Int get() = data[0].size

    public override operator fun get(row: Int): ByteGF256 = data[row]

    public operator fun get(row: Int, col: Int): Octet = data[row][col]

    public operator fun set(row: Int, col: Int, value: Octet) {
        data[row][col] = value
    }

    public fun fillZero() {
        for (row in 0 until rows) {
            data[row].fillZero()
        }
    }

    public fun setFrom(m: MatrixGF256View, rowOffset: Int, colOffset: Int) {
        val to = blockView(rowOffset, colOffset, rows - rowOffset, cols - colOffset)
        for (i in 0 until m.rows) {
            m[i].copyInto(to[i])
        }
    }

    public fun applyRowPermutation(rowPermutation: IntArray): MatrixGF256 {
        val result = MatrixGF256(rows, cols)
        for (i in 0 until rows) {
            data[rowPermutation[i]].copyInto(result.data[i])
        }
        return result
    }

    public fun addAssignRow(
        destRow: Int,
        srcRow: Int,
        srcMatrix: MatrixGF256 = this
    ) {
        val destRowData = data[destRow].data
        val srcRowData = srcMatrix.data[srcRow].data
        for (i in 0 until cols) {
            destRowData[i] = destRowData[i] xor srcRowData[i]
        }
    }

    public fun addAssign(other: MatrixGF256) {
        for (row in 0 until rows) {
            val destRow = data[row].data
            val srcRow = other.data[row].data
            for (col in 0 until cols) {
                destRow[col] = destRow[col] xor srcRow[col]
            }
        }
    }

    public fun addMulAssignRow(
        destRow: Int,
        srcRow: Int,
        octet: Octet
    ) {
        addMulAssignRow(destRow, srcRow, this, octet)
    }

    public fun addMulAssignRow(
        destRow: Int,
        srcRow: Int,
        srcMatrix: MatrixGF256,
        octet: Octet
    ) {
        when (octet) {
            Octet.ZERO -> return
            Octet.ONE -> return addAssignRow(destRow, srcRow, srcMatrix)
            else -> {
                val destRowData = data[destRow].data
                val srcRowData = srcMatrix.data[srcRow].data
                val cols = cols
                val mul = octet.toInt()
                for (i in 0 until cols) {
                    destRowData[i] = destRowData[i] xor MUL_PRE_CALC[srcRowData[i].toInt() and 0xFF][mul]
                }
            }
        }
    }

    public fun mulAssignRow(
        row: Int,
        octet: Octet
    ) {
        val rowData = data[row]
        for (i in 0 until cols) {
            rowData[i] = rowData[i] * octet
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun toString(): String = buildString {
        append("\n")
        for (row in 0 until rows) {
            append(data[row].data.toHexString(HexFormat {
                bytes.byteSeparator = " "
            }))
            append('\n')
        }
    }

    public class BlockView(
        private val rowOffset: Int,
        private val colOffset: Int,
        override val rows: Int,
        override val cols: Int,
        private val matrix: MatrixGF256
    ) : MatrixGF256View {
        override fun get(row: Int): ByteGF256 = matrix.data[row + rowOffset].withoutPrefix(colOffset)
    }

    public fun blockView(rowOffset: Int, colOffset: Int, rowSize: Int, colSize: Int): BlockView {
        return BlockView(rowOffset, colOffset, rowSize, colSize, this)
    }

    public companion object {
        internal fun mul(a: SparseMatrixGF2, b: MatrixGF256): MatrixGF256 {
            val result = MatrixGF256(a.rows, b.cols)
            a.generate { row, col ->
                result.addAssignRow(row, col, b)
            }
            return result
        }
    }

}

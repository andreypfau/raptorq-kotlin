package io.github.andreypfau.raptorq.math

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
            result.data[i] = data[rowPermutation[i]]
        }
        return result
    }

    public operator fun plus(m: MatrixGF256): MatrixGF256 =  apply {
        for (i in 0 until rows) {
            val to = get(i)
            val from = m[i]
            from.copyInto(to)
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
}

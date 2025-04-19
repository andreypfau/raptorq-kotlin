package io.github.andreypfau.raptorq.math

import io.github.andreypfau.raptorq.generators.Generator
import io.github.andreypfau.raptorq.generators.GeneratorFunction
import io.github.andreypfau.raptorq.generators.PermutationGenerator
import io.github.andreypfau.raptorq.generators.TransposeGenerator

public class SparseMatrixGF2(
    public override val rows: Int,
    public override val cols: Int,
    private val data: IntArray
) : Generator {
    override val nonZeroes: Int get() = data.size
    private val colOffset = IntArray(cols + 1)

    public constructor(generator: Generator) : this(
        generator.rows,
        generator.cols,
        IntArray(generator.nonZeroes)
    ) {
        generator.generate { row, col ->
//            check(row < rows && col < cols) { "row: $row, col: $col" }
            colOffset[col + 1]++
        }
        for (i in 1 until colOffset.size) {
            colOffset[i] += colOffset[i - 1]
        }
        val colPos = colOffset.clone()

        generator.generate { row, col ->
            data[colPos[col]++] = row
        }

//        // todo: tests in SparseMatrixGF2Test
//        for (colI in 0 until cols) {
//            val c = col(colI).asSequence().toList()
//            for (j in 1 until c.size) {
////                println("c[j]=${c[j]} c[j-1]=${c[j - 1]} col_i=$colI")
//                check(c[j] > c[j - 1]) { "${c[j]} > ${c[j - 1]} row $colI" }
//            }
//        }
    }

    internal fun col(index: Int, from: Int = 0) = ColIterator(index, from)

    public fun blockForEach(
        rowStartIndex: Int,
        colStartIndex: Int,
        rowSize: Int,
        colSize: Int,
        block: GeneratorFunction
    ) {
        val colTill = colStartIndex + colSize
        val rowTill = rowStartIndex + rowSize
        for (colIndex in colStartIndex until colTill) {
            val col = col(colIndex, rowStartIndex)

            val c = colIndex - colStartIndex
            while (col.hasNext()) {
                val row = col.next()
                if (row >= rowTill) {
                    break
                }
                val r = row - rowStartIndex
                block(r, c)
            }
        }
    }

    override fun generate(block: GeneratorFunction) {
        blockForEach(0, 0, rows, cols, block)
    }

    public fun blockDense(rowStartIndex: Int, colStartIndex: Int, rowSize: Int, colSize: Int): DenseMatrixGF2 {
        val result = DenseMatrixGF2(rowSize, colSize)
        blockForEach(rowStartIndex, colStartIndex, rowSize, colSize) { row, col ->
            result.setOne(row, col)
        }
        return result
    }

    public fun blockSparse(rowStartIndex: Int, colStartIndex: Int, rowSize: Int, colSize: Int): SparseMatrixGF2 =
        SparseMatrixGF2(BlockView(rowStartIndex, colStartIndex, rowSize, colSize, this))

    public fun transpose(): SparseMatrixGF2 =
        SparseMatrixGF2(TransposeGenerator(this))

    public fun applyColPermutation(p: IntArray): SparseMatrixGF2 =
        SparseMatrixGF2(PermutationGenerator(this, p))

    public fun applyRowPermutation(p: IntArray): SparseMatrixGF2 {
        var result = this
        result = result.transpose()
        result = result.applyColPermutation(p)
        result = result.transpose()
        return result
    }

    public operator fun times(other: DenseMatrixGF2): DenseMatrixGF2 {
        return DenseMatrixGF2.mul(this, other)
    }

    public operator fun times(other: MatrixGF256): MatrixGF256 {
        return MatrixGF256.mul(this, other)
    }

    internal inner class ColIterator(
        index: Int,
        rowFrom: Int = 0
    ) : Iterator<Int> {
        var pointer = colOffset[index]
        val size = colOffset[index + 1] - pointer
        val lastIndex = pointer + size

        init {
            if (rowFrom > 0) {
                var left = pointer
                var right = lastIndex
                while (left < right) {
                    val mid = (left + right) ushr 1
                    if (data[mid] < rowFrom) {
                        left = mid + 1
                    } else {
                        right = mid
                    }
                }
                pointer = left
            }
        }

        override fun hasNext(): Boolean = pointer < lastIndex

        fun drop(n: Int) {
            pointer += n
        }

        override fun next(): Int {
            if (!hasNext()) throw NoSuchElementException()
            return data[pointer++]
        }
    }

    private class BlockView(
        private val rowOffset: Int,
        private val colOffset: Int,
        override val rows: Int,
        override val cols: Int,
        private val matrix: SparseMatrixGF2
    ) : Generator {
        override val nonZeroes: Int
            get() {
                var result = 0
                matrix.blockForEach(rowOffset, colOffset, rows, cols) { _, _ ->
                    result++
                }
                return result
            }

        override fun generate(block: GeneratorFunction) {
            matrix.blockForEach(rowOffset, colOffset, rows, cols, block)
        }
    }
}

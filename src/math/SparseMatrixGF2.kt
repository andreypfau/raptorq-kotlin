package io.github.andreypfau.raptorq.math

import io.github.andreypfau.raptorq.generators.Generator
import io.github.andreypfau.raptorq.generators.PermutationGenerator
import io.github.andreypfau.raptorq.generators.TransposeGenerator
import io.github.andreypfau.raptorq.generators.inversePermutation

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
            check(row < rows && col < cols) { "row: $row, col: $col" }
            colOffset[col + 1]++
        }
        for (i in 1 until colOffset.size) {
            colOffset[i] += colOffset[i - 1]
        }
        val colPos = colOffset.clone()

        generator.generate { row, col ->
            data[colPos[col]++] = row
        }

        // todo: tests in SparseMatrixGF2Test
        for (colI in 0 until cols) {
            val c = col(colI).asSequence().toList()
            for (j in 1 until c.size) {
//                println("c[j]=${c[j]} c[j-1]=${c[j - 1]} col_i=$colI")
                check(c[j] > c[j - 1]) { "${c[j]} > ${c[j - 1]} row $colI" }
            }
        }
    }

    public fun col(index: Int): Iterator<Int> = ColIterator(index)

    public fun blockForEach(
        rowStartIndex: Int,
        colStartIndex: Int,
        rowSize: Int,
        colSize: Int,
        block: (Int, Int) -> Unit
    ) {
        for (row in rowStartIndex until rowStartIndex + rowSize) {
            for (col in colStartIndex until colStartIndex + colSize) {
                block(row - rowStartIndex, col - colStartIndex)
            }
        }
    }

    override fun generate(block: (row: Int, col: Int) -> Unit) {
        blockForEach(0, 0, rows, cols, block)
    }

    public fun blockDense(rowStartIndex: Int, colStartIndex: Int, rowSize: Int, colSize: Int): MatrixGF2 {
        val result = MatrixGF2(rowSize, colSize)
        blockForEach(rowStartIndex, colStartIndex, rowSize, colSize) { row, col ->
            result.setOne(row, col)
        }
        return result
    }

    public fun blockSparse(rowStartIndex: Int, colStartIndex: Int, rowSize: Int, colSize: Int): SparseMatrixGF2 =
        SparseMatrixGF2(BlockView(rowStartIndex, colStartIndex, rowSize, colSize, this))

    public fun transpose(): SparseMatrixGF2 = SparseMatrixGF2(TransposeGenerator(this))

    public fun applyColPermutation(p: IntArray): SparseMatrixGF2 =
        SparseMatrixGF2(PermutationGenerator(this, p.inversePermutation()))

    public fun applyRowPermutation(p: IntArray): SparseMatrixGF2 = transpose().applyColPermutation(p).transpose()

    public operator fun times(matrix: MatrixGF2): MatrixGF2 {
        val result = MatrixGF2(rows, matrix.cols)
        generate { row, col ->
            result[row] + matrix[col]
        }
        return result
    }

    public operator fun times(matrixGF256: MatrixGF256): MatrixGF256 {
        val result = MatrixGF256(rows, matrixGF256.cols)
        generate { row, col ->
            result[row] + matrixGF256[col]
        }
        return result
    }

    private inner class ColIterator(
        index: Int
    ) : Iterator<Int> {
        var pointer = colOffset[index]
        val size = colOffset[index + 1] - pointer
        val lastIndex = pointer + size

        override fun hasNext(): Boolean = pointer < lastIndex

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

        override fun generate(block: (Int, Int) -> Unit) {
            matrix.blockForEach(rowOffset, colOffset, rows, cols, block)
        }
    }
}

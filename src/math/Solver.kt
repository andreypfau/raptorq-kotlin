package io.github.andreypfau.raptorq.math

import io.github.andreypfau.raptorq.generators.inversePermutation
import io.github.andreypfau.raptorq.rfc.Parameters
import io.github.andreypfau.raptorq.rfc.Rand
import kotlin.time.TimeSource

internal object Solver {
    internal var debug = false

    operator fun invoke(
        parameters: Parameters,
        symbols: Array<ByteArray>,
    ): MatrixGF256 {
        var timer = TimeSource.Monotonic.markNow()

        fun perfLog(message: () -> String) {
            if (!debug) return
            val elapsedNow = timer.elapsedNow()
            println("PERF: ${message()} $elapsedNow")
            timer = TimeSource.Monotonic.markNow()
        }

        // Solve linear system
        // A * C = D
        // C - intermeidate symbols
        // D - encoded symbols and restriction symbols.
        //
        // A:
        // +--------+-----+-------+
        // | LDPC1  | I_S |  LDPC2|
        // +--------+-----+-------+
        // | ENC                  |
        // +---------------+------+
        // | HDCP          | I_H  |
        // +---------------+------+
        check(parameters.kPadded <= symbols.size)
        val encodingRows = Array(symbols.size) {
            parameters.getEncodingRow(it)
        }

        // Generate matrix upper_A: sparse part of A, first S + K_padded rows.
        var upperA = parameters.upperA(encodingRows)
        var d = createD(parameters, symbols)
        perfLog { "Generate sparse matrix" }

        // Run indactivation decoding.
        // Lets call resulting lower triangualr matrix U
        val decodingResult = InactivationDecoding(upperA, parameters.piSymbols).invoke()
        perfLog { "Inactivation decoding" }
        val uSize = decodingResult.size

        val rowPermutation = decodingResult.pRows.let {
            while (it.size < d.rows) {
                it.add(it.size)
            }
            it.toIntArray()
        }
        val colPermutation = decodingResult.pCols.toIntArray()

        // +--------+---------+        +---------+
        // | U      | E       |        | D_upper |
        // +--------+---------+        +---------+
        // | G_left | G_right | * C =  |         |
        // +--------+--+------+        | D_lower |
        // |HDCP       | I_H  |        |         |
        // +-----------+------+        +---------+
        d = d.applyRowPermutation(rowPermutation)
        perfLog { "D: apply permutation" }

        upperA = upperA.applyRowPermutation(rowPermutation)
        upperA = upperA.applyColPermutation(colPermutation)
        perfLog { "A_upper: apply permutation" }

        val e = upperA.blockDense(0, uSize, uSize, parameters.intermediateSymbols - uSize)
        perfLog { "Calc E" }

        val c = MatrixGF256(upperA.cols, d.cols)
        c.setFrom(d.blockView(0, 0, uSize, d.cols), 0, 0)
        // Make U Identity matrix and calculate E and D_upper.
        for (i in 0 until uSize) {
            for (row in upperA.col(i)) {
                if (row == i) {
                    continue
                }
                if (row >= uSize) {
                    break
                }
                e.addAssignRow(row, i)
                d.addAssignRow(row, i)
            }
        }
        perfLog { "Triangular -> Identity" }

        // Calculate small_A_upper
        // small_A_upper = G_right
        val gLeft = upperA.blockSparse(uSize, 0, upperA.rows - uSize, uSize)
        perfLog { "GLeft" }

        val smallUpperA = MatrixGF256(upperA.rows - uSize, upperA.cols - uSize)
        upperA.blockForEach(uSize, uSize, upperA.rows - uSize, upperA.cols - uSize) { row, col ->
            smallUpperA[row, col] = Octet.ONE
        }

        smallUpperA.addAssign((gLeft * e).toGf256())
        perfLog { "small_A_upper" }

        // Calculate small_A_lower
        val smallLowerA = MatrixGF256(parameters.hdpcSymbols, upperA.cols - uSize)
        for (i in 1.. parameters.hdpcSymbols) {
            smallLowerA[smallLowerA.rows - i, smallLowerA.cols - i] = Octet.ONE
        }

        // Calculate HDPC_right and set it into small_A_lower
        var t = MatrixGF256(
            parameters.kPadded + parameters.ldpcSymbols,
            parameters.kPadded + parameters.ldpcSymbols - uSize
        )
        for (i in 0 until t.cols) {
            t[colPermutation[i + t.rows - t.cols], i] = Octet.ONE
        }
        val hdcpRight = hdpcMultiply(parameters.hdpcSymbols, t)
        smallLowerA.setFrom(hdcpRight, 0, 0)
        perfLog { "small_A_lower" }

        // small_A_lower += HDPC_left * E
        t = e.toGf256()
        perfLog { "t" }
        smallLowerA.addAssign(hdpcLeftMultiply(t, parameters, colPermutation))
        perfLog { "small_A_lower += HDPC_left * E" }

        val upperD = MatrixGF256(uSize, d.cols)
        upperD.setFrom(d.blockView(0, 0, upperD.rows, upperD.cols), 0, 0)

        // small_D_upper
        val smallUpperD = MatrixGF256(upperA.rows - uSize, d.cols)
        smallUpperD.setFrom(d.blockView(uSize, 0, smallUpperD.rows, smallUpperD.cols), 0, 0)
//        println("small_D_upper from d: $smallUpperD")
        val mul = gLeft * upperD
        smallUpperD.addAssign(mul)
        perfLog { "small_D_upper" }

        // small_D_lower
        val smallLowerD = MatrixGF256(parameters.hdpcSymbols, d.cols)
        smallLowerD.setFrom(d.blockView(upperA.rows, 0 , smallLowerD.rows, smallLowerD.cols), 0, 0)
        perfLog { "small_D_lower" }

        smallLowerD.addAssign(hdpcLeftMultiply(upperD, parameters, colPermutation))
        perfLog { "small_D_lower += HDPC_left * D_upper" }

        // Combine small_A from small_A_lower and small_A_upper
        val smallA = MatrixGF256(smallUpperA.rows + smallLowerA.rows, smallUpperA.cols)
        smallA.setFrom(smallUpperA, 0, 0)
        smallA.setFrom(smallLowerA, smallUpperA.rows, 0)
        perfLog { "Combine small_A" }

        // Combine small_D from small_D_lower and small_D_upper
        val smallD = MatrixGF256(smallUpperD.rows + smallLowerD.rows, smallUpperD.cols)
        smallD.setFrom(smallUpperD, 0, 0)
        smallD.setFrom(smallLowerD, smallUpperD.rows, 0)
        perfLog { "Combine small_D" }

        val smallC = gaussianElimination(smallA, smallD)?.also {
            perfLog { "gauss" }
        } ?: throw IllegalStateException("Gaussian elimination failed")

        c.setFrom(smallC.blockView(0, 0, c.rows - uSize, c.cols), uSize, 0)
        val upperAT = upperA.transpose()
        for (row in 0 until uSize) {
            for (col in upperAT.col(row)) {
                if (col == row) continue
                c.addAssignRow(row, col)
            }
        }
        perfLog { "Calc result" }

        val result = c.applyRowPermutation(colPermutation.inversePermutation())
        perfLog { "Apply inverse permutation" }

        return result
    }

    private fun createD(parameters: Parameters, symbols: Array<ByteArray>): MatrixGF256 {
        val symbolSize = symbols[0].size
        val d = MatrixGF256((parameters.ldpcSymbols + parameters.hdpcSymbols + symbols.size), symbolSize)
        var offset = parameters.ldpcSymbols
        for (symbol in symbols) {
            for (i in 0 until symbolSize) {
                d[offset, i] = symbol[i].toOctet()
            }
            offset++
        }
        return d
    }

    private fun hdpcLeftMultiply(
        m: MatrixGF256,
        parameters: Parameters,
        colPermutation: IntArray
    ): MatrixGF256 {
        val t = MatrixGF256(parameters.kPadded + parameters.ldpcSymbols, m.cols)
        for (i in 0 until m.rows) {
            m[i].copyInto(t[colPermutation[i]])
        }
        return hdpcMultiply(parameters.hdpcSymbols, t)
    }

    private fun hdpcMultiply(rows: Int, v: MatrixGF256): MatrixGF256 {
        val alpha = Octet.ALPHA
        for (i in 1 until v.rows) {
            v.addMulAssignRow(i, i - 1, alpha)
        }

        val u = MatrixGF256(rows, v.cols)
        for (i in 0 until rows) {
            u.addMulAssignRow(i, v.rows - 1, v, Octet.octExp(i % 255))
        }

        for (col in 0 until v.rows - 1) {
            val a = Rand.rand(col + 1, 6, rows)
            val b = (a + Rand.rand(col + 1, 7, rows - 1) + 1) % rows
            u.addAssignRow(a, col, v)
            u.addAssignRow(b, col, v)
        }

        return u
    }
}

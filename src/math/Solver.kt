package io.github.andreypfau.raptorq.math

import io.github.andreypfau.raptorq.rfc.Parameters
import io.github.andreypfau.raptorq.rfc.Rand

public object Solver {
    public operator fun invoke(
        parameters: Parameters,
        symbols: Array<ByteArray>,
    ) : MatrixGF256 {
        check(parameters.kPadded <= symbols.size)
        val encodingRows = Array(symbols.size) {
            parameters.getEncodingRow(it)
        }

        // Generate matrix upper_A: sparse part of A, first S + K_padded rows.
        var upperA = parameters.upperA(encodingRows)
        var d = createD(parameters, symbols)

        // Run indactivation decoding.
        // Lets call resulting lower triangualr matrix U
        val decodingResult = InactivationDecoding(upperA, parameters.p).invoke()
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
        upperA = upperA.applyRowPermutation(rowPermutation)

        val e = upperA.blockDense(0, uSize, uSize, parameters.l - uSize)

        val c = MatrixGF256(upperA.cols, d.cols)
        c.setFrom(d.blockView(0, 0, uSize, d.cols), 0, 0)

        // Make U Identity matrix and calculate E and D_upper.
        for(i in 0 until uSize) {
            for (row in upperA.col(i)) {
                if (row == i) {
                    continue
                }
                if (row >= uSize) {
                    break
                }
                e[row] + e[i]
                c[row] + c[i]
            }
        }

        // Calculate small_A_upper
        // small_A_upper = G_right
        val gLeft = upperA.blockSparse(uSize, 0, upperA.rows - uSize, uSize)
        var smallUpperA = MatrixGF256(upperA.rows - uSize, upperA.cols - uSize)
        upperA.blockForEach(uSize, uSize, upperA.rows - uSize, upperA.cols - uSize) { row, col ->
            smallUpperA[row, col] = Octet(1)
        }

        smallUpperA += (gLeft * e).toGf256()

        // Calculate small_A_lower
        var smallLowerA = MatrixGF256(parameters.h, upperA.cols - uSize)
        for (i in 1..parameters.h) {
            smallLowerA[smallLowerA.rows - i, smallLowerA.cols - i] = Octet(1)
        }

        // Calculate HDPC_right and set it into small_A_lower
        var t = MatrixGF256(parameters.kPadded + parameters.s, parameters.kPadded + parameters.s - uSize)
        for (i in 0 until t.cols) {
            t[colPermutation[i + t.rows - t.cols], i] = Octet(1)
        }
        val hdcpRight = hdpcMultiply(parameters.h, t)
        smallLowerA.setFrom(hdcpRight, 0, 0)

        // small_A_lower += HDPC_left * E
        t = e.toGf256()
        smallLowerA += hdpcLeftMultiply(t, parameters, colPermutation)

        val dUpper = MatrixGF256(uSize, d.cols)
        dUpper.setFrom(d.blockView(0,0, dUpper.rows, dUpper.cols), 0, 0)

        // small_D_upper
        var smallUpperD = MatrixGF256(upperA.rows - uSize, d.cols)
        smallUpperD.setFrom(d.blockView(uSize, 0, smallUpperD.rows, smallUpperD.cols), 0, 0)
        smallUpperD += (gLeft * dUpper)

        // small_D_lower
        val smallLowerD = MatrixGF256(parameters.h, d.cols)
//        smallLowerD.setFrom(d.blockView(u), 0, 0)
    }

    private fun createD(parameters: Parameters, symbols: Array<ByteArray>): MatrixGF256 {
        val symbolSize = symbols[0].size
        val d = MatrixGF256(parameters.s + parameters.h, symbolSize)
        var offset = parameters.s
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
        val t = MatrixGF256(parameters.kPadded + parameters.s, m.cols)
        for (i in 0 until m.rows) {
            m[i].copyInto(t[colPermutation[i]])
        }
        return hdpcMultiply(parameters.h, t)
    }

    private fun hdpcMultiply(rows: Int, v: MatrixGF256): MatrixGF256 {
        val alpha = Octet(1).exp()
        for (i in 1 until v.rows) {
            v[i].plusTimes(v[i - 1], alpha)
        }

        val u = MatrixGF256(rows, v.cols)
        for (i in 0 until rows) {
            v[i].plusTimes(v[v.rows - 1], Octet(i % 255).exp())
        }

        for (col in 0 until v.rows - 1) {
            val a = Rand.rand(col + 1, 6, rows)
            val b = (a + Rand.rand(col + 1, 7, rows - 1) + 1) % rows
            u[a].plus(v[col])
            u[b].plus(v[col])
        }
        return u
    }
}

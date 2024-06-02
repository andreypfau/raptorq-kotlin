package io.github.andreypfau.raptorq

import io.github.andreypfau.raptorq.math.MatrixGF256
import io.github.andreypfau.raptorq.rfc.Parameters

public class RawEncoder(
    private val parameters: Parameters,
    private val matrix: MatrixGF256
) {
    public val symbolSize: Int get() = matrix.cols
    private val d = MatrixGF256(1, symbolSize)

    public fun encodeIntoByteArray(
        symbolId: Int,
        destination: ByteArray,
        destinationOffset: Int = 0,
    ) {
        d.fillZero()
        parameters.encodingRowForEach(parameters.getEncodingRow(symbolId)) { row ->
            d[0] + matrix[row]
        }
        d[0].data.copyInto(destination, destinationOffset, 0, symbolSize)
    }

    public fun encodeToByteArray(symbolId: Int): ByteArray {
        val result = ByteArray(symbolSize)
        encodeIntoByteArray(symbolId, result)
        return result
    }
}

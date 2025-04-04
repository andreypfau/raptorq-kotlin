package io.github.andreypfau.raptorq

import io.github.andreypfau.raptorq.math.Solver
import io.github.andreypfau.raptorq.rfc.Parameters
import kotlin.math.min

public class Encoder(
    public val parameters: Parameters,
    public val symbolSize: Int,
    public val dataSize: Int,
    private val symbols: Array<ByteArray>,
) {
    public val symbolCount: Int get() = parameters.k
    private val rawEncoder: RawEncoder by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        val matrix = Solver(parameters, symbols)
        RawEncoder(parameters, matrix)
    }

    public fun encodeIntoByteArray(
        symbolId: Int,
        destination: ByteArray,
        destinationOffset: Int = 0,
    ) {
        if (symbolId < parameters.k) {
            symbols[symbolId].copyInto(destination, destinationOffset)
        } else {
            rawEncoder.encodeIntoByteArray(symbolId + parameters.kPadded - parameters.k, destination, destinationOffset)
        }
    }

    public fun encodeToByteArray(symbolId: Int): ByteArray {
        val result = ByteArray(symbolSize)
        encodeIntoByteArray(symbolId, result)
        return result
    }

    public fun prepareMoreSymbols() {
        rawEncoder
    }

    public companion object {
        public fun create(
            symbolSize: Int,
            data: ByteArray
        ): Encoder {
            val parameters = Parameters.fromK((data.size + symbolSize - 1) / symbolSize)
            val firstSymbols = Array(parameters.kPadded) { id ->
                val symbol = ByteArray(symbolSize)
                val offset = id * symbolSize
                val length = min(symbolSize, data.size - offset)
                if (length > 0) {
                    data.copyInto(symbol, 0, offset, offset + length)
                }
                symbol
            }
            return Encoder(parameters, symbolSize, data.size, firstSymbols)
        }
    }
}

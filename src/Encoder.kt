package io.github.andreypfau.raptorq

import io.github.andreypfau.raptorq.math.MatrixGF256
import io.github.andreypfau.raptorq.math.Solver
import kotlin.math.min

public class Encoder(
    public val parameters: Parameters,
    public val symbolSize: Int,
    data: ByteArray,
    private var solvedC: MatrixGF256? = null,
) {
    public constructor(symbolSize: Int, data: ByteArray) : this(
        Parameters.fromK((data.size + symbolSize - 1) / symbolSize),
        symbolSize,
        data
    )

    public val dataSize: Int = data.size

    private val d = MatrixGF256(1, symbolSize)

    private val sourceSymbols: Array<ByteArray> = Array(parameters.extendedSourceSymbols) { id ->
        val symbol = ByteArray(symbolSize)
        val offset = id * symbolSize
        val length = min(symbolSize, data.size - offset)
        if (length > 0) {
            data.copyInto(symbol, 0, offset, offset + length)
        }
        symbol
    }

    public fun encodeIntoByteArray(
        symbolId: Int,
        destination: ByteArray,
        destinationOffset: Int = 0,
    ) {
        if (symbolId < parameters.sourceSymbols) {
            sourceSymbols[symbolId].copyInto(destination, destinationOffset)
        } else {
            val internalSymbolId = symbolId + parameters.paddingSymbols
            val tuple = parameters.encodingTuple(internalSymbolId)
            val c = solve()
            val d = d
            d[0].fillZero()
            parameters.encode(tuple) { row ->
                d.addAssignRow(0, row, c)
            }
            d[0].data.copyInto(destination, destinationOffset, 0, symbolSize)
        }
    }

    public fun encodeToByteArray(symbolId: Int): ByteArray {
        val result = ByteArray(symbolSize)
        encodeIntoByteArray(symbolId, result)
        return result
    }

    public fun solve(): MatrixGF256 {
        var c = solvedC
        if (c != null) {
            return c
        }
        val symbolIds = IntArray(sourceSymbols.size) { it }
        c = Solver(parameters, sourceSymbols, symbolIds) ?: throw IllegalStateException("Failed to solve pi")
        solvedC = c
        return c
    }
}

package io.github.andreypfau.raptorq

import io.github.andreypfau.raptorq.math.DenseMatrixGF2
import io.github.andreypfau.raptorq.math.MatrixGF256
import io.github.andreypfau.raptorq.math.Solver
import kotlin.math.min

//private typealias SymbolMask = Decoder.MatrixBitSet
private typealias SymbolMask = BooleanArray

public class Decoder(
    public val parameters: Parameters,
    public val dataSize: Int,
    public val symbolSize: Int,
) {
    public constructor(dataSize: Int, symbolSize: Int) : this(
        Parameters.fromSize(dataSize, symbolSize),
        dataSize,
        symbolSize,
    )

    private val sourceData = ByteArray(parameters.sourceSymbols * symbolSize)
    private val sourceSymbolMask = SymbolMask(parameters.sourceSymbols)
    private val repairSymbols = ArrayList<ByteArray>()
    private val repairSymbolsIds = LinkedHashSet<Int>()

    public var receivedSourceSymbols: Int = 0
        private set

    public val receivedRepairSymbols: Int
        get() = repairSymbols.size

    private var solvedC: MatrixGF256? = null
    private val d = MatrixGF256(1, symbolSize)

    public fun addSymbol(
        symbolId: Int,
        source: ByteArray,
        startIndex: Int = 0,
        endIndex: Int = source.size,
    ): Boolean {
        val size = endIndex - startIndex
        require(size == symbolSize) {
            "Invalid symbol size: expected $symbolSize, got $size"
        }
        require(symbolId >= 0) {
            "Invalid symbol ID: $symbolId"
        }
        if (symbolId < parameters.sourceSymbols) {
            if (!sourceSymbolMask[symbolId]) {
                source.copyInto(sourceData, symbolId * symbolSize, startIndex, endIndex)
                sourceSymbolMask[symbolId] = true
                receivedSourceSymbols++
            }
        } else {
            // We need to convert from ESI to ISI
            val isi = symbolId + parameters.paddingSymbols
            if (repairSymbolsIds.add(isi)) {
                val symbol = ByteArray(symbolSize)
                source.copyInto(symbol, startIndex, 0, size)
                repairSymbols.add(symbol)
            }
        }
        // We have all source symbols and can return them without decoding
        if (receivedSourceSymbols == parameters.sourceSymbols) {
            return true
        }
        // We have already solved the system of equations
        if (solvedC != null) {
            return true
        }
        // the number of received packets is insufficient for decoding
        if ((receivedSourceSymbols + receivedRepairSymbols) < parameters.sourceSymbols) {
            return false
        }

        return true
    }

    public fun decodeFullyToByteArray(): ByteArray? {
        if (receivedSourceSymbols != parameters.sourceSymbols) {
            solve() ?: return null
        }
        val result = ByteArray(dataSize)
        decodeIntoByteArray(result)
        return result
    }

    public fun decodeFullyIntoByteArray(
        destination: ByteArray,
        destinationOffset: Int = 0,
    ): Boolean {
        if (receivedSourceSymbols != parameters.sourceSymbols) {
            solve() ?: return false
        }
        decodeIntoByteArray(destination, destinationOffset)
        return true
    }

    public fun decodeIntoByteArray(
        destination: ByteArray,
        destinationOffset: Int = 0,
        startIndex: Int = 0,
        endIndex: Int = dataSize,
    ): Int {
        val startSymbolId = startIndex / symbolSize
        val endSymbolId = endIndex / symbolSize
        val totalLength = endIndex - startIndex
        var remainingBytes = totalLength
        for (symbolId in startSymbolId until endSymbolId) {
            val offset = symbolId * symbolSize
            if (!sourceSymbolMask[symbolId]) {
                val solvedC = solve() ?: break
                recoverSymbolId(symbolId, solvedC)
            }
            val length = min(remainingBytes, symbolSize)
            sourceData.copyInto(
                destination,
                destinationOffset + totalLength - remainingBytes,
                offset,
                min(offset + length, destination.size)
            )
            remainingBytes -= length
        }
        return totalLength - remainingBytes
    }

    public fun solve(): MatrixGF256? {
        var solvedC = solvedC
        if (solvedC != null) {
            return solvedC
        }
        val encodedSymbolCount =
            receivedRepairSymbols + receivedSourceSymbols + parameters.extendedSourceSymbols - parameters.sourceSymbols
        val encodedIds = IntArray(encodedSymbolCount)
        val encodedSymbols = Array(encodedSymbolCount) { ByteArray(symbolSize) }

        var encodedIndex = 0
        sourceSymbolMask.forEachIndexed { index, isSet ->
            if (isSet) {
                encodedIds[encodedIndex] = index
                sourceData.copyInto(encodedSymbols[encodedIndex], 0, index * symbolSize, (index + 1) * symbolSize)
                encodedIndex++
            }
        }
        for (id in parameters.sourceSymbols until parameters.extendedSourceSymbols) {
            encodedIds[encodedIndex++] = id
        }
        repairSymbolsIds.forEachIndexed { index, id ->
            encodedIds[encodedIndex] = id
            repairSymbols[index].copyInto(encodedSymbols[encodedIndex], 0, 0, symbolSize)
            encodedIndex++
        }

        solvedC = Solver(parameters, encodedSymbols, encodedIds) ?: return null
        this.solvedC = solvedC
        return solvedC
    }

    public fun solvedEncoderOrNull(): Encoder? {
        val solved = solvedC ?: return null
        if (receivedSourceSymbols != parameters.sourceSymbols) {
            repeat(parameters.sourceSymbols) { symbolId ->
                recoverSymbolId(symbolId, solved)
            }
        }

        return Encoder(parameters, symbolSize, sourceData, solved)
    }

    private fun recoverSymbolId(symbolId: Int, solved: MatrixGF256) {
        if (sourceSymbolMask[symbolId]) {
            return
        }
        val offset = symbolId * symbolSize
        d.fillZero()
        parameters.encode(parameters.encodingTuple(symbolId)) { row ->
            d.addAssignRow(0, row, solved)
        }
        d[0].data.copyInto(sourceData, offset, 0, symbolSize)
        sourceSymbolMask[symbolId] = true
        receivedSourceSymbols++
    }

    internal class MatrixBitSet(size: Int) {
        private val matrix = DenseMatrixGF2(1, size)
        operator fun get(index: Int): Boolean = matrix[0, index]
        operator fun set(index: Int, value: Boolean) {
            matrix[0, index] = value
        }
    }
}

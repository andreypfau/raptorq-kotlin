package io.github.andreypfau.raptorq.generators

import io.github.andreypfau.raptorq.EncodingTuple
import io.github.andreypfau.raptorq.Parameters

public class EncodeGenerator(
    private val parameters: Parameters,
    private val encodingTuples: Array<EncodingTuple>
) : Generator {
    override val rows: Int
        get() = encodingTuples.size
    override val cols: Int
        get() = parameters.intermediateSymbols
    override val nonZeroes: Int
        get() = encodingTuples.sumOf { it.size }

    override fun generate(block: GeneratorFunction) {
        for ((row, encodingRow) in encodingTuples.withIndex()) {
            parameters.encode(encodingRow) { col ->
                block(row, col)
            }
        }
    }
}

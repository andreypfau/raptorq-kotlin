package io.github.andreypfau.raptorq.rfc

import io.github.andreypfau.raptorq.generators.Generator
import io.github.andreypfau.raptorq.generators.GeneratorBlock

public class ENC(
    private val parameters: Parameters,
    private val encodingRows: Array<EncodingRow>
) : Generator {
    override val rows: Int
        get() = encodingRows.size
    override val cols: Int
        get() = parameters.intermediateSymbols
    override val nonZeroes: Int
        get() = encodingRows.sumOf { it.size }

    override fun generate(block: GeneratorBlock) {
        for ((row, encodingRow) in encodingRows.withIndex()) {
            parameters.encodingRowForEach(encodingRow) { col ->
                block(row, col)
            }
        }
    }
}

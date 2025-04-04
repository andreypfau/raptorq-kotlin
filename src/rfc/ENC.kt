package io.github.andreypfau.raptorq.rfc

import io.github.andreypfau.raptorq.generators.Generator

public class ENC(
    private val parameters: Parameters,
    private val encodingRows: Array<EncodingRow>
) : Generator {
    override val rows: Int
        get() = encodingRows.size
    override val cols: Int
        get() = parameters.l
    override val nonZeroes: Int
        get() {
            var result = 0
            for (row in encodingRows) {
                result += row.size
            }
            return result
        }

    override fun generate(block: (Int, Int) -> Unit) {
        for ((row, encodingRow) in encodingRows.withIndex()) {
            parameters.encodingRowForEach(encodingRow) { col ->
                block(row, col)
            }
        }
    }
}

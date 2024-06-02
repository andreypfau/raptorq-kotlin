package io.github.andreypfau.raptorq.generators

public class BlockGenerator(
    public override val rows: Int,
    public override val cols: Int,
    private val generators: List<Generator>
) : Generator {
    public constructor(rows: Int, cols: Int, vararg generators: Generator) : this(rows, cols, generators.toList())

    override val nonZeroes: Int get() {
        var result = 0
        for (generator in generators) {
            result += generator.nonZeroes
        }
        return result
    }

    override fun generate(block: (Int, Int) -> Unit) {
        var rowOffset = 0
        var nextRowOffset = 0
        var colOffset = 0
        generators.forEach { generator ->
            if (colOffset == 0) {
                nextRowOffset = rowOffset + generator.rows
            }
            generator.generate { row, col ->
                block(row + rowOffset, col + colOffset)
            }
            colOffset += generator.cols
            if (colOffset >= cols) {
                colOffset = 0
                rowOffset = nextRowOffset
            }
        }
    }
}

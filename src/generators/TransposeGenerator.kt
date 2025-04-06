package io.github.andreypfau.raptorq.generators

public class TransposeGenerator(
    public val generator: Generator
) : Generator {
    override val cols: Int get() = generator.rows

    override val rows: Int get() = generator.cols

    override val nonZeroes: Int get() = generator.nonZeroes

    override fun generate(block: GeneratorBlock) {
        generator.generate { row, col ->
            block(col, row)
        }
    }
}

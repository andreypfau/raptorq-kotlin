package io.github.andreypfau.raptorq.generators

public class TransposeGenerator(
    public val generator: Generator
) : Generator by generator {
    override fun generate(block: (Int, Int) -> Unit) {
        generator.generate { row, col ->
            block(col, row)
        }
    }
}

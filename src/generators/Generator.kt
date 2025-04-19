package io.github.andreypfau.raptorq.generators

public interface Generator {
    public val rows: Int
    public val cols: Int
    public val nonZeroes: Int

    public fun generate(block: GeneratorFunction)
}

public fun interface GeneratorFunction {
    public operator fun invoke(row: Int, col: Int)
}

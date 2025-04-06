package io.github.andreypfau.raptorq.generators

public interface Generator {
    public val rows: Int
    public val cols: Int
    public val nonZeroes: Int

    public fun generate(block: GeneratorBlock)
}

public fun interface GeneratorBlock {
    public operator fun invoke(row: Int, col: Int)
}

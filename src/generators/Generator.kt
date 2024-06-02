package io.github.andreypfau.raptorq.generators

public interface Generator {
    public val rows: Int
    public val cols: Int
    public val nonZeroes: Int

    public fun generate(block: (row: Int, col: Int) -> Unit)
}

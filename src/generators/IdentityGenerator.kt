package io.github.andreypfau.raptorq.generators

public class IdentityGenerator(
    override val nonZeroes: Int
) : Generator {
    override val rows: Int get() = nonZeroes
    override val cols: Int get() = nonZeroes

    override fun generate(block: (Int, Int) -> Unit) {
        for (i in 0 until nonZeroes) {
            block(i, i)
        }
    }
}

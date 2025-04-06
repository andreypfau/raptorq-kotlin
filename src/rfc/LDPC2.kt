package io.github.andreypfau.raptorq.rfc

import io.github.andreypfau.raptorq.generators.Generator
import io.github.andreypfau.raptorq.generators.GeneratorBlock

public class LDPC2(
    public override val rows: Int,
    public override val cols: Int
) : Generator {
    public override val nonZeroes: Int get() = rows * 2

    public override fun generate(block: GeneratorBlock) {
        for (row in 0 until rows) {
            block(row, row % cols)
            block(row, (row + 1) % cols)
        }
    }
}

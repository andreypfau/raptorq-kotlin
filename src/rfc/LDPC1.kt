package io.github.andreypfau.raptorq.rfc

import io.github.andreypfau.raptorq.generators.Generator

public class LDPC1(
    public override val rows: Int,
    public override val cols: Int
) : Generator {
    public override val nonZeroes: Int get() = cols * 3

    public override fun generate(block: (Int, Int) -> Unit) {
        for (col in 0 until cols) {
            val i = col / rows
            val shift = col % rows
            var a = shift
            var b = (i + 1 + shift) % rows
            var c = (2 * (i + 1) + shift) % rows

            var temp: Int
            if (a > c) {
                temp = a
                a = c
                c = temp
            }
            if (b > c) {
                temp = b
                b = c
                c = temp
            }
            if (a > b) {
                temp = a
                a = b
                b = temp
            }

            block(a, col)
            block(b, col)
            block(c, col)
        }
    }
}

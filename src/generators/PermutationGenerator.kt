package io.github.andreypfau.raptorq.generators

public class PermutationGenerator(
    private val generator: Generator,
    p: IntArray
) : Generator by generator {
    private val p = p.inversePermutation()

    override fun generate(block: (Int, Int) -> Unit) {
        generator.generate { row, col ->
            block(row, p[col])
        }
    }
}

internal fun IntArray.inversePermutation(): IntArray {
    val result = IntArray(size)
    for (i in indices) {
        result[this[i]] = i
    }
    return result
}

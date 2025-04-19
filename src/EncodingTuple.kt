package io.github.andreypfau.raptorq

public data class EncodingTuple(
    /**
     * `d` is a positive integer denoting an encoding symbol LT degree
     */
    public val d: UInt,

    /**
     * `a` is a positive integer between 1 and W-1 inclusive
     */
    public val a: UInt,

    /**
     * `b` is a non-negative integer between 0 and W-1 inclusive
     */
    public val b: UInt,

    /**
     * `d1` is a positive integer that has value either 2 or 3 denoting an encoding symbol PI degree
     */
    public val d1: UInt,

    /**
     * `a1` is a positive integer between 1 and P1-1 inclusive
     */
    public val a1: UInt,

    /**
     * b1 is a non-negative integer between 0 and P1-1 inclusive
     */
    public val b1: UInt
) {
    val size: Int get() = (d + d1).toInt()

    public companion object {
        // Tuple[K', X] as defined in section 5.3.5.4
        public fun fromParameters(parameters: Parameters, internalSymbolId: Int): EncodingTuple {
            return create(
                internalSymbolId,
                parameters.ltSymbols,
                parameters.systematicIndex,
                parameters.p1
            )
        }

        public fun create(
            internalSymbolId: Int,
            ltSymbols: Int,
            systematicIndex: Int,
            p1: Int,
        ): EncodingTuple {
            val j = systematicIndex.toUInt()
            val w = ltSymbols

            var aLocal = 53591u + j * 997u
            if (aLocal % 2u == 0u) {
                aLocal++
            }
            val bLocal = 10267u * (j + 1u)
            val y = bLocal + internalSymbolId.toUInt() * aLocal
            val v = Rand.rand(y.toInt(), 0, 1 shl 20)
            val d = Deg.deg(v, w).toUInt()
            val a = 1u + Rand.rand(y.toInt(), 1, (w - 1)).toUInt()
            val b = Rand.rand(y.toInt(), 2, w).toUInt()
            val d1 = if (d < 4u) {
                2u + Rand.rand(internalSymbolId, 3, 2).toUInt()
            } else {
                2u
            }
            val a1 = 1u + Rand.rand(internalSymbolId, 4, p1 - 1).toUInt()
            val b1 = Rand.rand(internalSymbolId, 5, p1).toUInt()
            return EncodingTuple(d, a, b, d1, a1, b1)
        }
    }
}

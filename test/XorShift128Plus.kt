package io.github.andreypfau.raptorq

class XorShift128Plus constructor(seed: Long) {
    private var s0: Long
    private var s1: Long

    init {
        var state = seed
        fun next(): Long {
            state += 0x9E3779B97F4A7C15uL.toLong()
            var z = state
            z = (z xor (z ushr 30)) * 0xBF58476D1CE4E5B9uL.toLong()
            z = (z xor (z ushr 27)) * 0x94D049BB133111EBuL.toLong()
            return z xor (z ushr 31)
        }
        s0 = next()
        s1 = next()
    }

    fun nextLong(): Long {
        var x = s0
        val y = s1
        s0 = y
        x = x xor (x shl 23)
        s1 = (x xor y xor (x ushr 17) xor (y ushr 26))
        return s1 + y
    }

    fun nextLong(min: Long = 0, max: Long = Long.MAX_VALUE): Long {
        var value = nextLong().toULong()
        val result = (value % (max.toULong() - min.toUInt() + 1u) + min.toULong())
        return result.toLong()
    }

    fun nextInt(min: Int = 0, max: Int = Int.MAX_VALUE): Int =
        nextLong(min.toLong(), max.toLong()).toInt()

    fun nextString(from: Char, to: Char, len: Int): String = buildString(len) {
        repeat(len) {
            val code = nextInt(from.code, to.code)
            append(code.toChar())
        }
    }
}

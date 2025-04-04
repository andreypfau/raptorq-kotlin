package io.github.andreypfau.raptorq

import kotlin.test.Test

class RaptorQTest {
    @Test
    fun test() {
        val maxSymbolSize = 200
        val data = XorShift128Plus(321).nextString( 'a', 'z', maxSymbolSize * 50000)
        val encoder = Encoder.create(maxSymbolSize, data.toByteArray())
        encoder.prepareMoreSymbols()

        println(data)
    }
}

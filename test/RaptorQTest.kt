package io.github.andreypfau.raptorq

import io.github.andreypfau.raptorq.math.Solver
import kotlin.test.Test

class RaptorQTest {
    @Test
    fun test() {
        repeat(50) {
            runTest()
        }
        Solver.debug = true
        runTest()
    }

    fun runTest() {
        val maxSymbolSize = 200
        val data = XorShift128Plus(321).nextString( 'a', 'z', maxSymbolSize * 50000)
        val encoder = Encoder.create(maxSymbolSize, data.toByteArray())
        encoder.prepareMoreSymbols()
    }
}

package io.github.andreypfau.raptorq

import kotlin.test.Test
import kotlin.test.assertContentEquals

class RaptorQTest {
    @Test
    fun test() {
        runTest()
    }

    fun runTest() {
        val maxSymbolSize = 756
//        val symbolCount = Random.nextInt(5, 300)
        val symbolCount = 3000
//        val seed = Random.nextLong()
        val data = XorShift128Plus(333)
            .nextString('a', 'z', maxSymbolSize * symbolCount)
        fecTest(data.toByteArray(), maxSymbolSize)
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun fecTest(
        data: ByteArray,
        maxSymbolSize: Int
    ) {
        val encoder = Encoder(maxSymbolSize, data)

        val parameters = encoder.parameters
        val decoder = Decoder(parameters, encoder.symbolSize, encoder.dataSize)

        val random = XorShift128Plus(111)
        var sentSymbols = 0
        for (i in 0 until data.size / maxSymbolSize * 20) {
            if (random.nextInt(0, 5) == 0) continue
            val symbol = encoder.encodeToByteArray(i)

            println("add symbol: $i ${symbol.toHexString()}")
            val canDecode = decoder.addSymbol(i, symbol)

            sentSymbols++
            if (canDecode) { // декодер может восстановить данные
                val result = decoder.decodeFullyToByteArray()
//                println("try decode result: ${result != null}")

                if (result != null) {
                    assertContentEquals(data, result)
                    println("$sentSymbols / ${parameters.sourceSymbols}")
                    return
                }
            }
        }
    }
}

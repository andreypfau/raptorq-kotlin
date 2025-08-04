# raptorq-kotlin

**raptorq-kotlin** is a pure Kotlin implementation of [RaptorQ FEC (RFC 6330)](https://tools.ietf.org/html/rfc6330),
designed for use in distributed systems, P2P protocols, and applications where reliable transmission with minimal
redundancy is critical.

Supports Kotlin Multiplatform (JVM, Native, JS).

---

## Features

- 💡 Pure Kotlin implementation with no native dependencies
- 🧹 Customizable symbol sizes
- 🚀 Optimized for performance and modularity
- 🧪 Includes full encode-decode tests with simulated packet loss
- 📦 Kotlin Multiplatform support out of the box

---

## Installation

Add to your `build.gradle.kts`:

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.andreypfau:raptorq-kotlin:0.1.0")
}
```

Replace `<version>` with the latest available version
on [Maven Central](https://search.maven.org/artifact/io.github.andreypfau/raptorq-kotlin).

---

## Example

```kotlin
import io.github.andreypfau.raptorq.*

fun main() {
    val data = ByteArray(10_000) { it.toByte() }
    val symbolSize = 768

    val encoder = Encoder(symbolSize, data)
    val decoder = Decoder(encoder.parameters, encoder.symbolSize, encoder.dataSize)

    for (i in 0 until 20_000) {
        if (i % 5 == 0) continue // simulate 20% loss
        val symbol = encoder.encodeToByteArray(i)
        if (decoder.addSymbol(i, symbol)) {
            val result = decoder.decodeFullyToByteArray()
            if (result != null) {
                println("Decoded successfully after \${i + 1} symbols")
                break
            }
        }
    }
}
```

## License

This project is licensed under the Apache 2.0 License. See the [LICENSE](LICENSE) file for details.

---

## TODO

-

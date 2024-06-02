package io.github.andreypfau.raptorq

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalEncodingApi::class)
public suspend fun main() {
    val httpClient = HttpClient(CIO)
    val result = httpClient.post("http://localhost:8080/api/v1/broadcast") {
        setBody(Base64.decode("te6cckECAwEAASYAAeGIAQo4oCnfhe0iPkJAAkOomdWg1rKvu5V/otYHWN0DpuaKAcZd4IE/p/GHsBC3+hg7zw7tPWppwBlh2YVPpplQW9AaPrZyylTkk9f7wPkZROzfzCpCCsFvI3tgf0KBEWbDsElNTRi7LjIlsAAABGAADAEBaGIAT+D3vIBIb5VwFB+r4Nt310+uQnZlF/3oK7Qpm8tZLOEgF9eEAAAAAAAAAAAAAAAAAAECAPJNaW5lAGXGR/2FHFAU78L2kR8hIAEh1Ezq0GtZV93Kv9FrA6xugdNzRZlQsXtLqIlFn6gxfg8FwOF5X0I1MqaozC9mWHgl+/9rms+y3GAWQLJ2VJGkC0UOLZlQsXtLqIlFn6gxfg8FwOF5X0I1MqaozC9mWHgl+/9rVmDTSg=="))
    }
    println("${result.status} ${result.bodyAsText()}")
}

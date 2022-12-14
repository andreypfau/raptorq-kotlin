@file:Suppress("OPT_IN_USAGE")

package com.github.andreypfau.raptorq.utils

internal inline fun UShortArray.swap(i: Int, j: Int) {
    val tmp = this[i]
    this[i] = this[j]
    this[j] = tmp
}

internal inline fun UIntArray.swap(i: Int, j: Int) {
    val tmp = this[i]
    this[i] = this[j]
    this[j] = tmp
}

internal inline fun IntArray.swap(i: Int, j: Int) {
    val tmp = this[i]
    this[i] = this[j]
    this[j] = tmp
}

internal inline fun <T> Iterator<T>.nextOrNull(): T? = if (hasNext()) next() else null

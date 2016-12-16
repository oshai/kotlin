package kotlin

import kotlin.annotation.AnnotationTarget.*

open header class Error : Throwable {
    constructor(message: String)
}

open header class Exception : Throwable

open header class IllegalArgumentException : RuntimeException {
    constructor()
    constructor(message: String)
}

open header class IllegalStateException : RuntimeException {
    constructor(message: String)
}

open header class IndexOutOfBoundsException : RuntimeException {
    constructor(message: String)
}

open header class NoSuchElementException : RuntimeException {
    constructor()
    constructor(message: String)
}

open header class RuntimeException : Exception {
    constructor()
    constructor(message: String)
}

open header class UnsupportedOperationException : RuntimeException {
    constructor(message: String)
}


header interface Comparator<T> {
    fun compare(a: T, b: T): Int
}

header inline fun <T> Comparator(crossinline comparison: (T, T) -> Int): Comparator<T>

// From kotlin.kt

internal header fun <T> arrayOfNulls(reference: Array<out T>, size: Int): Array<T>
internal inline header fun <K, V> Map<K, V>.toSingletonMapOrSelf(): Map<K, V>
internal inline header fun <K, V> Map<out K, V>.toSingletonMap(): Map<K, V>
internal inline header fun <T> Array<out T>.copyToArrayOfAny(isVarargs: Boolean): Array<out Any?>

internal header interface Serializable

// temporary
internal header object Math {
    fun max(a: Int, b: Int): Int
    fun min(a: Int, b: Int): Int
}


// From concurrent.kt

@Target(PROPERTY, FIELD)
header annotation class Volatile

inline header fun <R> synchronized(lock: Any, crossinline block: () -> R): R


public header fun <T> lazy(initializer: () -> T): Lazy<T>

/**
 * Creates a new instance of the [Lazy] that uses the specified initialization function [initializer].
 *
 * The [mode] parameter is ignored. */
public header fun <T> lazy(mode: LazyThreadSafetyMode, initializer: () -> T): Lazy<T>

/**
 * Creates a new instance of the [Lazy] that uses the specified initialization function [initializer].
 *
 * The [lock] parameter is ignored.
 */
public header fun <T> lazy(lock: Any?, initializer: () -> T): Lazy<T>

package minim.util

/**
 * Converts a [Boolean] to a **truthy** [Float] value.
 *
 * @return `1F` if this is `true`, or `0F` otherwise
 */
fun Boolean.toFloat() =
    if (this) 1F else 0F

/**
 * Converts a [Boolean] to a **truthy** [Int] value.
 *
 * @return `1` if `this` is `true`, or `0` otherwise
 */
fun Boolean.toInt() =
    if (this) 1 else 0

/**
 * Converts a [Float] to a [Boolean] based on it's **truthiness**.
 *
 * @return `true` if `this` is nonzero, or `false` otherwise
 */
fun Float.toBoolean() =
    this != 0F

/**
 * Converts an [Int] to a [Boolean] based on it's **truthiness**.
 *
 * @return `true` if `this` is nonzero, or `false` otherwise
 */
fun Int.toBoolean() =
    this != 0

/**
 * Gets the escape sequence associated with this character, if it exists.
 *
 * @return the associated escape sequence, or this character by itself if there is none
 */
fun Char.escaped() = when (this) {
    '\u0000' -> "\\0"
    
    '\u0007' -> "\\a"
    
    '\b'     -> "\\b"
    
    '\u000c' -> "\\f"
    
    '\n'     -> "\\n"
    
    '\r'     -> "\\r"
    
    '\t'     -> "\\t"
    
    '\u000B' -> "\\v"
    
    else     -> if (this.code in 0..31) {
        "\\x${this.code.toString(16).padStart(2, '0')}"
    }
    else {
        "$this"
    }
}
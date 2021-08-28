package minim.util

fun Boolean.toFloat() =
    if (this) 1F else 0F

fun Boolean.toInt() =
    if (this) 1 else 0

fun Float.toBoolean() =
    this != 0F

fun Int.toBoolean() =
    this != 0

fun Char.slashify() = when (this) {
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
package minim.util

fun Boolean.toFloat() =
    if (this) 1F else 0F

fun Boolean.toInt() =
    if (this) 1 else 0

fun Float.toBoolean() =
    this != 0F

fun Int.toBoolean() =
    this != 0
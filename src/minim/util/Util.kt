package minim.util

fun Boolean.toFloat() =
    if (this) 1F else 0F

fun Float.toBoolean() =
    this != 0F
package minim.runtime

import kotlin.math.max

class MArray(private val elements: List<MNumber>) : List<MNumber> by elements {
    constructor(size: Int) : this(List(size) { MNumber() })
    
    val ascii get() = String(map { it.value.toInt().toChar() }.toCharArray())
    
    operator fun get(indices: IntRange) =
        MArray(slice(indices))
    
    fun printDebug() {
        val widths = IntArray(size)
        
        for (i in indices) {
            val widthN = get(i).toString().length
            val widthC = get(i).toChar().toString().length
            
            widths[i] = max(widthN, widthC)
        }
        
        println(mapIndexed { i, e -> e.toString().padStart(widths[i]) }.joinToString())
    
        println(mapIndexed { i, e -> e.toChar().toString().padStart(widths[i]) }.joinToString())
    }
    
    override fun toString() =
        joinToString(prefix = "{ ", separator = ", ", postfix = " }")
}

fun String.toMArray()=
    MArray(map { MNumber(it.code.toFloat()) })
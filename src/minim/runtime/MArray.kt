package minim.runtime

import kotlin.math.max

class MArray(private val elements: List<Ref>) : List<Ref> by elements {
    constructor(size: Int) : this(List(size) { Ref(MNumber.Float()) })
    
    val ascii get() = String(map { it.value.toChar() }.toCharArray())
    
    operator fun get(indices: IntRange) =
        MArray(slice(indices))
    
    fun printDebug() {
        val widths = IntArray(size)
        
        for (i in indices) {
            val widthN = get(i).value.toString().length
            val widthC = get(i).value.toChar().toString().length
            
            widths[i] = max(widthN, widthC)
        }
        
        println(mapIndexed { i, e -> e.value.toString().padStart(widths[i]) }.joinToString())
    
        println(mapIndexed { i, e -> e.value.toChar().toString().padStart(widths[i]) }.joinToString())
    }
    
    override fun toString() =
        map { it.fromRef() }.joinToString(prefix = "{ ", separator = ", ", postfix = " }")
}

fun String.toMArray()=
    MArray(map { Ref(MNumber.Int(it.code)) })
package minim.runtime

import minim.util.slashify

class MArray(private val elements: List<Ref>) : List<Ref> by elements {
    constructor(size: Int) : this(List(size) { Ref(MNumber.Float()) })
    
    operator fun get(indices: IntRange) =
        MArray(slice(indices))
    
    fun scanString(start: Int) = buildString {
        var i = start
        
        while (i < size) {
            val element = elements[i++].value.toChar()
            
            if (element == '\u0000') break
            
            append(element)
        }
    }
    
    fun printDebug() {
        val widths = IntArray(size)
        
        for (i in indices) {
            val widthI = i.toString().length
            val widthN = get(i).value.toString().length
            val widthC = get(i).value.toChar().slashify().length
            
            widths[i] = maxOf(widthI, widthN, widthC)
        }
        
        println(mapIndexed { i, _ -> i.toString().padStart(widths[i]) }.joinToString(prefix = "Index | ",
            separator = " | "))
        
        println(mapIndexed { i, e -> e.value.toString().padStart(widths[i]) }.joinToString(prefix = "Value | ",
            separator = " | "))
        
        println(mapIndexed { i, e -> e.value.toChar().slashify().padStart(widths[i]) }.joinToString(prefix = " Char | ",
            separator = " | "))
    }
    
    override fun toString() =
        map { it.fromRef() }.joinToString(prefix = "{ ", separator = ", ", postfix = " }")
}

fun String.toMArray() =
    MArray(map { Ref(MNumber.Int(it.code)) })
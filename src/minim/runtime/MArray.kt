package minim.runtime

class MArray(private val elements: List<Ref>) : List<Ref> by elements {
    constructor(size: Int) : this(List(size) { Ref(MNumber.Float()) })
    
    val ascii get() = String(map { it.value.toChar() }.toCharArray())
    
    operator fun get(indices: IntRange) =
        MArray(slice(indices))
    
    fun printDebug() {
        val widths = IntArray(size)
        
        for (i in indices) {
            val widthI = i.toString().length
            val widthN = get(i).value.toString().length
            val widthC = get(i).value.toChar().slashify().length
            
            widths[i] = maxOf(widthI, widthN, widthC)
        }
        
        println(mapIndexed { i, _ -> i.toString().padStart(widths[i]) }.joinToString(prefix = "INDEX: ",
            separator = " | "))
        
        println(mapIndexed { i, e -> e.value.toString().padStart(widths[i]) }.joinToString(prefix = "VALUE: ",
            separator = " | "))
        
        println(mapIndexed { i, e -> e.value.toChar().slashify().padStart(widths[i]) }.joinToString(prefix = " CHAR: ",
            separator = " | "))
    }
    
    override fun toString() =
        map { it.fromRef() }.joinToString(prefix = "{ ", separator = ", ", postfix = " }")
}

fun Char.slashify() = when (this) {
    '\u0000' -> "\\0"
    '\u0007' -> "\\a"
    '\b'     -> "\\b"
    '\u000c' -> "\\f"
    '\n'     -> "\\n"
    '\r'     -> "\\r"
    '\t'     -> "\\t"
    '\u000B' -> "\\v"
    else     -> "$this"
}

fun String.toMArray() =
    MArray(map { Ref(MNumber.Int(it.code)) })
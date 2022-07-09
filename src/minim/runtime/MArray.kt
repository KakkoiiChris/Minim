package minim.runtime

import minim.util.escaped
import kotlin.math.min

/**
 * Represents an array of numbers.
 *
 * @param elements the elements of this array
 *
 * @constructor an array instance with specified elements
 */
class MArray(private val elements: List<Reference>) : List<Reference> by elements {
    /**
     * Creates an array of zeroes.
     *
     * @param size the size of the array
     *
     * @constructor an array instance of zeroes of specified size
     */
    constructor(size: Int) : this(List(size) { Reference(MNumber.Float()) })
    
    /**
     * Gets a string from this array starting from the specified index until the next zero.
     *
     * @param start the index to scan from
     *
     * @return a [String] generated from the scanned unicode values
     */
    fun scanString(start: Int) = buildString {
        var i = start
        
        while (i < size) {
            val element = elements[i++].value.toChar()
            
            if (element == '\u0000') break
            
            append(element)
        }
    }
    
    /**
     * Prints an interactive table for debugging purposes.
     *
     * The user is asked if they want to view the table. After entering 'y' or 'yes', the user is asked for the start index, end index, and the count per line. Table rows will be printed out for the memory in the given range.
     */
    fun printDebugTable() {
        print("Print Debug Table? ")
        
        if ((readLine() ?: "").matches("[Yy]([Ee][Ss])?".toRegex())) {
            print("\nStart index (defaults to 0): ")
            
            val start = readLine()?.takeIf { it.isNotEmpty() }?.toInt() ?: 0
            
            print("\nEnd index (defaults to $size): ")
            
            val end = readLine()?.takeIf { it.isNotEmpty() }?.toInt() ?: size
            
            print("\nIndices per row (defaults to 100): ")
            
            val step = readLine()?.takeIf { it.isNotEmpty() }?.toInt() ?: 100
            
            println()
            
            var from = start
            var to = min(start + step, end)
            
            while (from < end) {
                val slice = MArray(slice(from until to))
                
                val widths = IntArray(slice.size)
                
                for (i in slice.indices) {
                    val widthI = (i + from).toString().length
                    val widthN = slice[i].value.toString().length
                    val widthC = slice[i].value.toChar().escaped().length
                    
                    widths[i] = maxOf(widthI, widthN, widthC)
                }
                
                println("[$from -> $to]")
                
                println(List(slice.size) { i ->
                    (i + from).toString().padStart(widths[i])
                }.joinToString(prefix = "Index | ", separator = " | "))
                
                println(slice.mapIndexed { i, e ->
                    e.value.toString().padStart(widths[i])
                }.joinToString(prefix = "Value | ", separator = " | "))
                
                println(slice.mapIndexed { i, e ->
                    e.value.toChar().escaped().padStart(widths[i])
                }.joinToString(prefix = " Char | ", separator = " | "))
                
                println()
                
                from = min(from + step, end)
                to = min(to + step, end)
            }
        }
    }
    
    /**
     * Gets a formatted view of this array.
     *
     * @return Ex. '{ 1, 2, 4, 8, 16 }'
     */
    override fun toString() =
        map { it.fromReference() }.joinToString(prefix = "{ ", separator = ", ", postfix = " }")
}

/**
 * Converts this string to a null-terminated array of unicode values.
 *
 * @receiver the string to convert
 *
 * @return Ex. "Hello" -> '{ 72, 101, 108, 108, 111, 0 }'
 */
fun String.toMArray() =
    MArray(map { Reference(MNumber.Int(it.code)) } + Reference(MNumber.Int()))
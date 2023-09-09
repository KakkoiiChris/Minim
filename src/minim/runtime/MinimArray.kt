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
class MinimArray(private val elements: List<Reference>) : List<Reference> by elements {
    /**
     * Creates an array of zeroes.
     *
     * @param size the size of the array
     *
     * @constructor an array instance of zeroes of specified size
     */
    constructor(size: Int) : this(List(size) { Reference(MinimNumber.Float()) })

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
            val char = elements[i++].value.toChar()

            if (char == '\u0000') break

            append(char)
        }
    }

    /**
     * Prints an interactive table for debugging purposes.
     *
     * The user is asked if they want to view the table. After entering 'y' or 'yes', the user is asked for the start index, end index, and the count per line. Table rows will be printed out for the memory in the given range.
     */
    fun printDebugTable() {
        print("Print Debug Table? ")

        if (readln().matches("(?i)Y(es)?".toRegex())) {
            print("\nStart index (defaults to 0): ")

            val start = readln().takeIf { it.isNotEmpty() }?.toInt() ?: 0

            print("\nEnd index (defaults to $size): ")

            val end = readln().takeIf { it.isNotEmpty() }?.toInt() ?: size

            print("\nIndices per row (defaults to 100): ")

            val step = readln().takeIf { it.isNotEmpty() }?.toInt() ?: 100

            println()

            var from = start
            var to = min(start + step, end)

            while (from < end) {
                val slice = MinimArray(slice(from until to))

                val widths = IntArray(slice.size) { i ->
                    val widthI = (i + from).toString().length
                    val widthN = slice[i].value.toString().length
                    val widthC = slice[i].value.toChar().escaped().length

                    maxOf(widthI, widthN, widthC)
                }

                val indices = slice
                    .indices
                    .joinToString(prefix = "Index | ", separator = " | ") { i ->
                        (i + from).toString().padStart(widths[i])
                    }

                val values = slice
                    .mapIndexed { i, e ->
                        e.value.toString().padStart(widths[i])
                    }
                    .joinToString(prefix = "Value | ", separator = " | ")

                val chars = slice
                    .mapIndexed { i, e ->
                        e.value.toChar().escaped().padStart(widths[i])
                    }
                    .joinToString(prefix = " Char | ", separator = " | ")

                println("""
                    [$from -> $to]
                    $indices
                    $values
                    $chars
                    """.trimIndent())

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
    MinimArray(map { Reference(MinimNumber.Int(it.code)) } + Reference(MinimNumber.Int()))
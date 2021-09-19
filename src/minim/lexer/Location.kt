package minim.lexer

/**
 * Representation of a location within a file, for use in error messages.
 *
 * @param name the name of the file
 * @param row the 1-indexed vertical position
 * @param col the 1-indexed horizontal position
 */
data class Location(val name: String, val row: Int, val col: Int) {
    companion object {
        /**
         * An *empty* [Location] instance for omitted expressions that intrinsically would not have a location.
         */
        val none = Location("", -1, -1)
    }
    
    /**
     * Formats the location data for use in error messages.
     *
     * @return Ex. '@ hello.min (2, 7)'
     */
    override fun toString() =
        "@ $name.min ($row, $col)"
}
package minim.lexer

data class Location(val name: String, val row: Int, val col: Int) {
    companion object {
        val none = Location("", -1, -1)
    }
    
    override fun toString() =
        "$name.min ($row, $col)"
}
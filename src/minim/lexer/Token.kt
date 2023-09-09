package minim.lexer

/**
 * A representation of a single valid sequence of characters.
 *
 * @param loc the [Location] this token occurred at
 * @param type the type of this token
 * @param value the value associated with this token, if any (defaults to `0F`)
 */
data class Token(val loc: Location, val type: Type) {
    /**
     * Gets the string representation of this token.
     *
     * @return Ex. 'Token { @ 0.0 } @ tictactoe.min (12, 6)', 'Token { V 3.1415 }'
     */
    override fun toString() =
        if (loc == Location.none)
            "Token { ${type.rep} }"
        else
            "Token { ${type.rep} } $loc"
}
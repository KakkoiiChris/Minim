package minim.lexer

sealed interface Type {
    val rep: String
}

class Value(val value: Float) : Type {
    override val rep
        get() = value.toString()
}

class Dynamic(val char: Char) : Type {
    override val rep
        get() = char.toString()
}

/**
 * An enumeration of all valid unique character sequences.
 *
 * @param rep the string representation of this type
 */
enum class Symbol(override val rep: String) : Type {
    DOUBLE_PLUS("++"),
    DOUBLE_DASH("--"),
    DOUBLE_QUESTION("??"),
    DOUBLE_BANG("!!"),
    DOUBLE_TILDE("~~"),
    BANG("!"),
    TILDE("~"),
    SMALL_I("i"),
    SMALL_F("f"),
    SMALL_S("s"),
    STAR("*"),
    SLASH("/"),
    PERCENT("%"),
    PLUS("+"),
    DASH("-"),
    DOUBLE_LESS("<<"),
    DOUBLE_GREATER(">>"),
    TRIPLE_GREATER(">>>"),
    LESS("<"),
    LESS_EQUAL("<="),
    GREATER(">"),
    GREATER_EQUAL(">="),
    DOUBLE_EQUAL("=="),
    LESS_GREATER("<>"),
    AMPERSAND("&"),
    CARET("^"),
    PIPE("|"),
    DOUBLE_AMPERSAND("&&"),
    DOUBLE_PIPE("||"),
    QUESTION("?"),
    EQUAL("="),
    STAR_EQUAL("*="),
    SLASH_EQUAL("/="),
    PERCENT_EQUAL("%="),
    PLUS_EQUAL("+="),
    DASH_EQUAL("-="),
    DOUBLE_LESS_EQUAL("<<="),
    DOUBLE_GREATER_EQUAL(">>="),
    TRIPLE_GREATER_EQUAL(">>>="),
    AMPERSAND_EQUAL("&="),
    CARET_EQUAL("^="),
    PIPE_EQUAL("|="),
    DOUBLE_AMPERSAND_EQUAL("&&="),
    DOUBLE_PIPE_EQUAL("||="),
    POUND("#"),
    DOLLAR("$"),
    UNDERSCORE("_"),
    BACKSLASH("\\"),
    BIG_M("M"),
    LEFT_PAREN("("),
    RIGHT_PAREN(")"),
    LEFT_SQUARE("["),
    RIGHT_SQUARE("]"),
    LEFT_BRACE("{"),
    RIGHT_BRACE("}"),
    COLON(":"),
    AT("@"),
    COMMA(","),
    DOT("."),
    END_OF_FILE("0");

    /**
     * Gets the string representation of this type.
     *
     * @return Ex. '(', 'M', '<<'
     */
    override fun toString() = rep
}
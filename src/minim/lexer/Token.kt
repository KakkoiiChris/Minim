package minim.lexer

/**
 * A representation of a single valid sequence of characters.
 *
 * @param loc the [Location] this token occurred at
 * @param type the type of this token
 * @param value the value associated with this token, if any (defaults to `0F`)
 */
data class Token(val loc: Location, val type: Type, val value: Float = 0F) {
    /**
     * An enumeration of all valid unique character sequences.
     *
     * @param rep the string representation of this type
     */
    enum class Type(private val rep: String) {
        Value("V"),
        DoublePlus("++"),
        DoubleMinus("--"),
        DoubleQuestion("??"),
        DoubleExclamation("!!"),
        DoubleTilde("~~"),
        Exclamation("!"),
        Tilde("~"),
        SmallI("i"),
        SmallF("f"),
        SmallS("s"),
        Star("*"),
        Slash("/"),
        Percent("%"),
        Plus("+"),
        Minus("-"),
        DoubleLess("<<"),
        DoubleGreater(">>"),
        TripleGreater(">>>"),
        LessSign("<"),
        LessEqualSign("<="),
        GreaterSign(">"),
        GreaterEqualSign(">="),
        DoubleEqual("=="),
        LessGreater("<>"),
        Ampersand("&"),
        Caret("^"),
        Pipe("|"),
        DoubleAmpersand("&&"),
        DoublePipe("||"),
        Question("?"),
        EqualSign("="),
        StarEqual("*="),
        SlashEqual("/="),
        PercentEqual("%="),
        PlusEqual("+="),
        MinusEqual("-="),
        DoubleLessEqual("<<="),
        DoubleGreaterEqual(">>="),
        TripleGreaterEqual(">>>="),
        AndEqual("&="),
        CaretEqual("^="),
        PipeEqual("|="),
        DoubleAmpersandEqual("&&="),
        DoublePipeEqual("||="),
        Number("#"),
        Dollar("$"),
        Underscore("_"),
        Backslash("\\"),
        BigM("M"),
        Dynamic("D"),
        LeftParen("("),
        RightParen(")"),
        LeftSquare("["),
        RightSquare("]"),
        LeftBrace("{"),
        RightBrace("}"),
        Colon(":"),
        At("@"),
        Comma(","),
        Dot("."),
        EndOfFile("0");
        
        /**
         * Gets the string representation of this type.
         *
         * @return Ex. '(', 'M', '<<'
         */
        override fun toString() = rep
    }
    
    /**
     * Gets the string representation of this token.
     *
     * @return Ex. 'Token { @ 0.0 } @ tictactoe.min (12, 6)', 'Token { V 3.1415 }'
     */
    override fun toString() =
        if (loc == Location.none)
            "Token { $type $value }"
        else
            "Token { $type $value } $loc"
}
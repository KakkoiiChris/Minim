package minim.lexer

data class Token(val loc: Location, val type: Type, val value: Float = 0F) {
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
    
        DoubleAnd("&&"),
        
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
        DoubleAndEqual("&&="),
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
        
        override fun toString() = rep
    }
}
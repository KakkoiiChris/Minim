package minim.lexer

data class Token(val loc: Location, val type: Type, val value: Float = 0F) {
    enum class Type(private val rep: String) {
        VAL("V"),
        ADD("+"),
        SUB("-"),
        MUL("*"),
        DIV("/"),
        REM("%"),
        LPR("("),
        RPR(")"),
        LSQ("["),
        RSQ("]"),
        LBC("{"),
        RBC("}"),
        TRN("?"),
        RNG(":"),
        REL("@"),
        ASN("="),
        LSS("<"),
        LEQ("<="),
        GRT(">"),
        GEQ(">="),
        EQU("=="),
        NEQ("<>"),
        AND("&"),
        BND("&&"),
        ORR("|"),
        BOR("||"),
        INV("~"),
        NOT("!"),
        PRI("++x"),
        PRD("--x"),
        POI("x++"),
        POD("x--"),
        XOR("^"),
        SHL("<<"),
        SHR(">>"),
        USR(">>>"),
        NUM("#"),
        TXT("$"),
        LBL("_"),
        SYS("\\"),
        SEP(","),
        EOS("."),
        EOF("0");
        
        override fun toString() = rep
    }
}
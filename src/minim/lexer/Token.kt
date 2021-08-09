package minim.lexer

data class Token(val loc: Location, val type: Type, val value: Float = 0F) {
    enum class Type(private val rep: String) {
        VAL("V"),
        
        PRI("++x"),
        PRD("--x"),
        INV("~"),
        NOT("!"),
        
        POI("x++"),
        POD("x--"),
        INT("i"),
        FLT("f"),
        STR("s"),
        
        MUL("*"),
        DIV("/"),
        REM("%"),
        
        ADD("+"),
        SUB("-"),
        
        SHL("<<"),
        SHR(">>"),
        USR(">>>"),
        
        LSS("<"),
        LEQ("<="),
        GRT(">"),
        GEQ(">="),
        
        EQU("=="),
        NEQ("<>"),
    
        BND("&"),
    
        XOR("^"),
    
        BOR("|"),
    
        AND("&&"),
        
        ORR("||"),
    
        TRN("?"),
        
        ASN("="),
        
        NUM("#"),
        TXT("$"),
        LBL("_"),
        SYS("\\"),
        MEM("M"),
        
        DYN("C"),
        
        LPR("("),
        RPR(")"),
        LSQ("["),
        RSQ("]"),
        LBC("{"),
        RBC("}"),
        RNG(":"),
        REL("@"),
        SEP(","),
        EOS("."),
        EOF("0");
        
        override fun toString() = rep
    }
}
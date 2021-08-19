package minim.parser

import minim.lexer.Location

sealed class Expr(val loc: Location) {
    abstract fun <X> accept(visitor: Visitor<X>): X
    
    interface Visitor<X> {
        fun visit(expr: Expr) =
            expr.accept(this)
        
        fun visitNoneExpr(expr: None): X
        
        fun visitPrefixExpr(expr: Prefix): X
        
        fun visitPostfixExpr(expr: Postfix): X
        
        fun visitBinaryExpr(expr: Binary): X
        
        fun visitTernaryExpr(expr: Ternary): X
        
        fun visitNumberExpr(expr: Number): X
        
        fun visitArrayExpr(expr: Array): X
        
        fun visitSingleExpr(expr: Single): X
        
        fun visitFixedRangeExpr(expr: FixedRange): X
        
        fun visitRelativeRangeExpr(expr: RelativeRange): X
        
        fun visitDynamicLiteralExpr(expr: DynamicLiteral): X
    }
    
    object None : Expr(Location.none) {
        override fun <X> accept(visitor: Visitor<X>) =
            visitor.visitNoneExpr(this)
    }
    
    class Prefix(loc: Location, val operator: Operator, val expr: Expr) : Expr(loc) {
        enum class Operator(private val rep: String) {
            Increment("++"),
            Decrement("--"),
            Narrowed("??"),
            Toggled("!!"),
            Inverted("~~"),
            Negative("-"),
            Narrow("?"),
            Not("!"),
            Invert("~");
            
            companion object {
                operator fun get(rep: String) =
                    values().find { it.rep == rep }!!
            }
            
            override fun toString() = rep
        }
        
        override fun <X> accept(visitor: Visitor<X>) =
            visitor.visitPrefixExpr(this)
    }
    
    class Postfix(loc: Location, val operator: Operator, val expr: Expr) : Expr(loc) {
        enum class Operator(private val rep: String) {
            Increment("++"),
            Decrement("--"),
            Narrowed("??"),
            Toggled("!!"),
            Inverted("~~"),
            IntegerCast("i"),
            FloatCast("f"),
            StringCast("s");
            
            companion object {
                operator fun get(rep: String) =
                    values().find { it.rep == rep }!!
            }
            
            override fun toString() = rep
        }
        
        override fun <X> accept(visitor: Visitor<X>) =
            visitor.visitPostfixExpr(this)
    }
    
    class Binary(loc: Location, val operator: Operator, val left: Expr, val right: Expr) : Expr(loc) {
        enum class Operator(private val rep: String) {
            Multiply("*"),
            Divide("/"),
            Modulus("%"),
            Add("+"),
            Subtract("-"),
            ShiftLeft("<<"),
            ShiftRight(">>"),
            UnsignedShiftRight(">>>"),
            Less("<"),
            LessEqual("<="),
            Greater(">"),
            GreaterEqual(">="),
            Equal("=="),
            NotEqual("<>"),
            BitAnd("&"),
            Xor("^"),
            BitOr("|"),
            And("&&"),
            Or("||"),
            Assign("=");
            
            companion object {
                operator fun get(rep: String) =
                    values().find { it.rep == rep }!!
            }
            
            override fun toString() = rep
        }
        
        override fun <X> accept(visitor: Visitor<X>) =
            visitor.visitBinaryExpr(this)
    }
    
    class Ternary(loc: Location, val test: Expr, val yes: Expr, val no: Expr) : Expr(loc) {
        override fun <X> accept(visitor: Visitor<X>) =
            visitor.visitTernaryExpr(this)
    }
    
    class Number(loc: Location, val value: Float) : Expr(loc) {
        override fun <X> accept(visitor: Visitor<X>) =
            visitor.visitNumberExpr(this)
    }
    
    class Array(loc: Location, val elements: List<Expr>) : Expr(loc) {
        override fun <X> accept(visitor: Visitor<X>) =
            visitor.visitArrayExpr(this)
    }
    
    class Single(loc: Location, val index: Expr) : Expr(loc) {
        override fun <X> accept(visitor: Visitor<X>) =
            visitor.visitSingleExpr(this)
    }
    
    class FixedRange(loc: Location, val start: Expr, val end: Expr, val step: Expr) : Expr(loc) {
        override fun <X> accept(visitor: Visitor<X>) =
            visitor.visitFixedRangeExpr(this)
    }
    
    class RelativeRange(loc: Location, val start: Expr, val count: Expr, val step: Expr) : Expr(loc) {
        override fun <X> accept(visitor: Visitor<X>) =
            visitor.visitRelativeRangeExpr(this)
    }
    
    class DynamicLiteral(loc: Location, val name: Name) : Expr(loc) {
        enum class Name {
            A, C, R, S;
            
            companion object {
                operator fun contains(char: Char) =
                    get(char) != null
                
                operator fun get(char: Char) =
                    values().find { it.name == "$char" }
            }
        }
        
        override fun <X> accept(visitor: Visitor<X>) =
            visitor.visitDynamicLiteralExpr(this)
    }
}
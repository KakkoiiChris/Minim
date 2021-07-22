package minim.parser

import minim.lexer.Location
import minim.lexer.Token

sealed class Expr(val loc: Location) {
    abstract fun <X> accept(visitor: Visitor<X>): X
    
    interface Visitor<X> {
        fun visit(expr: Expr) =
            expr.accept(this)
        
        fun visitNoneExpr(expr: None): X
        
        fun visitUnaryExpr(expr: Unary): X
        
        fun visitBinaryExpr(expr: Binary): X
        
        fun visitTernaryExpr(expr: Ternary): X
        
        fun visitNumberExpr(expr: Number): X
        
        fun visitArrayExpr(expr: Array): X
        
        fun visitVariableExpr(expr: Variable): X
        
        fun visitFixedRangeExpr(expr: FixedRange): X
        
        fun visitRelativeRangeExpr(expr: RelativeRange): X
    }
    
    object None : Expr(Location.none) {
        override fun <X> accept(visitor: Visitor<X>) =
            visitor.visitNoneExpr(this)
    }
    
    class Unary(loc: Location, val op: Token.Type, val expr: Expr) : Expr(loc) {
        override fun <X> accept(visitor: Visitor<X>) =
            visitor.visitUnaryExpr(this)
    }
    
    class Binary(loc: Location, val op: Token.Type, val left: Expr, val right: Expr) : Expr(loc) {
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
    
    class Variable(loc: Location, val index: Expr) : Expr(loc) {
        override fun <X> accept(visitor: Visitor<X>) =
            visitor.visitVariableExpr(this)
    }
    
    class FixedRange(loc: Location, val start: Expr, val end: Expr, val step: Expr) : Expr(loc) {
        override fun <X> accept(visitor: Visitor<X>) =
            visitor.visitFixedRangeExpr(this)
    }
    
    class RelativeRange(loc: Location, val start: Expr, val count: Expr, val step: Expr) : Expr(loc) {
        override fun <X> accept(visitor: Visitor<X>) =
            visitor.visitRelativeRangeExpr(this)
    }
}
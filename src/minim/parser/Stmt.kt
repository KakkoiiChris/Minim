package minim.parser

import minim.lexer.Location

sealed class Stmt(val loc: Location) {
    abstract fun <X> accept(visitor: Visitor<X>): X
    
    interface Visitor<X> {
        fun visit(stmt: Stmt) =
            stmt.accept(this)
        
        fun visitNoneStmt(stmt: None): X
        
        fun visitNumberInStmt(stmt: NumberIn): X
        
        fun visitNumberOutStmt(stmt: NumberOut): X
        
        fun visitTextInStmt(stmt: TextIn): X
        
        fun visitTextOutStmt(stmt: TextOut): X
        
        fun visitLabelStmt(stmt: Label): X
        
        fun visitGotoStmt(stmt: Goto): X
        
        fun visitJumpStmt(stmt: Jump): X
    
        fun visitGosubStmt(stmt: Gosub): X
    
        fun visitReturnStmt(stmt: Return): X
        
        fun visitSystemArgStmt(stmt: SystemArg): X
        
        fun visitSystemCallStmt(stmt: SystemCall): X
        
        fun visitVariableAssignStmt(stmt: VariableAssign): X
        
        fun visitFixedRangeAssignStmt(stmt: FixedRangeAssign): X
        
        fun visitRelativeRangeAssignStmt(stmt: RelativeRangeAssign): X
        
        fun visitExpressionStmt(stmt: Expression): X
    }
    
    class None(loc: Location) : Stmt(loc) {
        override fun <X> accept(visitor: Visitor<X>): X =
            visitor.visitNoneStmt(this)
    }
    
    class NumberIn(loc: Location, val expr: Expr) : Stmt(loc) {
        override fun <X> accept(visitor: Visitor<X>): X =
            visitor.visitNumberInStmt(this)
    }
    
    class NumberOut(loc: Location, val expr: Expr) : Stmt(loc) {
        override fun <X> accept(visitor: Visitor<X>): X =
            visitor.visitNumberOutStmt(this)
    }
    
    class TextIn(loc: Location, val expr: Expr) : Stmt(loc) {
        override fun <X> accept(visitor: Visitor<X>): X =
            visitor.visitTextInStmt(this)
    }
    
    class TextOut(loc: Location, val expr: Expr) : Stmt(loc) {
        override fun <X> accept(visitor: Visitor<X>): X =
            visitor.visitTextOutStmt(this)
    }
    
    class Label(loc: Location, val id: Expr) : Stmt(loc) {
        override fun <X> accept(visitor: Visitor<X>) =
            visitor.visitLabelStmt(this)
    }
    
    class Goto(loc: Location, val id: Expr) : Stmt(loc) {
        override fun <X> accept(visitor: Visitor<X>) =
            visitor.visitGotoStmt(this)
    }
    
    class Jump(loc: Location, val condition: Expr) : Stmt(loc) {
        override fun <X> accept(visitor: Visitor<X>) =
            visitor.visitJumpStmt(this)
    }
    
    class Gosub(loc: Location, val id: Expr):Stmt(loc) {
        override fun <X> accept(visitor: Visitor<X>) =
            visitor.visitGosubStmt(this)
    }
    
    class Return(loc: Location, val expr: Expr):Stmt(loc) {
        override fun <X> accept(visitor: Visitor<X>) =
            visitor.visitReturnStmt(this)
    }
    
    class SystemArg(loc: Location, val expr: Expr) : Stmt(loc) {
        override fun <X> accept(visitor: Visitor<X>): X =
            visitor.visitSystemArgStmt(this)
    }
    
    class SystemCall(loc: Location, val expr: Expr) : Stmt(loc) {
        override fun <X> accept(visitor: Visitor<X>): X =
            visitor.visitSystemCallStmt(this)
    }
    
    class VariableAssign(loc: Location, val variable: Expr.Variable, val expr: Expr) : Stmt(loc) {
        override fun <X> accept(visitor: Visitor<X>): X =
            visitor.visitVariableAssignStmt(this)
    }
    
    class FixedRangeAssign(loc: Location, val range: Expr.FixedRange, val expr: Expr) : Stmt(loc) {
        override fun <X> accept(visitor: Visitor<X>): X =
            visitor.visitFixedRangeAssignStmt(this)
    }
    
    class RelativeRangeAssign(loc: Location, val range: Expr.RelativeRange, val expr: Expr) : Stmt(loc) {
        override fun <X> accept(visitor: Visitor<X>): X =
            visitor.visitRelativeRangeAssignStmt(this)
    }
    
    class Expression(loc: Location, val expr: Expr) : Stmt(loc) {
        override fun <X> accept(visitor: Visitor<X>) =
            visitor.visitExpressionStmt(this)
    }
}
package minim.parser

import minim.lexer.Location

typealias Stmts = List<Stmt>

/**
 * Subclass for all possible statements.
 *
 * @param loc the location of this statement
 */
sealed class Stmt(val loc: Location) {
    /**
     * Delegates the visitor function specific to this statement.
     *
     * @param visitor the visitor that this statement was passed to
     *
     * @return the result of evaluating this statement
     */
    abstract fun <X> accept(visitor: Visitor<X>): X
    
    /**
     * Employs a visitor pattern for traversing over statements.
     */
    interface Visitor<X> {
        /**
         * The default method that all statements are passed to, that delegates to the specific method for each statement.
         *
         * @param stmt the statement to evaluate
         *
         * @return the result of evaluating the given statement
         */
        fun visit(stmt: Stmt) =
            stmt.accept(this)
        
        /**
         * The method for evaluating the [None] statement.
         *
         * @param stmt the statement to evaluate
         *
         * @return the result of evaluating the given statement
         */
        fun visitNoneStmt(stmt: None): X
        
        /**
         * The method for evaluating the [NumberIn] statement.
         *
         * @param stmt the statement to evaluate
         *
         * @return the result of evaluating the given statement
         */
        fun visitNumberInStmt(stmt: NumberIn): X
        
        /**
         * The method for evaluating the [NumberOut] statement.
         *
         * @param stmt the statement to evaluate
         *
         * @return the result of evaluating the given statement
         */
        fun visitNumberOutStmt(stmt: NumberOut): X
        
        /**
         * The method for evaluating the [TextIn] statement.
         *
         * @param stmt the statement to evaluate
         *
         * @return the result of evaluating the given statement
         */
        fun visitTextInStmt(stmt: TextIn): X
        
        /**
         * The method for evaluating the [TextOut] statement.
         *
         * @param stmt the statement to evaluate
         *
         * @return the result of evaluating the given statement
         */
        fun visitTextOutStmt(stmt: TextOut): X
        
        /**
         * The method for evaluating the [TextFlush] statement.
         *
         * @param stmt the statement to evaluate
         *
         * @return the result of evaluating the given statement
         */
        fun visitTextFlushStmt(stmt: TextFlush): X
        
        /**
         * The method for evaluating the [Label] statement.
         *
         * @param stmt the statement to evaluate
         *
         * @return the result of evaluating the given statement
         */
        fun visitLabelStmt(stmt: Label): X
        
        /**
         * The method for evaluating the [Goto] statement.
         *
         * @param stmt the statement to evaluate
         *
         * @return the result of evaluating the given statement
         */
        fun visitGotoStmt(stmt: Goto): X
        
        /**
         * The method for evaluating the [Jump] statement.
         *
         * @param stmt the statement to evaluate
         *
         * @return the result of evaluating the given statement
         */
        fun visitJumpStmt(stmt: Jump): X
        
        /**
         * The method for evaluating the [Gosub] statement.
         *
         * @param stmt the statement to evaluate
         *
         * @return the result of evaluating the given statement
         */
        fun visitGosubStmt(stmt: Gosub): X
        
        /**
         * The method for evaluating the [Return] statement.
         *
         * @param stmt the statement to evaluate
         *
         * @return the result of evaluating the given statement
         */
        fun visitReturnStmt(stmt: Return): X
        
        /**
         * The method for evaluating the [SystemArg] statement.
         *
         * @param stmt the statement to evaluate
         *
         * @return the result of evaluating the given statement
         */
        fun visitSystemArgStmt(stmt: SystemArg): X
        
        /**
         * The method for evaluating the [SystemYield] statement.
         *
         * @param stmt the statement to evaluate
         *
         * @return the result of evaluating the given statement
         */
        fun visitSystemYieldStmt(stmt: SystemYield): X
        
        /**
         * The method for evaluating the [SystemCall] statement.
         *
         * @param stmt the statement to evaluate
         *
         * @return the result of evaluating the given statement
         */
        fun visitSystemCallStmt(stmt: SystemCall): X
        
        /**
         * The method for evaluating the [SystemFlush] statement.
         *
         * @param stmt the statement to evaluate
         *
         * @return the result of evaluating the given statement
         */
        fun visitSystemFlushStmt(stmt: SystemFlush): X
        
        /**
         * The method for evaluating the [MemoryPush] statement.
         *
         * @param stmt the statement to evaluate
         *
         * @return the result of evaluating the given statement
         */
        fun visitMemoryPushStmt(stmt: MemoryPush): X
        
        /**
         * The method for evaluating the [MemoryPop] statement.
         *
         * @param stmt the statement to evaluate
         *
         * @return the result of evaluating the given statement
         */
        fun visitMemoryPopStmt(stmt: MemoryPop): X
        
        /**
         * The method for evaluating the [MemoryIn] statement.
         *
         * @param stmt the statement to evaluate
         *
         * @return the result of evaluating the given statement
         */
        fun visitMemoryInStmt(stmt: MemoryIn): X
        
        /**
         * The method for evaluating the [MemoryOut] statement.
         *
         * @param stmt the statement to evaluate
         *
         * @return the result of evaluating the given statement
         */
        fun visitMemoryOutStmt(stmt: MemoryOut): X
        
        /**
         * The method for evaluating the [MemoryFlush] statement.
         *
         * @param stmt the statement to evaluate
         *
         * @return the result of evaluating the given statement
         */
        fun visitMemoryFlushStmt(stmt: MemoryFlush): X
        
        /**
         * The method for evaluating the [SingleAssign] statement.
         *
         * @param stmt the statement to evaluate
         *
         * @return the result of evaluating the given statement
         */
        fun visitSingleAssignStmt(stmt: SingleAssign): X
        
        /**
         * The method for evaluating the [FixedRangeAssign] statement.
         *
         * @param stmt the statement to evaluate
         *
         * @return the result of evaluating the given statement
         */
        fun visitFixedRangeAssignStmt(stmt: FixedRangeAssign): X
        
        /**
         * The method for evaluating the [RelativeRangeAssign] statement.
         *
         * @param stmt the statement to evaluate
         *
         * @return the result of evaluating the given statement
         */
        fun visitRelativeRangeAssignStmt(stmt: RelativeRangeAssign): X
        
        /**
         * The method for evaluating the [Expression] statement.
         *
         * @param stmt the statement to evaluate
         *
         * @return the result of evaluating the given statement
         */
        fun visitExpressionStmt(stmt: Expression): X
    }
    
    /**
     * A subclass representing an empty statement.
     *
     * @param loc the location of this statement
     */
    class None(loc: Location) : Stmt(loc) {
        override fun <X> accept(visitor: Visitor<X>): X =
            visitor.visitNoneStmt(this)
    }
    
    /**
     * A subclass representing a numerical input statement.
     *
     * @param loc the location of this statement
     * @param isIntMode whether this statement will convert its result to an integer
     * @param expr the target to store the result in
     */
    class NumberIn(loc: Location, val isIntMode: Boolean, val expr: Expr) : Stmt(loc) {
        override fun <X> accept(visitor: Visitor<X>): X =
            visitor.visitNumberInStmt(this)
    }
    
    /**
     * A subclass representing a numerical output statement.
     *
     * @param loc the location of this statement
     * @param isIntMode whether this statement will convert its result to an integer
     * @param expr the value to print as a number
     */
    class NumberOut(loc: Location, val isIntMode: Boolean, val expr: Expr) : Stmt(loc) {
        override fun <X> accept(visitor: Visitor<X>): X =
            visitor.visitNumberOutStmt(this)
    }
    
    /**
     * A subclass representing a text input statement.
     *
     * @param loc the location of this statement
     * @param isIntMode whether this statement will convert its result to an integer
     * @param expr the target to store the result in
     */
    class TextIn(loc: Location, val isIntMode: Boolean, val expr: Expr) : Stmt(loc) {
        override fun <X> accept(visitor: Visitor<X>): X =
            visitor.visitTextInStmt(this)
    }
    
    /**
     * A subclass representing a text output statement.
     *
     * @param loc the location of this statement
     * @param expr the value to print as a character
     */
    class TextOut(loc: Location, val expr: Expr) : Stmt(loc) {
        override fun <X> accept(visitor: Visitor<X>): X =
            visitor.visitTextOutStmt(this)
    }
    
    /**
     * A subclass representing a text flush statement.
     *
     * @param loc the location of this statement
     */
    class TextFlush(loc: Location) : Stmt(loc) {
        override fun <X> accept(visitor: Visitor<X>): X =
            visitor.visitTextFlushStmt(this)
    }
    
    /**
     * A subclass representing a label statement.
     *
     * @param loc the location of this statement
     * @param id the identifier of the label to define
     */
    class Label(loc: Location, val id: Expr) : Stmt(loc) {
        override fun <X> accept(visitor: Visitor<X>) =
            visitor.visitLabelStmt(this)
    }
    
    /**
     * A subclass representing a goto statement.
     *
     * @param loc the location of this statement
     * @param id the identifier of the label to go to
     */
    class Goto(loc: Location, val id: Expr) : Stmt(loc) {
        override fun <X> accept(visitor: Visitor<X>) =
            visitor.visitGotoStmt(this)
    }
    
    /**
     * A subclass representing a jump statement.
     *
     * @param loc the location of this statement
     * @param condition whether the next statement is skipped
     */
    class Jump(loc: Location, val condition: Expr) : Stmt(loc) {
        override fun <X> accept(visitor: Visitor<X>) =
            visitor.visitJumpStmt(this)
    }
    
    /**
     * A subclass representing a gosub statement.
     *
     * @param loc the location of this statement
     * @param id the identifier of the label to gosub to
     */
    class Gosub(loc: Location, val id: Expr) : Stmt(loc) {
        override fun <X> accept(visitor: Visitor<X>) =
            visitor.visitGosubStmt(this)
    }
    
    /**
     * A subclass representing a return statement.
     *
     * @param loc the location of this statement
     */
    class Return(loc: Location) : Stmt(loc) {
        override fun <X> accept(visitor: Visitor<X>) =
            visitor.visitReturnStmt(this)
    }
    
    /**
     * A subclass representing a system argument statement.
     *
     * @param loc the location of this statement
     * @param expr the argument to pass to the system function
     */
    class SystemArg(loc: Location, val expr: Expr) : Stmt(loc) {
        override fun <X> accept(visitor: Visitor<X>): X =
            visitor.visitSystemArgStmt(this)
    }
    
    /**
     * A subclass representing a system yield statement.
     *
     * @param loc the location of this statement
     * @param isIntMode whether this statement will convert its result to an integer
     * @param expr the target to store the result in
     */
    class SystemYield(loc: Location, val isIntMode: Boolean, val expr: Expr) : Stmt(loc) {
        override fun <X> accept(visitor: Visitor<X>): X =
            visitor.visitSystemYieldStmt(this)
    }
    
    /**
     * A subclass representing a system call statement.
     *
     * @param loc the location of this statement
     */
    class SystemCall(loc: Location) : Stmt(loc) {
        override fun <X> accept(visitor: Visitor<X>): X =
            visitor.visitSystemCallStmt(this)
    }
    
    /**
     * A subclass representing a system flush statement.
     *
     * @param loc the location of this statement
     */
    class SystemFlush(loc: Location) : Stmt(loc) {
        override fun <X> accept(visitor: Visitor<X>): X =
            visitor.visitSystemFlushStmt(this)
    }
    
    /**
     * A subclass representing a memory push statement.
     *
     * @param loc the location of this statement
     */
    class MemoryPush(loc: Location) : Stmt(loc) {
        override fun <X> accept(visitor: Visitor<X>): X =
            visitor.visitMemoryPushStmt(this)
    }
    
    /**
     * A subclass representing a memory pop statement.
     *
     * @param loc the location of this statement
     */
    class MemoryPop(loc: Location) : Stmt(loc) {
        override fun <X> accept(visitor: Visitor<X>): X =
            visitor.visitMemoryPopStmt(this)
    }
    
    /**
     * A subclass representing a memory input statement.
     *
     * @param loc the location of this statement
     * @param expr the target to store the result in
     */
    class MemoryIn(loc: Location, val expr: Expr) : Stmt(loc) {
        override fun <X> accept(visitor: Visitor<X>): X =
            visitor.visitMemoryInStmt(this)
    }
    
    /**
     * A subclass representing a memory output statement.
     *
     * @param loc the location of this statement
     * @param expr the expression to store in the memory queue
     */
    class MemoryOut(loc: Location, val expr: Expr) : Stmt(loc) {
        override fun <X> accept(visitor: Visitor<X>): X =
            visitor.visitMemoryOutStmt(this)
    }
    
    /**
     * A subclass representing a memory flush statement.
     *
     * @param loc the location of this statement
     */
    class MemoryFlush(loc: Location) : Stmt(loc) {
        override fun <X> accept(visitor: Visitor<X>): X =
            visitor.visitMemoryFlushStmt(this)
    }
    
    /**
     * A subclass representing a single assignment statement.
     *
     * @param loc the location of this statement
     * @param single the location in memory to store to
     * @param expr the expression to store
     */
    class SingleAssign(loc: Location, val single: Expr.Single, val expr: Expr) : Stmt(loc) {
        override fun <X> accept(visitor: Visitor<X>): X =
            visitor.visitSingleAssignStmt(this)
    }
    
    /**
     * A subclass representing a fixed range assignment statement.
     *
     * @param loc the location of this statement
     * @param range the locations in memory to store to
     * @param expr the expression to store
     */
    class FixedRangeAssign(loc: Location, val range: Expr.FixedRange, val expr: Expr) : Stmt(loc) {
        override fun <X> accept(visitor: Visitor<X>): X =
            visitor.visitFixedRangeAssignStmt(this)
    }
    
    /**
     * A subclass representing a relative range assignment statement.
     *
     * @param loc the location of this statement
     * @param range the locations in memory to store to
     * @param expr the expression to store
     */
    class RelativeRangeAssign(loc: Location, val range: Expr.RelativeRange, val expr: Expr) : Stmt(loc) {
        override fun <X> accept(visitor: Visitor<X>): X =
            visitor.visitRelativeRangeAssignStmt(this)
    }
    
    /**
     * A subclass representing an expression statement.
     *
     * @param loc the location of this statement
     * @param expr the expression to evaluate
     */
    class Expression(loc: Location, val expr: Expr) : Stmt(loc) {
        override fun <X> accept(visitor: Visitor<X>) =
            visitor.visitExpressionStmt(this)
    }
}
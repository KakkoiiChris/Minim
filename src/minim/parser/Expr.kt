package minim.parser

import minim.lexer.Location
import minim.lexer.Token

/**
 * Subclass for all possible expressions.
 *
 * @param loc the location of this expression
 */
sealed class Expr(val loc: Location) {
    /**
     * Delegates the visitor function specific to this expression.
     *
     * @param visitor the visitor that this expression was passed to
     *
     * @return the result of evaluating this expression
     */
    abstract fun <X> accept(visitor: Visitor<X>): X
    
    /**
     * Employs a visitor pattern for traversing through expressions.
     */
    interface Visitor<X> {
        /**
         * The default method that all expressions are passed to, that delegates to the specific method for each expression.
         *
         * @param expr the expression to evaluate
         *
         * @return the result of evaluating the given expression
         */
        fun visit(expr: Expr) =
            expr.accept(this)
    
        /**
         * The method for evaluating the [None] expression.
         *
         * @param expr the expression to evaluate
         *
         * @return the result of evaluating the given expression
         */
        fun visitNoneExpr(expr: None): X
        
        /**
         * The method for evaluating the [Prefix] expression.
         *
         * @param expr the expression to evaluate
         *
         * @return the result of evaluating the given expression
         */
        fun visitPrefixExpr(expr: Prefix): X
        
        /**
         * The method for evaluating the [Postfix] expression.
         *
         * @param expr the expression to evaluate
         *
         * @return the result of evaluating the given expression
         */
        fun visitPostfixExpr(expr: Postfix): X
        
        /**
         * The method for evaluating the [Binary] expression.
         *
         * @param expr the expression to evaluate
         *
         * @return the result of evaluating the given expression
         */
        fun visitBinaryExpr(expr: Binary): X
        
        /**
         * The method for evaluating the [Ternary] expression.
         *
         * @param expr the expression to evaluate
         *
         * @return the result of evaluating the given expression
         */
        fun visitTernaryExpr(expr: Ternary): X
        
        /**
         * The method for evaluating the [Number] expression.
         *
         * @param expr the expression to evaluate
         *
         * @return the result of evaluating the given expression
         */
        fun visitNumberExpr(expr: Number): X
        
        /**
         * The method for evaluating the [Array] expression.
         *
         * @param expr the expression to evaluate
         *
         * @return the result of evaluating the given expression
         */
        fun visitArrayExpr(expr: Array): X
        
        /**
         * The method for evaluating the [Single] expression.
         *
         * @param expr the expression to evaluate
         *
         * @return the result of evaluating the given expression
         */
        fun visitSingleExpr(expr: Single): X
        
        /**
         * The method for evaluating the [FixedRange] expression.
         *
         * @param expr the expression to evaluate
         *
         * @return the result of evaluating the given expression
         */
        fun visitFixedRangeExpr(expr: FixedRange): X
        
        /**
         * The method for evaluating the [RelativeRange] expression.
         *
         * @param expr the expression to evaluate
         *
         * @return the result of evaluating the given expression
         */
        fun visitRelativeRangeExpr(expr: RelativeRange): X
        
        /**
         * The method for evaluating the [DynamicLiteral] expression.
         *
         * @param expr the expression to evaluate
         *
         * @return the result of evaluating the given expression
         */
        fun visitDynamicLiteralExpr(expr: DynamicLiteral): X
    }
    
    /**
     * A subclass representing an empty expression.
     */
    object None : Expr(Location.none) {
        override fun <X> accept(visitor: Visitor<X>) =
            visitor.visitNoneExpr(this)
    }
    
    /**
     * A subclass representing a prefix operator expression.
     *
     * @param loc the location of this expression
     * @param operator the operator for this expression
     * @param expr the operand for this expression
     */
    class Prefix(loc: Location, val operator: Operator, val expr: Expr) : Expr(loc) {
        /**
         * An enumeration of all available prefix operators.
         *
         * @param type the token type associated with this operator
         */
        enum class Operator(private val type: Token.Type) {
            Increment(Token.Type.DoublePlus),
            Decrement(Token.Type.DoubleMinus),
            Narrowed(Token.Type.DoubleQuestion),
            Toggled(Token.Type.DoubleExclamation),
            Inverted(Token.Type.DoubleTilde),
            Negative(Token.Type.Minus),
            Narrow(Token.Type.Question),
            Not(Token.Type.Exclamation),
            Invert(Token.Type.Tilde);
            
            companion object {
                /**
                 * Gets the operator associated with the given token type.
                 *
                 * @param type the token type to check
                 *
                 * @return the associated operator
                 */
                operator fun get(type: Token.Type) =
                    values().find { it.type == type }!!
            }
            
            /**
             * Gets the string representation of this operator.
             *
             * @return Ex. '??', '~'
             */
            override fun toString() =
                type.toString()
        }
        
        override fun <X> accept(visitor: Visitor<X>) =
            visitor.visitPrefixExpr(this)
    }
    
    /**
     * A subclass representing a postfix operator expression.
     *
     * @param loc the location of this expression
     * @param operator the operator for this expression
     * @param expr the operand for this expression
     */
    class Postfix(loc: Location, val operator: Operator, val expr: Expr) : Expr(loc) {
        /**
         * An enumeration of all available postfix operators.
         *
         * @param type the token type associated with this operator
         */
        enum class Operator(private val type: Token.Type) {
            Increment(Token.Type.DoublePlus),
            Decrement(Token.Type.DoubleMinus),
            Narrowed(Token.Type.DoubleQuestion),
            Toggled(Token.Type.DoubleExclamation),
            Inverted(Token.Type.DoubleTilde),
            IntegerCast(Token.Type.SmallI),
            FloatCast(Token.Type.SmallF),
            StringCast(Token.Type.SmallS);
            
            companion object {
                /**
                 * Gets the operator associated with the given token type.
                 *
                 * @param type the token type to check
                 *
                 * @return the associated operator
                 */
                operator fun get(type: Token.Type) =
                    values().find { it.type == type }!!
            }
            
            /**
             * Gets the string representation of this operator.
             *
             * @return Ex. '--', 'i'
             */
            override fun toString() =
                type.toString()
        }
        
        override fun <X> accept(visitor: Visitor<X>) =
            visitor.visitPostfixExpr(this)
    }
    
    /**
     * A subclass representing a binary operator expression.
     *
     * @param loc the location of this expression
     * @param operator the operator for this expression
     * @param left the left operand for this expression
     * @param right the right operand for this expression
     */
    class Binary(loc: Location, val operator: Operator, val left: Expr, val right: Expr) : Expr(loc) {
        /**
         * An enumeration of all available binary operators.
         *
         * @param type the token type associated with this operator
         */
        enum class Operator(private val type: Token.Type) {
            Multiply(Token.Type.Star),
            Divide(Token.Type.Slash),
            Modulus(Token.Type.Percent),
            Add(Token.Type.Plus),
            Subtract(Token.Type.Minus),
            ShiftLeft(Token.Type.DoubleLess),
            ShiftRight(Token.Type.DoubleGreater),
            UnsignedShiftRight(Token.Type.TripleGreater),
            Less(Token.Type.LessSign),
            LessEqual(Token.Type.LessEqualSign),
            Greater(Token.Type.GreaterSign),
            GreaterEqual(Token.Type.GreaterEqualSign),
            Equal(Token.Type.DoubleEqual),
            NotEqual(Token.Type.LessGreater),
            BitAnd(Token.Type.Ampersand),
            Xor(Token.Type.Caret),
            BitOr(Token.Type.Pipe),
            And(Token.Type.DoubleAmpersand),
            Or(Token.Type.DoublePipe),
            Assign(Token.Type.EqualSign);
            
            companion object {
                /**
                 * Gets the operator associated with the given token type.
                 *
                 * @param type the token type to check
                 *
                 * @return the associated operator
                 */
                operator fun get(type: Token.Type) =
                    values().find { it.type == type }!!
            }
    
            /**
             * Gets the string representation of this operator.
             *
             * @return Ex. '*', '>>>'
             */
            override fun toString() =
                type.toString()
        }
        
        override fun <X> accept(visitor: Visitor<X>) =
            visitor.visitBinaryExpr(this)
    }
    
    /**
     * A subclass representing a ternary expression.
     *
     * @param loc the location of this expression
     * @param condition the expression to test
     * @param trueExpr the expression to evaluate if the condition is true
     * @param falseExpr the expression to evaluate if the condition is false
     */
    class Ternary(loc: Location, val condition: Expr, val trueExpr: Expr, val falseExpr: Expr) : Expr(loc) {
        override fun <X> accept(visitor: Visitor<X>) =
            visitor.visitTernaryExpr(this)
    }
    
    /**
     * A subclass representing a number expression.
     *
     * @param loc the location of this expression
     * @param value the value of this number
     */
    class Number(loc: Location, val value: Float) : Expr(loc) {
        override fun <X> accept(visitor: Visitor<X>) =
            visitor.visitNumberExpr(this)
    }
    
    /**
     * A subclass representing an array expression.
     *
     * @param loc the location of this expression
     * @param elements the elements to populate the array with
     */
    class Array(loc: Location, val elements: List<Expr>) : Expr(loc) {
        override fun <X> accept(visitor: Visitor<X>) =
            visitor.visitArrayExpr(this)
    }
    
    /**
     * A subclass representing a single access expression.
     *
     * @param loc the location of this expression
     * @param index the memory index to access
     */
    class Single(loc: Location, val index: Expr) : Expr(loc) {
        override fun <X> accept(visitor: Visitor<X>) =
            visitor.visitSingleExpr(this)
    }
    
    /**
     * A subclass representing a fixed range access expression.
     *
     * @param loc the location of this expression
     * @param start the first index to access
     * @param end the last index to access
     * @param step the distance between each index
     */
    class FixedRange(loc: Location, val start: Expr, val end: Expr, val step: Expr) : Expr(loc) {
        override fun <X> accept(visitor: Visitor<X>) =
            visitor.visitFixedRangeExpr(this)
    }
    
    /**
     * A subclass representing a relative range access expression.
     *
     * @param loc the location of this expression
     * @param start the first index to access
     * @param count the amount of indices to access
     * @param step the distance between each index
     */
    class RelativeRange(loc: Location, val start: Expr, val count: Expr, val step: Expr) : Expr(loc) {
        override fun <X> accept(visitor: Visitor<X>) =
            visitor.visitRelativeRangeExpr(this)
    }
    
    /**
     * A subclass representing a dynamic literal expression.
     *
     * @param loc the location of this expression
     * @param name the name of this dynamic literal
     */
    class DynamicLiteral(loc: Location, val name: Name) : Expr(loc) {
        /**
         * An enumeration of all available dynamic literals.
         */
        enum class Name {
            /**
             * Program **A**rguments
             */
            A,
    
            /**
             * Program **C**ounter
             */
            C,
    
            /**
             * Program Arguments **L**ength
             */
            L,
    
            /**
             * **R**andom Number
             */
            R,
    
            /**
             * Memory **S**ize
             */
            S;
            
            companion object {
                /**
                 * Gets if the given char is a valid dynamic literal name.
                 *
                 * @param char the name to check
                 *
                 * @return `true` if the given name exists, or `false` otherwise
                 */
                operator fun contains(char: Char) =
                    get(char) != null
                
                /**
                 * Gets the [Name] based on the given character if it exists.
                 *
                 * @param char the name to check
                 *
                 * @return the name that matches the given character, or null if it doesn't exist
                 */
                operator fun get(char: Char) =
                    values().find { it.name == "$char" }
            }
    
            /**
             * Gets the string representation of this operator.
             *
             * @return Ex. 'A', 'R'
             */
            override fun toString() =
                name
        }
        
        override fun <X> accept(visitor: Visitor<X>) =
            visitor.visitDynamicLiteralExpr(this)
    }
}
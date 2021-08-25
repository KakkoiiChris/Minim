package minim.parser

import minim.lexer.Location
import minim.lexer.Token
import minim.lexer.Token.Type.*
import minim.lexer.Token.Type.Number
import minim.parser.Expr.Binary.Operator.*
import minim.util.*

class Parser(private val tokens: List<Token>) {
    private var pos = 0
    
    fun parse(): List<Stmt> {
        val stmts = mutableListOf<Stmt>()
        
        while (!skip(EndOfFile)) {
            stmts.add(stmt())
        }
        
        return stmts
    }
    
    private fun peek() =
        if (pos < tokens.size)
            tokens[pos]
        else
            tokens.last()
    
    private fun step() {
        pos++
    }
    
    private fun match(vararg types: Token.Type): Boolean {
        for (type in types) {
            if (peek().type == type) {
                return true
            }
        }
        
        return false
    }
    
    private fun skip(type: Token.Type) =
        if (peek().type == type) {
            step()
            true
        }
        else {
            false
        }
    
    private fun mustSkip(type: Token.Type) {
        if (!skip(type)) {
            invalidTypeError(peek().type, type, here())
        }
    }
    
    private fun here() =
        peek().loc
    
    private fun stmt(): Stmt {
        val stmt = when {
            match(Number)     -> numeric()
            
            match(Dollar)     -> text()
            
            match(Underscore) -> control()
            
            match(Backslash)  -> system()
            
            match(BigM)       -> memory()
            
            match(Dot)        -> Stmt.None(here())
            
            else              -> expression()
        }
        
        mustSkip(Dot)
        
        return stmt
    }
    
    private fun isIntMode() = when {
        skip(SmallI) -> true
        
        skip(SmallF) -> false
        
        else         -> false
    }
    
    private fun numeric(): Stmt {
        val loc = here()
        
        mustSkip(Number)
        
        return when {
            skip(LessSign)    -> Stmt.NumberOut(loc, isIntMode(), expr())
            
            skip(GreaterSign) -> Stmt.NumberIn(loc, isIntMode(), expr())
            
            else              -> invalidStatementHeaderError("#${peek().type}", loc)
        }
    }
    
    private fun text(): Stmt {
        val loc = here()
        
        mustSkip(Dollar)
        
        return when {
            skip(LessSign)    -> Stmt.TextOut(loc, isIntMode(), expr())
            
            skip(GreaterSign) -> Stmt.TextIn(loc, isIntMode(), expr())
            
            skip(Exclamation) -> Stmt.TextFlush(loc)
            
            else              -> invalidStatementHeaderError("\$${peek().type}", loc)
        }
    }
    
    private fun control(): Stmt {
        val loc = here()
        
        mustSkip(Underscore)
        
        return when {
            skip(LessSign)    -> {
                val id = expr()
                
                val fallback = if (skip(Colon)) expr() else Expr.None
                
                Stmt.Goto(loc, id, fallback)
            }
            
            skip(GreaterSign) -> Stmt.Label(loc, expr())
            
            skip(Caret)       -> Stmt.Jump(loc, expr())
            
            skip(Plus)        -> {
                val id = expr()
                
                val fallback = if (skip(Colon)) expr() else Expr.None
                
                Stmt.Gosub(loc, id, fallback)
            }
            
            skip(Minus)       -> Stmt.Return(loc)
            
            else              -> invalidStatementHeaderError("_${peek().type}", loc)
        }
    }
    
    private fun system(): Stmt {
        val loc = here()
        
        mustSkip(Backslash)
        
        return when {
            skip(LessSign)    -> Stmt.SystemArg(loc, expr())
            
            skip(GreaterSign) -> Stmt.SystemCall(loc, expr())
            
            skip(Exclamation) -> Stmt.SystemFlush(loc)
            
            else              -> invalidStatementHeaderError("\\${peek().type}", loc)
        }
    }
    
    private fun memory(): Stmt {
        val loc = here()
        
        mustSkip(BigM)
        
        return when {
            skip(Plus)        -> Stmt.MemoryPush(loc)
            
            skip(Minus)       -> Stmt.MemoryPop(loc)
            
            skip(LessSign)    -> Stmt.MemoryOut(loc, expr())
            
            skip(GreaterSign) -> Stmt.MemoryIn(loc, expr())
            
            skip(Exclamation) -> Stmt.MemoryFlush(loc)
            
            else              -> invalidStatementHeaderError("M${peek().type}", loc)
        }
    }
    
    private fun expression(): Stmt {
        val loc = here()
        
        val expr = expr()
        
        if (expr is Expr.Binary && expr.operator == Assign) {
            return when (val left = expr.left) {
                is Expr.Single        -> Stmt.SingleAssign(expr.loc, left, expr.right)
                
                is Expr.FixedRange    -> Stmt.FixedRangeAssign(expr.loc, left, expr.right)
                
                is Expr.RelativeRange -> Stmt.RelativeRangeAssign(expr.loc, left, expr.right)
                
                else                  -> Stmt.Expression(loc, expr)
            }
        }
        
        return Stmt.Expression(loc, expr)
    }
    
    private fun expr() = assign()
    
    private fun assign(): Expr {
        val expr = conditional()
        
        return if (match(EqualSign,
                StarEqual,
                SlashEqual,
                PercentEqual,
                PlusEqual,
                MinusEqual,
                DoubleLessEqual,
                DoubleGreaterEqual,
                TripleGreaterEqual,
                AndEqual,
                CaretEqual,
                PipeEqual,
                DoubleAmpersandEqual,
                DoublePipeEqual)
        ) {
            val op = peek()
            
            mustSkip(op.type)
            
            when (op.type) {
                StarEqual          -> compoundAssign(op.loc, expr, Multiply)
                
                SlashEqual         -> compoundAssign(op.loc, expr, Divide)
                
                PercentEqual       -> compoundAssign(op.loc, expr, Modulus)
                
                PlusEqual          -> compoundAssign(op.loc, expr, Add)
                
                MinusEqual         -> compoundAssign(op.loc, expr, Subtract)
                
                DoubleLessEqual    -> compoundAssign(op.loc, expr, ShiftLeft)
                
                DoubleGreaterEqual -> compoundAssign(op.loc, expr, ShiftRight)
                
                TripleGreaterEqual -> compoundAssign(op.loc, expr, UnsignedShiftRight)
                
                AndEqual           -> compoundAssign(op.loc, expr, BitAnd)
                
                CaretEqual         -> compoundAssign(op.loc, expr, Xor)
                
                PipeEqual          -> compoundAssign(op.loc, expr, BitOr)
                
                DoubleAmpersandEqual -> compoundAssign(op.loc, expr, And)
                
                DoublePipeEqual    -> compoundAssign(op.loc, expr, Or)
                
                else               -> Expr.Binary(op.loc, Assign, expr, assign())
            }
        }
        else {
            expr
        }
    }
    
    private fun compoundAssign(loc: Location, expr: Expr, operator: Expr.Binary.Operator) =
        Expr.Binary(loc, Assign, expr, Expr.Binary(loc, operator, expr, assign()))
    
    private fun conditional(): Expr {
        var node = logicalOr()
        
        if (match(Question)) {
            val op = peek()
            
            mustSkip(op.type)
            
            val yes = conditional()
            
            mustSkip(Colon)
            
            val no = conditional()
            
            node = Expr.Ternary(op.loc, node, yes, no)
        }
        
        return node
    }
    
    private fun logicalOr(): Expr {
        var node = logicalAnd()
        
        while (match(DoublePipe)) {
            val op = peek()
            
            mustSkip(op.type)
            
            node = Expr.Binary(op.loc, Expr.Binary.Operator[op.type], node, logicalAnd())
        }
        
        return node
    }
    
    private fun logicalAnd(): Expr {
        var node = bitwiseOr()
        
        while (match(DoubleAmpersand)) {
            val op = peek()
            
            mustSkip(op.type)
            
            node = Expr.Binary(op.loc, Expr.Binary.Operator[op.type], node, bitwiseOr())
        }
        
        return node
    }
    
    private fun bitwiseOr(): Expr {
        var node = bitwiseXor()
        
        while (match(Pipe)) {
            val op = peek()
            
            mustSkip(op.type)
            
            node = Expr.Binary(op.loc, Expr.Binary.Operator[op.type], node, bitwiseXor())
        }
        
        return node
    }
    
    private fun bitwiseXor(): Expr {
        var node = bitwiseAnd()
        
        while (match(Caret)) {
            val op = peek()
            
            mustSkip(op.type)
            
            node = Expr.Binary(op.loc, Expr.Binary.Operator[op.type], node, bitwiseAnd())
        }
        
        return node
    }
    
    private fun bitwiseAnd(): Expr {
        var node = equality()
        
        while (match(Ampersand)) {
            val op = peek()
            
            mustSkip(op.type)
            
            node = Expr.Binary(op.loc, Expr.Binary.Operator[op.type], node, equality())
        }
        
        return node
    }
    
    private fun equality(): Expr {
        var node = relational()
        
        while (match(DoubleEqual, LessGreater)) {
            val op = peek()
            
            mustSkip(op.type)
            
            node = Expr.Binary(op.loc, Expr.Binary.Operator[op.type], node, relational())
        }
        
        return node
    }
    
    private fun relational(): Expr {
        var node = shift()
        
        while (match(LessSign, LessEqualSign, GreaterSign, GreaterEqualSign)) {
            val op = peek()
            
            mustSkip(op.type)
            
            node = Expr.Binary(op.loc, Expr.Binary.Operator[op.type], node, shift())
        }
        
        return node
    }
    
    private fun shift(): Expr {
        var node = additive()
        
        while (match(DoubleLess, DoubleGreater, TripleGreater)) {
            val op = peek()
            
            mustSkip(op.type)
            
            node = Expr.Binary(op.loc, Expr.Binary.Operator[op.type], node, additive())
        }
        
        return node
    }
    
    private fun additive(): Expr {
        var node = multiplicative()
        
        while (match(Plus, Minus)) {
            val op = peek()
            
            mustSkip(op.type)
            
            node = Expr.Binary(op.loc, Expr.Binary.Operator[op.type], node, multiplicative())
        }
        
        return node
    }
    
    private fun multiplicative(): Expr {
        var node = prefix()
        
        while (match(Star, Slash, Percent)) {
            val op = peek()
            
            mustSkip(op.type)
            
            node = Expr.Binary(op.loc, Expr.Binary.Operator[op.type], node, prefix())
        }
        
        return node
    }
    
    private fun prefix(): Expr {
        return if (match(Minus,
                Exclamation,
                Question,
                Tilde,
                DoublePlus,
                DoubleMinus,
                DoubleQuestion,
                DoubleExclamation,
                DoubleTilde)
        ) {
            val op = peek()
            
            skip(op.type)
            
            Expr.Prefix(op.loc, Expr.Prefix.Operator[op.type], prefix())
        }
        else {
            postfix()
        }
    }
    
    private fun postfix(): Expr {
        var expr = terminal()
        
        while (match(DoublePlus, DoubleMinus, DoubleQuestion, DoubleExclamation, DoubleTilde, SmallI, SmallF, SmallS)) {
            val op = peek()
            
            skip(op.type)
            
            expr = Expr.Postfix(op.loc, Expr.Postfix.Operator[op.type], expr)
        }
        
        return expr
    }
    
    private fun terminal(): Expr {
        return when {
            match(Value)      -> value()
            
            match(LeftParen)  -> nested()
            
            match(LeftSquare) -> access()
            
            match(LeftBrace)  -> array()
            
            match(Dynamic)    -> dynamic()
            
            else              -> invalidTerminalError(peek().type, here())
        }
    }
    
    private fun value(): Expr.Number {
        val token = peek()
        
        mustSkip(Value)
        
        return Expr.Number(token.loc, token.value)
    }
    
    private fun nested(): Expr {
        mustSkip(LeftParen)
        
        val expr = expr()
        
        mustSkip(RightParen)
        
        return expr
    }
    
    private fun access(): Expr {
        val loc = here()
        
        mustSkip(LeftSquare)
        
        if (skip(RightSquare)) {
            return Expr.FixedRange(loc, Expr.None, Expr.None, Expr.None)
        }
        
        val a = if (match(Colon, At, RightSquare))
            Expr.None
        else
            expr()
        
        if (skip(RightSquare)) {
            return Expr.Single(loc, a)
        }
        
        val fixed = skip(Colon)
        
        if (!fixed) {
            mustSkip(At)
        }
        
        if (skip(RightSquare)) {
            return if (fixed) {
                Expr.FixedRange(loc, a, Expr.None, Expr.None)
            }
            else {
                noRelativeRangeCountError(here())
            }
        }
        
        val b = if (match(Colon, RightSquare))
            Expr.None
        else
            expr()
        
        if (skip(RightSquare)) {
            return if (fixed) {
                Expr.FixedRange(loc, a, b, Expr.None)
            }
            else {
                Expr.RelativeRange(loc, a, b, Expr.None)
            }
        }
        
        mustSkip(Colon)
        
        val c = if (match(RightSquare))
            Expr.None
        else
            expr()
        
        mustSkip(RightSquare)
        
        return if (fixed) {
            Expr.FixedRange(loc, a, b, c)
        }
        else {
            Expr.RelativeRange(loc, a, b, c)
        }
    }
    
    private fun array(): Expr.Array {
        val token = peek()
        
        mustSkip(LeftBrace)
        
        val elements = mutableListOf<Expr>()
        
        do {
            val element = expr()
            
            if (element is Expr.Array) {
                invalidArrayElementError(element.loc)
            }
            
            elements += element
        }
        while (skip(Comma))
        
        mustSkip(RightBrace)
        
        return Expr.Array(token.loc, elements)
    }
    
    private fun dynamic(): Expr.DynamicLiteral {
        val token = peek()
        
        mustSkip(Dynamic)
        
        val name = Expr.DynamicLiteral.Name[token.value.toInt().toChar()]!!
        
        return Expr.DynamicLiteral(token.loc, name)
    }
}
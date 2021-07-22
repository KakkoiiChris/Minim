package minim.parser

import minim.lexer.Token
import minim.util.invalidArrayElementError
import minim.util.invalidTerminalError
import minim.util.invalidTypeError

class Parser(private val tokens: List<Token>) {
    private var pos = 0
    
    fun parse(): List<Stmt> {
        val stmts = mutableListOf<Stmt>()
        
        while (!skip(Token.Type.EOF)) {
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
            match(Token.Type.NUM) -> num()
            
            match(Token.Type.TXT) -> text()
            
            match(Token.Type.LBL) -> label()
            
            match(Token.Type.SYS) -> system()
            
            match(Token.Type.EOS) -> Stmt.None(here())
            
            else                  -> expression()
        }
        
        mustSkip(Token.Type.EOS)
        
        return stmt
    }
    
    private fun num(): Stmt {
        val loc = here()
        
        mustSkip(Token.Type.NUM)
        
        return when {
            skip(Token.Type.LSS) -> Stmt.NumberOut(loc, expr())
            
            skip(Token.Type.GRT) -> Stmt.NumberIn(loc, expr())
            
            else                 -> TODO()
        }
    }
    
    private fun text(): Stmt {
        val loc = here()
        
        mustSkip(Token.Type.TXT)
        
        return when {
            skip(Token.Type.LSS) -> Stmt.TextOut(loc, expr())
            
            skip(Token.Type.GRT) -> Stmt.TextIn(loc, expr())
            
            else                 -> TODO()
        }
    }
    
    private fun label(): Stmt {
        val loc = here()
        
        mustSkip(Token.Type.LBL)
        
        return when {
            skip(Token.Type.LSS) -> Stmt.Goto(loc, expr())
            
            skip(Token.Type.GRT) -> Stmt.Label(loc, expr())
            
            skip(Token.Type.XOR) -> Stmt.Jump(loc, expr())
            
            skip(Token.Type.ADD) -> Stmt.Gosub(loc, expr())
            
            skip(Token.Type.SUB) -> Stmt.Return(loc, expr())
            
            else                 -> TODO()
        }
    }
    
    private fun system(): Stmt {
        val loc = here()
        
        mustSkip(Token.Type.SYS)
        
        return when {
            skip(Token.Type.LSS) -> Stmt.SystemArg(loc, expr())
            
            skip(Token.Type.GRT) -> Stmt.SystemCall(loc, expr())
            
            else                 -> TODO()
        }
    }
    
    private fun expression(): Stmt {
        val loc = here()
        
        val expr = expr()
        
        if (expr is Expr.Binary) {
            return when (val left = expr.left) {
                is Expr.Variable      -> Stmt.VariableAssign(expr.loc, left, expr.right)
                
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
        
        return if (match(Token.Type.ASN)) {
            val op = peek()
            
            mustSkip(op.type)
            
            Expr.Binary(op.loc, op.type, expr, assign())
        }
        else {
            expr
        }
    }
    
    private fun conditional(): Expr {
        var node = or()
        
        if (match(Token.Type.TRN)) {
            val op = peek()
            
            mustSkip(op.type)
            
            val yes = conditional()
            
            mustSkip(Token.Type.RNG)
            
            val no = conditional()
            
            node = Expr.Ternary(op.loc, node, yes, no)
        }
        
        return node
    }
    
    private fun or(): Expr {
        var node = xor()
        
        while (match(Token.Type.ORR)) {
            val op = peek()
            
            mustSkip(op.type)
            
            node = Expr.Binary(op.loc, op.type, node, xor())
        }
        
        return node
    }
    
    private fun xor(): Expr {
        var node = and()
        
        while (match(Token.Type.XOR)) {
            val op = peek()
            
            mustSkip(op.type)
            
            node = Expr.Binary(op.loc, op.type, node, and())
        }
        
        return node
    }
    
    private fun and(): Expr {
        var node = equality()
        
        while (match(Token.Type.AND)) {
            val op = peek()
            
            mustSkip(op.type)
            
            node = Expr.Binary(op.loc, op.type, node, equality())
        }
        
        return node
    }
    
    private fun equality(): Expr {
        var node = relational()
        
        while (match(Token.Type.EQU, Token.Type.NEQ)) {
            val op = peek()
            
            mustSkip(op.type)
            
            node = Expr.Binary(op.loc, op.type, node, relational())
        }
        
        return node
    }
    
    private fun relational(): Expr {
        var node = shift()
        
        while (match(Token.Type.LSS, Token.Type.LEQ, Token.Type.GRT, Token.Type.GEQ)) {
            val op = peek()
            
            mustSkip(op.type)
            
            node = Expr.Binary(op.loc, op.type, node, shift())
        }
        
        return node
    }
    
    private fun shift(): Expr {
        var node = additive()
        
        while (match(Token.Type.SHL, Token.Type.SHR)) {
            val op = peek()
            
            mustSkip(op.type)
            
            node = Expr.Binary(op.loc, op.type, node, additive())
        }
        
        return node
    }
    
    private fun additive(): Expr {
        var node = multiplicative()
        
        while (match(Token.Type.ADD, Token.Type.SUB)) {
            val op = peek()
            
            mustSkip(op.type)
            
            node = Expr.Binary(op.loc, op.type, node, multiplicative())
        }
        
        return node
    }
    
    private fun multiplicative(): Expr {
        var node = prefix()
        
        while (match(Token.Type.MUL, Token.Type.DIV, Token.Type.REM)) {
            val op = peek()
            
            mustSkip(op.type)
            
            node = Expr.Binary(op.loc, op.type, node, prefix())
        }
        
        return node
    }
    
    private fun prefix(): Expr {
        return if (match(Token.Type.SUB,
                Token.Type.NOT,
                Token.Type.INV,
                Token.Type.TRN,
                Token.Type.PRI,
                Token.Type.PRD)
        ) {
            val op = peek()
            
            skip(op.type)
            
            Expr.Unary(op.loc, op.type, prefix())
        }
        else {
            postfix()
        }
    }
    
    private fun postfix(): Expr {
        var expr = terminal()
        
        while (match(Token.Type.PRI, Token.Type.PRD)) {
            val op = peek()
            
            skip(op.type)
            
            expr = when (op.type) {
                Token.Type.PRI -> Expr.Unary(op.loc, Token.Type.POI, expr)
                
                Token.Type.PRD -> Expr.Unary(op.loc, Token.Type.POD, expr)
                
                else           -> TODO("INVALID POSTFIX OPERATOR")
            }
        }
        
        return expr
    }
    
    private fun terminal(): Expr {
        return when {
            match(Token.Type.VAL) -> value()
            
            match(Token.Type.LPR) -> nested()
            
            match(Token.Type.LSQ) -> access()
            
            match(Token.Type.LBC) -> array()
            
            else                  -> invalidTerminalError(peek().type, here())
        }
    }
    
    private fun value(): Expr.Number {
        val token = peek()
        
        mustSkip(Token.Type.VAL)
        
        return Expr.Number(token.loc, token.value)
    }
    
    private fun nested(): Expr {
        mustSkip(Token.Type.LPR)
        
        val expr = expr()
        
        mustSkip(Token.Type.RPR)
        
        return expr
    }
    
    private fun access(): Expr {
        val loc = here()
        
        mustSkip(Token.Type.LSQ)
        
        if (skip(Token.Type.RSQ)) {
            return Expr.FixedRange(loc, Expr.None, Expr.None, Expr.None)
        }
        
        val a = if (match(Token.Type.RNG, Token.Type.REL, Token.Type.RSQ))
            Expr.None
        else
            expr()
        
        if (skip(Token.Type.RSQ)) {
            return Expr.Variable(loc, a)
        }
        
        val fixed = skip(Token.Type.RNG)
        
        if (!fixed) {
            mustSkip(Token.Type.REL)
        }
        
        if (skip(Token.Type.RSQ)) {
            return if (fixed) {
                Expr.FixedRange(loc, a, Expr.None, Expr.None)
            }
            else {
                TODO("RELATIVE RANGE COUNT NO DEFAULT")
            }
        }
        
        val b = if (match(Token.Type.RNG, Token.Type.RSQ))
            Expr.None
        else
            expr()
        
        if (skip(Token.Type.RSQ)) {
            return if (fixed) {
                Expr.FixedRange(loc, a, b, Expr.None)
            }
            else {
                Expr.RelativeRange(loc, a, b, Expr.None)
            }
        }
        
        mustSkip(Token.Type.RNG)
        
        val c = if (match(Token.Type.RSQ))
            Expr.None
        else
            expr()
        
        mustSkip(Token.Type.RSQ)
        
        return if (fixed) {
            Expr.FixedRange(loc, a, b, c)
        }
        else {
            Expr.RelativeRange(loc, a, b, c)
        }
    }
    
    private fun array(): Expr.Array {
        val token = peek()
        
        mustSkip(Token.Type.LBC)
        
        val elements = mutableListOf<Expr>()
        
        do {
            val element = expr()
            
            if (element is Expr.Array) {
                invalidArrayElementError(element.loc)
            }
            
            elements += element
        }
        while (skip(Token.Type.SEP))
        
        skip(Token.Type.RBC)
        
        return Expr.Array(token.loc, elements)
    }
}
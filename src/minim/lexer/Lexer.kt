package minim.lexer

import minim.lexer.Token.Type.*
import minim.parser.Expr
import minim.util.Source
import minim.util.invalidCharError
import minim.util.invalidEscapeError
import minim.util.invalidNumberError
import kotlin.math.E
import kotlin.math.PI

@Suppress("ControlFlowWithEmptyBody")
class Lexer(private val source: Source) {
    companion object {
        private const val NUL = '\u0000'
        
        private val literals = mapOf(
            'T' to 1F,
            'F' to 0F,
            'N' to Float.NaN,
            'I' to Float.POSITIVE_INFINITY,
            'P' to PI.toFloat(),
            'E' to E.toFloat()
        )
    }
    
    private val code = source.text.replace("\r", "")
    
    private var pos = 0
    private var row = 1
    private var col = 1
    
    fun lex(): List<Token> {
        val tokens = mutableListOf<Token>()
        
        loop@ while (!isEOF()) {
            tokens.addAll(when {
                match { isWhitespace() } -> {
                    skipWhitespace()
                    
                    continue@loop
                }
                
                match(';')               -> {
                    skipComment()
                    
                    continue@loop
                }
                
                match { isDigit() }      -> listOf(number())
                
                match { isLetter() }     -> listOf(word())
                
                match('\'')              -> listOf(char())
                
                match('"')               -> string()
                
                else                     -> listOf(operator())
            })
        }
        
        tokens += Token(here(), EndOfFile)
        
        return tokens
    }
    
    private fun peek(offset: Int = 0) =
        if (pos + offset < code.length)
            code[pos + offset]
        else
            NUL
    
    private fun match(char: Char) =
        peek() == char
    
    private fun match(predicate: Char.() -> Boolean) =
        predicate(peek())
    
    private fun matchNext(predicate: Char.() -> Boolean) =
        predicate(peek(1))
    
    private fun isEOF() =
        match('\u0000')
    
    private fun step(count: Int = 1) {
        repeat(count) {
            if (match('\n')) {
                row++
                col = 1
            }
            else {
                col++
            }
            
            pos++
        }
    }
    
    private fun skip(char: Char) =
        if (match(char)) {
            step()
            true
        }
        else {
            false
        }
    
    private fun skip(predicate: Char.() -> Boolean) =
        if (predicate(peek())) {
            step()
            true
        }
        else {
            false
        }
    
    private fun here() =
        Location(source.name, row, col)
    
    private fun StringBuilder.take(char: Char = peek()) {
        append(char)
        
        step()
    }
    
    private fun skipWhitespace() {
        while (skip { isWhitespace() });
    }
    
    private fun skipComment() {
        do {
            skip('\\')
            
            step()
        }
        while (!(isEOF() || skip('\n')))
    }
    
    private fun number(): Token {
        val loc = here()
        
        val result = buildString {
            do {
                take()
            }
            while (match { isDigit() })
            
            if (match('.') && matchNext { isDigit() }) {
                do {
                    take()
                }
                while (match { isDigit() })
            }
            
            if (match { this in "Ee" }) {
                take()
                
                do {
                    take()
                }
                while (match { isDigit() })
            }
        }
        
        return Token(loc, Value, result.toFloatOrNull() ?: invalidNumberError(result, loc))
    }
    
    private fun word(): Token {
        val loc = here()
        
        val char = peek()
        
        step()
        
        if (char in Expr.DynamicLiteral.Name) {
            return Token(loc, Dynamic, char.code.toFloat())
        }
        
        return when (char) {
            'i'  -> Token(loc, SmallI)
            
            'f'  -> Token(loc, SmallF)
            
            's'  -> Token(loc, SmallS)
            
            'M'  -> Token(loc, BigM)
            
            else -> Token(loc, Value, literals[char] ?: invalidCharError(char, loc))
        }
    }
    
    private fun unicode(size: Int) =
        buildString {
            repeat(size) {
                take()
            }
        }.toInt(16).toChar()
    
    private fun char(): Token {
        val loc = here()
        
        step()
        
        val char = if (skip('\\')) {
            when {
                skip('\\') -> '\\'
                
                skip('\'') -> '\''
                
                skip('0')  -> NUL
                
                skip('a')  -> '\u0007'
                
                skip('b')  -> '\b'
                
                skip('f')  -> '\u000c'
                
                skip('n')  -> '\n'
                
                skip('r')  -> '\r'
                
                skip('t')  -> '\t'
                
                skip('u')  -> unicode(4)
                
                skip('v')  -> '\u000B'
                
                skip('x')  -> unicode(2)
                
                else       -> invalidEscapeError(peek(), loc)
            }
        }
        else {
            val s = peek()
            
            step()
            
            s
        }
        
        step()
        
        return Token(loc, Value, char.code.toFloat())
    }
    
    private fun string(): List<Token> {
        val loc = here()
        
        step()
        
        val result = buildString {
            while (!skip('"')) {
                if (skip('\\')) {
                    append(when {
                        skip('\\') -> '\\'
                        
                        skip('"')  -> '"'
                        
                        skip('0')  -> NUL
                        
                        skip('a')  -> '\u0007'
                        
                        skip('b')  -> '\b'
                        
                        skip('f')  -> '\u000c'
                        
                        skip('n')  -> '\n'
                        
                        skip('r')  -> '\r'
                        
                        skip('t')  -> '\t'
                        
                        skip('u')  -> unicode(4)
                        
                        skip('v')  -> '\u000B'
                        
                        skip('x')  -> unicode(2)
                        
                        else       -> invalidEscapeError(peek(), loc)
                    })
                }
                else {
                    take()
                }
            }
        }
        
        val tokens = mutableListOf<Token>()
        
        tokens += Token(loc, LeftBrace)
        
        for ((i, c) in result.withIndex()) {
            if (i > 0) {
                tokens += Token(loc, Comma)
            }
            
            tokens += Token(loc, Value, c.code.toFloat())
        }
        
        tokens += Token(loc, Comma)
        tokens += Token(loc, Value, 0F)
        tokens += Token(loc, RightBrace)
        
        return tokens
    }
    
    private fun operator(): Token {
        val loc = here()
        
        val op = when {
            skip('+')  -> when {
                skip('+') -> DoublePlus
                
                skip('=') -> PlusEqual
                
                else      -> Plus
            }
            
            skip('-')  -> when {
                skip('-') -> DoubleMinus
                
                skip('=') -> MinusEqual
                
                else      -> Minus
            }
            
            skip('*')  -> when {
                skip('=') -> StarEqual
                
                else      -> Star
            }
            
            skip('/')  -> when {
                skip('=') -> SlashEqual
                
                else      -> Slash
            }
            
            skip('%')  -> when {
                skip('=') -> PercentEqual
                
                else      -> Percent
            }
            
            skip('(')  -> LeftParen
            
            skip(')')  -> RightParen
            
            skip('[')  -> LeftSquare
            
            skip(']')  -> RightSquare
            
            skip('{')  -> LeftBrace
            
            skip('}')  -> RightBrace
            
            skip('?')  -> when {
                skip('?') -> DoubleQuestion
                
                else      -> Question
            }
            
            skip(':')  -> Colon
            
            skip('=')  -> when {
                skip('=') -> DoubleEqual
                
                else      -> EqualSign
            }
            
            skip('<')  -> when {
                skip('<') -> when {
                    skip('=') -> DoubleLessEqual
                    
                    else      -> DoubleLess
                }
                
                skip('>') -> LessGreater
                
                skip('=') -> LessEqualSign
                
                else      -> LessSign
            }
            
            skip('>')  -> when {
                skip('>') -> when {
                    skip('>') -> when {
                        skip('=') -> TripleGreaterEqual
                        
                        else      -> TripleGreater
                    }
                    
                    skip('=') -> DoubleGreaterEqual
                    
                    else      -> DoubleGreater
                }
                
                skip('=') -> GreaterEqualSign
                
                else      -> GreaterSign
            }
            
            
            skip('&')  -> when {
                skip('&') -> when {
                    skip('=') -> DoubleAmpersandEqual
                    
                    else      -> DoubleAmpersand
                }
                
                skip('=') -> AndEqual
                
                else      -> Ampersand
            }
            
            skip('|')  -> when {
                skip('|') -> when {
                    skip('=') -> DoublePipeEqual
                    
                    else      -> DoublePipe
                }
                
                skip('=') -> PipeEqual
                
                else      -> Pipe
            }
            
            skip('^')  -> when {
                skip('=') -> CaretEqual
                
                else      -> Caret
            }
            
            skip('!')  -> when {
                skip('!') -> DoubleExclamation
                
                else      -> Exclamation
            }
            
            skip('~')  -> when {
                skip('~') -> DoubleTilde
                
                else      -> Tilde
            }
            
            skip('@')  -> At
            
            skip('#')  -> Number
            
            skip('$')  -> Dollar
            
            skip('_')  -> Underscore
            
            skip('\\') -> Backslash
            
            skip(',')  -> Comma
            
            skip('.')  -> Dot
            
            else       -> invalidCharError(peek(), here())
        }
        
        return Token(loc, op)
    }
}
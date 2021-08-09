package minim.lexer

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
        
        tokens += Token(here(), Token.Type.EOF)
        
        return tokens
    }
    
    private fun peek(offset: Int = 0) =
        if (pos + offset < code.length)
            code[pos + offset]
        else
            NUL
    
    private fun look(length: Int = 1) =
        buildString {
            repeat(length) { o ->
                append(peek(o))
            }
        }
    
    private fun match(char: Char) =
        peek() == char
    
    private fun match(predicate: Char.() -> Boolean) =
        predicate(peek())
    
    private fun match(string: String) =
        look(string.length) == string
    
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
    
    private fun skip(string: String) =
        if (look(string.length) == string) {
            step(string.length)
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
        
        return Token(loc, Token.Type.VAL, result.toFloatOrNull() ?: invalidNumberError(result, loc))
    }
    
    private fun word(): Token {
        val loc = here()
        
        val char = peek()
        
        step()
        
        return when (char) {
            'i'                -> Token(loc, Token.Type.INT)
            
            'f'                -> Token(loc, Token.Type.FLT)
            
            's'                -> Token(loc, Token.Type.STR)
            
            'M'                -> Token(loc, Token.Type.MEM)
            
            'A', 'C', 'L', 'S' -> Token(loc, Token.Type.DYN, char.code.toFloat())
            
            else               -> Token(loc, Token.Type.VAL, literals[char]!!)
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
        
        return Token(loc, Token.Type.VAL, char.code.toFloat())
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
        
        tokens += Token(loc, Token.Type.LBC)
        
        for ((i, c) in result.withIndex()) {
            if (i > 0) {
                tokens += Token(loc, Token.Type.SEP)
            }
            
            tokens += Token(loc, Token.Type.VAL, c.code.toFloat())
        }
        
        tokens += Token(loc, Token.Type.SEP)
        tokens += Token(loc, Token.Type.VAL, 0F)
        tokens += Token(loc, Token.Type.RBC)
        
        return tokens
    }
    
    private fun operator(): Token {
        val loc = here()
        
        val op = when {
            skip('+')  -> when {
                skip('+') -> Token.Type.PRI
                
                else      -> Token.Type.ADD
            }
            
            skip('-')  -> when {
                skip('-') -> Token.Type.PRD
                
                else      -> Token.Type.SUB
            }
            
            skip('*')  -> Token.Type.MUL
            
            skip('/')  -> Token.Type.DIV
            
            skip('%')  -> Token.Type.REM
            
            skip('(')  -> Token.Type.LPR
            
            skip(')')  -> Token.Type.RPR
            
            skip('[')  -> Token.Type.LSQ
            
            skip(']')  -> Token.Type.RSQ
            
            skip('{')  -> Token.Type.LBC
            
            skip('}')  -> Token.Type.RBC
            
            skip('?')  -> Token.Type.TRN
            
            skip(':')  -> Token.Type.RNG
            
            skip('=')  -> when {
                skip('=') -> Token.Type.EQU
                
                else      -> Token.Type.ASN
            }
            
            skip('<')  -> when {
                skip('<') -> Token.Type.SHL
                
                skip('>') -> Token.Type.NEQ
                
                skip('=') -> Token.Type.LEQ
                
                else      -> Token.Type.LSS
            }
            
            skip('>')  -> when {
                skip('>') -> when {
                    skip('>') -> Token.Type.USR
                    
                    else      -> Token.Type.SHR
                }
                
                skip('=') -> Token.Type.GEQ
                
                else      -> Token.Type.GRT
            }
            
            
            skip('&')  -> when {
                skip('&') -> Token.Type.AND
                
                else      -> Token.Type.BND
            }
            
            skip('|')  -> when {
                skip('|') -> Token.Type.ORR
                
                else      -> Token.Type.BOR
            }
            
            skip('^')  -> Token.Type.XOR
            
            skip('!')  -> Token.Type.NOT
            
            skip('~')  -> Token.Type.INV
            
            skip('@')  -> Token.Type.REL
            
            skip('#')  -> Token.Type.NUM
            
            skip('$')  -> Token.Type.TXT
            
            skip('_')  -> Token.Type.LBL
            
            skip('\\') -> Token.Type.SYS
            
            skip(',')  -> Token.Type.SEP
            
            skip('.')  -> Token.Type.EOS
            
            else       -> invalidCharError(peek(), here())
        }
        
        return Token(loc, op)
    }
}
package minim.lexer

import minim.lexer.Token.Type.*
import minim.parser.Expr
import minim.util.Source
import minim.util.invalidCharError
import minim.util.invalidEscapeError
import minim.util.invalidNumberError
import kotlin.math.E
import kotlin.math.PI

/**
 * The first stage of the interpreter; a scanner which converts source code into separate [tokens][Token].
 *
 * @param source the source to convert
 */
class Lexer(private val source: Source) {
    companion object {
        /**
         * Shorthand for the end-of-file character.
         */
        private const val NUL = '\u0000'
    
        /**
         * Map of all valid static numeric literals.
         */
        private val literals = mapOf(
            'T' to 1F,
            'F' to 0F,
            'N' to Float.NaN,
            'I' to Float.POSITIVE_INFINITY,
            'P' to PI.toFloat(),
            'E' to E.toFloat()
        )
    }
    
    /**
     * The source code for the program.
     */
    private val code = source.text.replace("\r", "")
    
    /**
     * The current position within the source code.
     */
    private var pos = 0
    
    /**
     * The current vertical position within the source code.
     */
    private var row = 1
    
    /**
     * The current horizontal position within the source code.
     */
    private var col = 1
    
    /**
     * Converts the given [source's][Source] [text content][Source.text] into a list of [tokens][Token].
     *
     * @return the source code, tokenized
     */
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
                
                match { isLetter() }     -> listOf(letter())
                
                match('\'')              -> listOf(char())
                
                match('"')               -> string()
                
                else                     -> listOf(symbol())
            })
        }
        
        tokens += Token(here(), EndOfFile)
        
        return tokens
    }
    
    /**
     * Gets the character a given [offset] from the current [position][pos]; usually the current character being lexed.
     *
     * @param offset the distance from the current position to look at
     *
     * @return the character at [pos] + [offset]
     */
    private fun peek(offset: Int = 0) =
        if (pos + offset < code.length)
            code[pos + offset]
        else
            NUL
    
    /**
     * Checks if the current character being lexed matches the given [character][char].
     *
     * @param char the character to match
     *
     * @return `true` if [char] occurs at the current position, or `false` otherwise
     */
    private fun match(char: Char) =
        peek() == char
    
    /**
     * Checks if the current character being lexed matches the given [predicate].
     *
     * @param predicate the condition of the character to match
     *
     * @return `true` if the character at the current position matches the predicate, or `false` otherwise
     */
    private fun match(predicate: Char.() -> Boolean) =
        predicate(peek())
    
    /**
     * Checks if the next character to be lexed matches the given [predicate].
     *
     * @param predicate the condition of the character to match
     *
     * @return `true` if the character at the next position matches the predicate, or `false` otherwise
     */
    private fun matchNext(predicate: Char.() -> Boolean) =
        predicate(peek(1))
    
    /**
     * Checks if the end of the source code has been reached.
     *
     * @return `true` if [peek] returns [NUL], or `false` otherwise
     */
    private fun isEOF() =
        match('\u0000')
    
    /**
     * Advances the lexer [pos]ition by the given step [count], and updates the [row] and [col] based on whether a newline was encountered.
     *
     * @param count the amount of times to advance the [pos]
     */
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
    
    /**
     * Checks if the current character being lexed matches the given [character][char], and [step]s if it does.
     *
     * @param char the character to skip
     *
     * @return `true` if [char] occurs at the current position, or `false` otherwise
     */
    private fun skip(char: Char) =
        if (match(char)) {
            step()
            true
        }
        else {
            false
        }
    
    /**
     * Checks if the current character being lexed matches the given [predicate], and [step]s if it does.
     *
     * @param predicate the condition of the character to skip
     *
     * @return `true` if the character at the current position matches the predicate, or `false` otherwise
     */
    private fun skip(predicate: Char.() -> Boolean) =
        if (predicate(peek())) {
            step()
            true
        }
        else {
            false
        }
    
    /**
     * Gets the current [Location] of the lexer.
     *
     * @return an instance of [Location] with the current file name, [row], and [col]
     */
    private fun here() =
        Location(source.name, row, col)
    
    /**
     * Helper extension function to add the current character to a [StringBuilder] and advance the lexer.
     *
     * @receiver a [StringBuilder] that is accumulating the contents of a multi-character [Token] ([number] or [string])
     */
    private fun StringBuilder.take() {
        append(peek())
        
        step()
    }
    
    /**
     * Advances the lexer while the current character is a whitespace character.
     */
    @Suppress("ControlFlowWithEmptyBody")
    private fun skipWhitespace() {
        while (skip { isWhitespace() });
    }
    
    /**
     * Advances the lexer until a newline not preceded by a backslash is encountered.
     */
    private fun skipComment() {
        skip(';')
        
        do {
            skip('\\')
            
            step()
        }
        while (!(isEOF() || skip('\n')))
    }
    
    /**
     * Lexes a single numeric literal.
     *
     * @return a [Value] [Token] with the numeric value
     */
    private fun number(): Token {
        val loc = here()
        
        val value = when {
            match('0') && matchNext { this in "Bb" } -> {
                val result = buildString {
                    take()
                    take()
                    
                    do {
                        take()
                    }
                    while (match { this in "01_" })
                }
                
                (result.substring(2).replace("_", "").toIntOrNull(2) ?: invalidNumberError(result, loc)).toFloat()
            }
            
            match('0') && matchNext { this in "Xx" } -> {
                val result = buildString {
                    take()
                    take()
                    
                    do {
                        take()
                    }
                    while (match { isDigit() || this in "AaBbCcDdEeFf_" })
                }
                
                (result.substring(2).replace("_", "").toIntOrNull(16) ?: invalidNumberError(result, loc)).toFloat()
            }
            
            else                                     -> {
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
                
                result.toFloatOrNull() ?: invalidNumberError(result, loc)
            }
        }
        
        return Token(loc, Value, value)
    }
    
    /**
     * Lexes a single alphabetic literal.
     *
     * @return a [Token] with the appropriate [Type][Token.Type]
     */
    private fun letter(): Token {
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
    
    /**
     * Gets a sequence of characters of length [size] and converts it from a hexadecimal number to it's associated unicode character.
     *
     * @param size the length of the unicode value to convert
     *
     * @return the [Char] associated with the value
     */
    private fun unicode(size: Int) =
        buildString {
            repeat(size) {
                take()
            }
        }.toInt(16).toChar()
    
    /**
     * Lexes a single character literal.
     *
     * @return a [Value] [Token] with the numeric value associated with the character
     */
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
    
    /**
     * Lexes a single string literal.
     *
     * @return a list of [Value] tokens based on the character values, including a null terminator, separated by [Comma] tokens, and surrounded by a [LeftBrace] and [RightBrace] token
     */
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
    
    /**
     * Lexes a single statement header, operator, or delimiter.
     *
     * @return a [Token] with the appropriate [Type][Token.Type]
     */
    private fun symbol(): Token {
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
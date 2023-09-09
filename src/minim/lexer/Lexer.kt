package minim.lexer

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
class Lexer(private val source: Source) : Iterator<Token> {
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

    private val stringTokens = ArrayDeque<Token>()

    override fun hasNext() =
        pos <= source.text.length

    /**
     * Converts the given [source's][Source] [text content][Source.text] into a list of [tokens][Token].
     *
     * @return the source code, tokenized
     */
    override fun next(): Token {
        while (!isEOF()) {
            return when {
                stringTokens.isNotEmpty() -> stringTokens.removeFirst()

                match { isWhitespace() }  -> {
                    skipWhitespace()

                    continue
                }

                match(';')                -> {
                    skipComment()

                    continue
                }

                match('"')                -> {
                    splitString()

                    continue
                }

                match { isDigit() }       -> number()

                match { isLetter() }      -> letter()

                match('\'')               -> char()

                else                      -> symbol()
            }
        }

        return Token(here(), Symbol.END_OF_FILE)
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
     * @receiver a [StringBuilder] that is accumulating the contents of a multi-character [Token]
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
     * Lexes a single string literal.
     *
     * @return a list of [Value] tokens based on the character values, including a null terminator, separated by [comma][Symbol.COMMA] tokens, and surrounded by a [left brace][Symbol.LEFT_BRACE] and [right brace][Symbol.RIGHT_BRACE] token
     */
    private fun splitString() {
        stringTokens.addLast(Token(here(), Symbol.LEFT_BRACE))

        skip('"')

        while (!match('"')) {
            val location = here()

            val char = if (skip('\\')) {
                when {
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

                    else       -> invalidEscapeError(peek(), location)
                }
            }
            else {
                val c = peek()

                skip(c)

                c
            }

            stringTokens.addLast(Token(location, Value(char.code.toFloat())))
            stringTokens.addLast(Token(location, Symbol.COMMA))
        }

        stringTokens.addLast(Token(here(), Value(0F)))
        stringTokens.addLast(Token(here(), Symbol.RIGHT_BRACE))

        skip('"')
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

        return Token(loc, Value(value))
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
            return Token(loc, Dynamic(char))
        }

        return when (char) {
            'i'  -> Token(loc, Symbol.SMALL_I)

            'f'  -> Token(loc, Symbol.SMALL_F)

            's'  -> Token(loc, Symbol.SMALL_S)

            'M'  -> Token(loc, Symbol.BIG_M)

            else -> Token(loc, Value(literals[char] ?: invalidCharError(char, loc)))
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

        return Token(loc, Value(char.code.toFloat()))
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
                skip('+') -> Symbol.DOUBLE_PLUS

                skip('=') -> Symbol.PLUS_EQUAL

                else      -> Symbol.PLUS
            }

            skip('-')  -> when {
                skip('-') -> Symbol.DOUBLE_DASH

                skip('=') -> Symbol.DASH_EQUAL

                else      -> Symbol.DASH
            }

            skip('*')  -> when {
                skip('=') -> Symbol.STAR_EQUAL

                else      -> Symbol.STAR
            }

            skip('/')  -> when {
                skip('=') -> Symbol.SLASH_EQUAL

                else      -> Symbol.SLASH
            }

            skip('%')  -> when {
                skip('=') -> Symbol.PERCENT_EQUAL

                else      -> Symbol.PERCENT
            }

            skip('(')  -> Symbol.LEFT_PAREN

            skip(')')  -> Symbol.RIGHT_PAREN

            skip('[')  -> Symbol.LEFT_SQUARE

            skip(']')  -> Symbol.RIGHT_SQUARE

            skip('{')  -> Symbol.LEFT_BRACE

            skip('}')  -> Symbol.RIGHT_BRACE

            skip('?')  -> when {
                skip('?') -> Symbol.DOUBLE_QUESTION

                else      -> Symbol.QUESTION
            }

            skip(':')  -> Symbol.COLON

            skip('=')  -> when {
                skip('=') -> Symbol.DOUBLE_EQUAL

                else      -> Symbol.EQUAL
            }

            skip('<')  -> when {
                skip('<') -> when {
                    skip('=') -> Symbol.DOUBLE_LESS_EQUAL

                    else      -> Symbol.DOUBLE_LESS
                }

                skip('>') -> Symbol.LESS_GREATER

                skip('=') -> Symbol.LESS_EQUAL

                else      -> Symbol.LESS
            }

            skip('>')  -> when {
                skip('>') -> when {
                    skip('>') -> when {
                        skip('=') -> Symbol.TRIPLE_GREATER_EQUAL

                        else      -> Symbol.TRIPLE_GREATER
                    }

                    skip('=') -> Symbol.DOUBLE_GREATER_EQUAL

                    else      -> Symbol.DOUBLE_GREATER
                }

                skip('=') -> Symbol.GREATER_EQUAL

                else      -> Symbol.GREATER
            }


            skip('&')  -> when {
                skip('&') -> when {
                    skip('=') -> Symbol.DOUBLE_AMPERSAND_EQUAL

                    else      -> Symbol.DOUBLE_AMPERSAND
                }

                skip('=') -> Symbol.AMPERSAND_EQUAL

                else      -> Symbol.AMPERSAND
            }

            skip('|')  -> when {
                skip('|') -> when {
                    skip('=') -> Symbol.DOUBLE_PIPE_EQUAL

                    else      -> Symbol.DOUBLE_PIPE
                }

                skip('=') -> Symbol.PIPE_EQUAL

                else      -> Symbol.PIPE
            }

            skip('^')  -> when {
                skip('=') -> Symbol.CARET_EQUAL

                else      -> Symbol.CARET
            }

            skip('!')  -> when {
                skip('!') -> Symbol.DOUBLE_BANG

                else      -> Symbol.BANG
            }

            skip('~')  -> when {
                skip('~') -> Symbol.DOUBLE_TILDE

                else      -> Symbol.TILDE
            }

            skip('@')  -> Symbol.AT

            skip('#')  -> Symbol.POUND

            skip('$')  -> Symbol.DOLLAR

            skip('_')  -> Symbol.UNDERSCORE

            skip('\\') -> Symbol.BACKSLASH

            skip(',')  -> Symbol.COMMA

            skip('.')  -> Symbol.DOT

            else       -> invalidCharError(peek(), here())
        }

        return Token(loc, op)
    }
}
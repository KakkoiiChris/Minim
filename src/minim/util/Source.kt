package minim.util

import minim.lexer.Lexer
import minim.parser.Parser
import minim.parser.Stmt
import minim.runtime.Config
import minim.runtime.Runtime

/**
 * An origin-agnostic representation of a code source.
 *
 * @param name the name associated with this source
 * @param text the code content associated with this source
 */
data class Source(val name: String, val text: String) {
    /**
     * Sequentially lexes and parses this source's [text].
     *
     * @return The [statements][Stmt] parsed from this source
     */
    fun compile(): List<Stmt> {
        val lexer = Lexer(this)
        
        val tokens = lexer.lex()
        
        val parser = Parser(tokens)
        
        return parser.parse()
    }
}
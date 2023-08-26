package minim.util

import minim.lexer.Lexer
import minim.parser.Parser
import minim.parser.Program
import minim.parser.Stmt

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
    fun create(): Program {
        val lexer = Lexer(this)
        
        val parser = Parser(lexer)
        
        val stmts = parser.parse()
        
        return Program(stmts)
    }
}
package minim.util

import minim.lexer.Lexer
import minim.parser.Parser
import minim.runtime.Config
import minim.runtime.Runtime

data class Source(val name: String, val text: String) {
    companion object {
        fun readLocalFile(name: String, path: String): Source {
            val content = Source::class.java
                .getResourceAsStream(path)
                ?.bufferedReader()
                ?.readText()
                ?: ""
            
            return Source(name, content)
        }
    }
    
    fun compile(config: Config): Runtime {
        val lexer = Lexer(this)
        
        val tokens = lexer.lex()
        
        val parser = Parser(tokens)
        
        val stmts = parser.parse()
        
        return Runtime(config, stmts)
    }
}
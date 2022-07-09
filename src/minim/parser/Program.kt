package minim.parser

class Program(private val stmts: Stmts) : List<Stmt> by stmts {
    companion object{
        val empty get() = Program(emptyList())
    }
}
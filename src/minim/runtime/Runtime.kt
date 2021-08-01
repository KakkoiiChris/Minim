package minim.runtime

import minim.lexer.Token
import minim.parser.Expr
import minim.parser.Stmt
import minim.util.*
import java.util.*
import kotlin.collections.ArrayDeque
import kotlin.math.min

class Runtime(val config: Config, private val stmts: List<Stmt>) : Expr.Visitor<Any>, Stmt.Visitor<Unit> {
    private val memory = MArray(config.size)
    
    private val startDefault = 0F
    private val endDefault = memory.lastIndex.toFloat()
    private val stepDefault = 1F
    
    private val labels = mutableMapOf<Float, Int>()
    
    private val callStack = Stack<Int>()
    
    private var pos = 0
    
    private val inputQueue = ArrayDeque<Float>()
    
    private val systemInputQueue = ArrayDeque<Float>()
    private val systemOutputQueue = ArrayDeque<Float>()
    
    fun run(): Any {
        while (pos < stmts.size) {
            visit(stmts[pos++])
        }
        
        return memory.first().value
    }
    
    override fun visitNoneExpr(expr: Expr.None) = Unit
    
    override fun visitUnaryExpr(expr: Expr.Unary): Any {
        return when (expr.op) {
            Token.Type.SUB -> when (val e = visit(expr.expr)) {
                is MNumber -> -e
                
                else       -> invalidUnaryOperandError(e, expr.op, expr.loc)
            }
            
            Token.Type.NOT -> when (val e = visit(expr.expr)) {
                is MNumber -> !e
                
                else       -> invalidUnaryOperandError(e, expr.op, expr.loc)
            }
            
            Token.Type.TRN -> when (val e = visit(expr.expr)) {
                is MNumber -> e.narrow()
                
                else       -> invalidUnaryOperandError(e, expr.op, expr.loc)
            }
            
            Token.Type.INV -> when (val e = visit(expr.expr)) {
                is MNumber -> e.inv()
                
                else       -> invalidUnaryOperandError(e, expr.op, expr.loc)
            }
            
            Token.Type.PRI -> when (val e = visit(expr.expr)) {
                is MNumber -> {
                    e.value++
                    
                    e
                }
                
                else       -> invalidUnaryOperandError(e, expr.op, expr.loc)
            }
            
            Token.Type.PRD -> when (val e = visit(expr.expr)) {
                is MNumber -> {
                    e.value--
                    
                    e
                }
                
                else       -> invalidUnaryOperandError(e, expr.op, expr.loc)
            }
            
            Token.Type.POI -> when (val e = visit(expr.expr)) {
                is MNumber -> {
                    val before = MNumber(e.value)
                    
                    e.value++
                    
                    before
                }
                
                else       -> invalidUnaryOperandError(e, expr.op, expr.loc)
            }
            
            Token.Type.POD -> when (val e = visit(expr.expr)) {
                is MNumber -> {
                    val before = MNumber(e.value)
                    
                    e.value--
                    
                    before
                }
                
                else       -> invalidUnaryOperandError(e, expr.op, expr.loc)
            }
            
            else           -> unexpectedError("BROKEN UNARY OPERATOR '${expr.op}'!")
        }
    }
    
    override fun visitBinaryExpr(expr: Expr.Binary): Any {
        return when (expr.op) {
            Token.Type.ADD -> when (val l = visit(expr.left)) {
                is MNumber -> when (val r = visit(expr.right)) {
                    is MNumber -> l + r
                    
                    else       -> invalidRightOperandError(r, expr.op, expr.loc)
                }
                
                else       -> invalidLeftOperandError(l, expr.op, expr.loc)
            }
            
            Token.Type.SUB -> when (val l = visit(expr.left)) {
                is MNumber -> when (val r = visit(expr.right)) {
                    is MNumber -> l - r
                    
                    else       -> invalidRightOperandError(r, expr.op, expr.loc)
                }
                
                else       -> invalidLeftOperandError(l, expr.op, expr.loc)
            }
            
            Token.Type.MUL -> when (val l = visit(expr.left)) {
                is MNumber -> when (val r = visit(expr.right)) {
                    is MNumber -> l * r
                    
                    else       -> invalidRightOperandError(r, expr.op, expr.loc)
                }
                
                else       -> invalidLeftOperandError(l, expr.op, expr.loc)
            }
            
            Token.Type.DIV -> when (val l = visit(expr.left)) {
                is MNumber -> when (val r = visit(expr.right)) {
                    is MNumber -> l / r
                    
                    else       -> invalidRightOperandError(r, expr.op, expr.loc)
                }
                
                else       -> invalidLeftOperandError(l, expr.op, expr.loc)
            }
            
            Token.Type.REM -> when (val l = visit(expr.left)) {
                is MNumber -> when (val r = visit(expr.right)) {
                    is MNumber -> l % r
                    
                    else       -> invalidRightOperandError(r, expr.op, expr.loc)
                }
                
                else       -> invalidLeftOperandError(l, expr.op, expr.loc)
            }
            
            Token.Type.LSS -> when (val l = visit(expr.left)) {
                is MNumber -> when (val r = visit(expr.right)) {
                    is MNumber -> l lss r
                    
                    else       -> invalidRightOperandError(r, expr.op, expr.loc)
                }
                
                else       -> invalidLeftOperandError(l, expr.op, expr.loc)
            }
            
            Token.Type.LEQ -> when (val l = visit(expr.left)) {
                is MNumber -> when (val r = visit(expr.right)) {
                    is MNumber -> l leq r
                    
                    else       -> invalidRightOperandError(r, expr.op, expr.loc)
                }
                
                else       -> invalidLeftOperandError(l, expr.op, expr.loc)
            }
            
            Token.Type.GRT -> when (val l = visit(expr.left)) {
                is MNumber -> when (val r = visit(expr.right)) {
                    is MNumber -> l grt r
                    
                    else       -> invalidRightOperandError(r, expr.op, expr.loc)
                }
                
                else       -> invalidLeftOperandError(l, expr.op, expr.loc)
            }
            
            Token.Type.GEQ -> when (val l = visit(expr.left)) {
                is MNumber -> when (val r = visit(expr.right)) {
                    is MNumber -> l geq r
                    
                    else       -> invalidRightOperandError(r, expr.op, expr.loc)
                }
                
                else       -> invalidLeftOperandError(l, expr.op, expr.loc)
            }
            
            Token.Type.EQU -> when (val l = visit(expr.left)) {
                is MNumber -> when (val r = visit(expr.right)) {
                    is MNumber -> l equ r
                    
                    else       -> invalidRightOperandError(r, expr.op, expr.loc)
                }
                
                else       -> invalidLeftOperandError(l, expr.op, expr.loc)
            }
            
            Token.Type.NEQ -> when (val l = visit(expr.left)) {
                is MNumber -> when (val r = visit(expr.right)) {
                    is MNumber -> l neq r
                    
                    else       -> invalidRightOperandError(r, expr.op, expr.loc)
                }
                
                else       -> invalidLeftOperandError(l, expr.op, expr.loc)
            }
            
            Token.Type.AND -> when (val l = visit(expr.left)) {
                is MNumber -> when (val r = visit(expr.right)) {
                    is MNumber -> MNumber((l.value.toBoolean() && r.value.toBoolean()).toFloat())
                    
                    else       -> invalidRightOperandError(r, expr.op, expr.loc)
                }
                
                else       -> invalidLeftOperandError(l, expr.op, expr.loc)
            }
            
            Token.Type.ORR -> when (val l = visit(expr.left)) {
                is MNumber -> when (val r = visit(expr.right)) {
                    is MNumber -> MNumber((l.value.toBoolean() || r.value.toBoolean()).toFloat())
                    
                    else       -> invalidRightOperandError(r, expr.op, expr.loc)
                }
                
                else       -> invalidLeftOperandError(l, expr.op, expr.loc)
            }
            
            Token.Type.XOR -> when (val l = visit(expr.left)) {
                is MNumber -> when (val r = visit(expr.right)) {
                    is MNumber -> l.value.toBoolean() xor r.value.toBoolean()
                    
                    else       -> invalidRightOperandError(r, expr.op, expr.loc)
                }
                
                else       -> invalidLeftOperandError(l, expr.op, expr.loc)
            }
            
            Token.Type.SHL -> when (val l = visit(expr.left)) {
                is MNumber -> when (val r = visit(expr.right)) {
                    is MNumber -> l neq r
                    
                    else       -> invalidRightOperandError(r, expr.op, expr.loc)
                }
                
                else       -> invalidLeftOperandError(l, expr.op, expr.loc)
            }
            
            Token.Type.SHR -> when (val l = visit(expr.left)) {
                is MNumber -> when (val r = visit(expr.right)) {
                    is MNumber -> l neq r
                    
                    else       -> invalidRightOperandError(r, expr.op, expr.loc)
                }
                
                else       -> invalidLeftOperandError(l, expr.op, expr.loc)
            }
            
            Token.Type.ASN -> when (val l = visit(expr.left)) {
                is MNumber -> when (val r = visit(expr.right)) {
                    is MNumber -> {
                        l.value = r.value
                        
                        l
                    }
                    
                    is MArray  -> {
                        l.value = r[0].value
                        
                        l
                    }
                    
                    else       -> invalidRightOperandError(r, expr.op, expr.loc)
                }
                
                is MArray  -> when (val r = visit(expr.right)) {
                    is MNumber -> l[0].value = r.value
                    
                    is MArray  -> {
                        val minSize = min(l.size, r.size)
                        
                        for (i in 0 until minSize) {
                            l[i].value = r[i].value
                        }
                    }
                    
                    else       -> invalidRightOperandError(r, expr.op, expr.loc)
                }
                
                else       -> invalidLeftOperandError(l, expr.op, expr.loc)
            }
            
            else           -> unexpectedError("BROKEN BINARY OPERATOR '${expr.op}'!")
        }
    }
    
    override fun visitTernaryExpr(expr: Expr.Ternary) =
        when (val test = visit(expr.test)) {
            is MNumber -> if (test.value.toBoolean())
                visit(expr.yes)
            else
                visit(expr.no)
            
            else       -> invalidTestExprError(expr.test.loc)
        }
    
    override fun visitNumberExpr(expr: Expr.Number) =
        MNumber(expr.value)
    
    override fun visitArrayExpr(expr: Expr.Array): Any {
        val bytes = mutableListOf<MNumber>()
        
        for (subExpr in expr.elements) {
            bytes.add(visit(subExpr) as MNumber)
        }
        
        return MArray(bytes)
    }
    
    override fun visitVariableExpr(expr: Expr.Variable): Any {
        val index = visit(expr.index) as? MNumber ?: invalidMemoryIndexError(expr.index.loc)
        
        return memory[index.value.toInt()]
    }
    
    override fun visitFixedRangeExpr(expr: Expr.FixedRange): Any {
        val list = mutableListOf<MNumber>()
        
        val start = (visit(expr.start) as? MNumber)?.value ?: startDefault
        val end = (visit(expr.end) as? MNumber)?.value ?: endDefault
        val step = (visit(expr.step) as? MNumber)?.value ?: stepDefault
        
        var i = start
        
        while (i <= end) {
            list.add(memory[i.toInt()])
            
            i += step
        }
        
        return MArray(list)
    }
    
    override fun visitRelativeRangeExpr(expr: Expr.RelativeRange): Any {
        val list = mutableListOf<MNumber>()
        
        val start = (visit(expr.start) as? MNumber)?.value ?: startDefault
        val count = (visit(expr.count) as? MNumber ?: invalidMemoryIndexError(expr.count.loc)).value
        val step = (visit(expr.step) as? MNumber)?.value ?: stepDefault
        
        var c = 0
        
        while (c < count) {
            val i = start + c * step
            
            list.add(memory[i.toInt()])
            
            c++
        }
        
        return MArray(list)
    }
    
    override fun visitNoneStmt(stmt: Stmt.None) = Unit
    
    override fun visitNumberInStmt(stmt: Stmt.NumberIn) {
        val number = visit(stmt.expr) as? MNumber ?: invalidStatementArgumentError(stmt.expr.loc)
        
        val input = readLine()?.takeIf { it.isNotEmpty() } ?: ""
        
        number.value = input.toFloatOrNull() ?: invalidNumericalInputError(input, stmt.loc)
    }
    
    override fun visitNumberOutStmt(stmt: Stmt.NumberOut) {
        val number = visit(stmt.expr) as? MNumber ?: invalidStatementArgumentError(stmt.expr.loc)
        
        print(number)
    }
    
    override fun visitTextInStmt(stmt: Stmt.TextIn) {
        if (inputQueue.isEmpty()) {
            val input = readLine()?.takeIf { it.isNotEmpty() } ?: return
            
            inputQueue.addAll(input.map { it.code.toFloat() } + 0F)
        }
        
        val number = visit(stmt.expr) as? MNumber ?: invalidStatementArgumentError(stmt.expr.loc)
        
        number.value = inputQueue.removeFirst()
    }
    
    override fun visitTextOutStmt(stmt: Stmt.TextOut) {
        val number = visit(stmt.expr) as? MNumber ?: invalidStatementArgumentError(stmt.expr.loc)
        
        print(number.toChar())
    }
    
    override fun visitLabelStmt(stmt: Stmt.Label) {
        val id = (visit(stmt.id) as? MNumber ?: invalidStatementArgumentError(stmt.id.loc)).value
        
        labels[id] = pos
    }
    
    override fun visitGotoStmt(stmt: Stmt.Goto) {
        val id = (visit(stmt.id) as? MNumber ?: invalidStatementArgumentError(stmt.id.loc)).value
        
        pos = labels[id] ?: findLabel(id) ?: undefinedLabelError(id, stmt.loc)
    }
    
    private fun findLabel(id: Float): Int? {
        for ((i, stmt) in stmts.withIndex()) {
            if (stmt is Stmt.Label) {
                val otherID = (visit(stmt.id) as? MNumber ?: invalidStatementArgumentError(stmt.id.loc)).value
                
                if (id == otherID) {
                    labels[id] = i
                    
                    return i
                }
            }
        }
        
        return null
    }
    
    override fun visitJumpStmt(stmt: Stmt.Jump) {
        val condition =
            (visit(stmt.condition) as? MNumber ?: invalidStatementArgumentError(stmt.condition.loc)).value.toBoolean()
        
        if (condition) {
            pos++
        }
    }
    
    override fun visitGosubStmt(stmt: Stmt.Gosub) {
        val id = (visit(stmt.id) as? MNumber ?: invalidStatementArgumentError(stmt.id.loc)).value
        
        callStack.push(pos)
        
        pos = labels[id] ?: findLabel(id) ?: undefinedLabelError(id, stmt.loc)
    }
    
    override fun visitReturnStmt(stmt: Stmt.Return) {
        //val expr = visit(stmt.expr) as? MNumber ?: TODO("GOTO")
        
        if (callStack.isNotEmpty()) {
            val last = callStack.pop()
            
            pos = last
        }
    }
    
    override fun visitSystemArgStmt(stmt: Stmt.SystemArg) {
        val expr = visit(stmt.expr) as? MNumber ?: invalidStatementArgumentError(stmt.expr.loc)
        
        systemInputQueue.add(expr.value)
    }
    
    override fun visitSystemCallStmt(stmt: Stmt.SystemCall) {
        if (systemInputQueue.isNotEmpty()) {
            val start = systemInputQueue.removeFirst().toInt()
            
            var end = start
            
            do {
                end++
            }
            while (memory[end].value != 0F)
            
            val commandName = memory[start until end].ascii
            
            val command = Library[commandName] ?: undefinedCommandError(commandName, stmt.loc)
            
            val args = mutableListOf<Float>()
            
            for (i in 0 until command.arity) {
                args.add(systemInputQueue.removeFirst())
            }
            
            val result = command(this, args.toFloatArray())
            
            systemOutputQueue.addAll(result.toList())
        }
        
        if (systemOutputQueue.isNotEmpty()) {
            val number = visit(stmt.expr) as? MNumber ?: invalidStatementArgumentError(stmt.expr.loc)
            
            number.value = systemOutputQueue.removeFirst()
        }
    }
    
    override fun visitVariableAssignStmt(stmt: Stmt.VariableAssign) {
        val index =
            (visit(stmt.variable.index) as? MNumber ?: invalidMemoryIndexError(stmt.variable.index.loc)).value.toInt()
        
        when (val expr = visit(stmt.expr)) {
            is MNumber -> memory[index].value = expr.value
            
            is MArray  -> memory[index].value = expr[0].value
        }
    }
    
    override fun visitFixedRangeAssignStmt(stmt: Stmt.FixedRangeAssign) {
        val start = when (val expr = visit(stmt.range.start)) {
            is MNumber -> expr.value
            
            is Unit    -> startDefault
            
            else       -> invalidRangeExprError("start", stmt.range.start.loc)
        }.toInt()
        
        val end = when (val expr = visit(stmt.range.end)) {
            is MNumber -> expr.value
            
            is Unit    -> endDefault
            
            else       -> invalidRangeExprError("end", stmt.range.end.loc)
        }.toInt()
        
        val step = when (val expr = visit(stmt.range.step)) {
            is MNumber -> expr.value
            
            is Unit    -> stepDefault
            
            else       -> invalidRangeExprError("step", stmt.range.step.loc)
        }.toInt()
        
        when (val expr = visit(stmt.expr)) {
            is MNumber -> {
                var memoryIndex = start
                
                while (memoryIndex <= end) {
                    memory[memoryIndex].value = expr.value
                    
                    memoryIndex += step
                }
            }
            
            is MArray  -> {
                var memoryIndex = start
                
                var arrayIndex = 0
                
                while (memoryIndex <= end && arrayIndex < expr.size) {
                    memory[memoryIndex].value = expr[arrayIndex].value
                    
                    memoryIndex += step
                    
                    arrayIndex++
                }
            }
        }
    }
    
    override fun visitRelativeRangeAssignStmt(stmt: Stmt.RelativeRangeAssign) {
        val start = when (val expr = visit(stmt.range.start)) {
            is MNumber -> expr.value
            
            is Unit    -> startDefault
            
            else       -> invalidRangeExprError("start", stmt.range.start.loc)
        }.toInt()
        
        val count = when (val expr = visit(stmt.range.count)) {
            is MNumber -> expr.value
            
            else       -> invalidRangeExprError("count", stmt.range.count.loc)
        }.toInt()
        
        val step = when (val expr = visit(stmt.range.step)) {
            is MNumber -> expr.value
            
            is Unit    -> stepDefault
            
            else       -> invalidRangeExprError("step", stmt.range.step.loc)
        }.toInt()
        
        when (val expr = visit(stmt.expr)) {
            is MNumber -> {
                var memoryIndex = start
                
                while (memoryIndex < start + count) {
                    memory[memoryIndex].value = expr.value
                    
                    memoryIndex += step
                }
            }
            
            is MArray  -> {
                var memoryIndex = start
                
                var arrayIndex = 0
                
                while (memoryIndex < start + count && arrayIndex < expr.size) {
                    memory[memoryIndex].value = expr[arrayIndex].value
                    
                    memoryIndex += step
                    
                    arrayIndex++
                }
            }
        }
    }
    
    override fun visitExpressionStmt(stmt: Stmt.Expression) {
        visit(stmt.expr)
    }
}
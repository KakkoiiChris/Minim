package minim.runtime

import minim.lexer.Token
import minim.parser.Expr
import minim.parser.Stmt
import minim.util.*
import java.math.BigDecimal
import java.util.*
import kotlin.collections.ArrayDeque
import kotlin.math.min

class Runtime(val config: Config, private val stmts: List<Stmt>) : Expr.Visitor<Any>, Stmt.Visitor<Unit> {
    private val memory = Stack<MArray>()
    
    private val labels = mutableMapOf<Float, Int>()
    
    private val callStack = Stack<Int>()
    
    private val counter = Ref(MNumber.Int())
    
    private val inputQueue = ArrayDeque<MNumber>()
    
    private val systemInputQueue = ArrayDeque<Float>()
    private val systemOutputQueue = ArrayDeque<Float>()
    
    private val memoryQueue = ArrayDeque<MNumber>()
    
    fun run(): Any {
        memory.push(MArray(config.size))
        
        while (counter.value.toInt() < stmts.size) {
            visit(stmts[counter.value.toInt()])
            
            counter.preIncrement()
        }
        
        memory.peek()[0..0xFF].printDebug()
        
        return memory.peek().first().value
    }
    
    override fun visitNoneExpr(expr: Expr.None) = Unit
    
    override fun visitUnaryExpr(expr: Expr.Unary): Any {
        return when (expr.op) {
            Token.Type.SUB -> when (val e = visit(expr.expr).fromRef()) {
                is MNumber -> -e
                
                else       -> invalidUnaryOperandError(e, expr.op, expr.loc)
            }
            
            Token.Type.NOT -> when (val e = visit(expr.expr).fromRef()) {
                is MNumber -> !e
                
                else       -> invalidUnaryOperandError(e, expr.op, expr.loc)
            }
            
            Token.Type.TRN -> when (val e = visit(expr.expr).fromRef()) {
                is MNumber -> e.narrow()
                
                else       -> invalidUnaryOperandError(e, expr.op, expr.loc)
            }
            
            Token.Type.INV -> when (val e = visit(expr.expr).fromRef()) {
                is MNumber -> e.inv()
                
                else       -> invalidUnaryOperandError(e, expr.op, expr.loc)
            }
            
            Token.Type.PRI -> when (val e = visit(expr.expr)) {
                is Ref -> e.preIncrement()
                
                else   -> invalidUnaryOperandError(e, expr.op, expr.loc)
            }
            
            Token.Type.PRD -> when (val e = visit(expr.expr)) {
                is Ref -> e.preDecrement()
                
                else   -> invalidUnaryOperandError(e, expr.op, expr.loc)
            }
            
            Token.Type.POI -> when (val e = visit(expr.expr)) {
                is Ref -> e.postIncrement()
                
                else   -> invalidUnaryOperandError(e, expr.op, expr.loc)
            }
            
            Token.Type.POD -> when (val e = visit(expr.expr)) {
                is Ref -> e.postDecrement()
                
                else   -> invalidUnaryOperandError(e, expr.op, expr.loc)
            }
            
            Token.Type.FLT -> when (val e = visit(expr.expr).fromRef()) {
                is MNumber -> MNumber.Float(e.toFloat())
                
                is MArray  -> for (ref in e) {
                    ref.value = MNumber.Float(ref.value.toFloat())
                }
                
                else       -> invalidUnaryOperandError(e, expr.op, expr.loc)
            }
            
            Token.Type.INT -> when (val e = visit(expr.expr).fromRef()) {
                is MNumber -> MNumber.Int(e.toInt())
                
                is MArray  -> for (ref in e) {
                    ref.value = MNumber.Int(ref.value.toInt())
                }
                
                else       -> invalidUnaryOperandError(e, expr.op, expr.loc)
            }
            
            Token.Type.STR -> when (val e = visit(expr.expr).fromRef()) {
                is MNumber -> e.toString().toMArray()
                
                else       -> invalidUnaryOperandError(e, expr.op, expr.loc)
            }
            
            else           -> unexpectedError("BROKEN UNARY OPERATOR '${expr.op}'!")
        }
    }
    
    override fun visitBinaryExpr(expr: Expr.Binary): Any {
        return when (expr.op) {
            Token.Type.ADD -> when (val l = visit(expr.left).fromRef()) {
                is MNumber -> when (val r = visit(expr.right).fromRef()) {
                    is MNumber -> l + r
                    
                    else       -> invalidRightOperandError(r, expr.op, expr.loc)
                }
                
                is MArray  -> when (val r = visit(expr.right).fromRef()) {
                    is MNumber -> MArray(l.toMutableList().let {
                        while (it.last().value.toInt() == 0) {
                            it.removeLast()
                        }
                        it
                    } + Ref(r))
                    
                    is MArray  -> MArray(l.toMutableList().let {
                        while (it.last().value.toInt() == 0) {
                            it.removeLast()
                        }
                        it
                    } + r.toMutableList())
                    
                    else       -> invalidRightOperandError(r, expr.op, expr.loc)
                }
                
                else       -> invalidLeftOperandError(l, expr.op, expr.loc)
            }
            
            Token.Type.SUB -> when (val l = visit(expr.left).fromRef()) {
                is MNumber -> when (val r = visit(expr.right).fromRef()) {
                    is MNumber -> l - r
                    
                    else       -> invalidRightOperandError(r, expr.op, expr.loc)
                }
                
                else       -> invalidLeftOperandError(l, expr.op, expr.loc)
            }
            
            Token.Type.MUL -> when (val l = visit(expr.left).fromRef()) {
                is MNumber -> when (val r = visit(expr.right).fromRef()) {
                    is MNumber -> l * r
                    
                    else       -> invalidRightOperandError(r, expr.op, expr.loc)
                }
                
                else       -> invalidLeftOperandError(l, expr.op, expr.loc)
            }
            
            Token.Type.DIV -> when (val l = visit(expr.left).fromRef()) {
                is MNumber -> when (val r = visit(expr.right).fromRef()) {
                    is MNumber -> l / r
                    
                    else       -> invalidRightOperandError(r, expr.op, expr.loc)
                }
                
                else       -> invalidLeftOperandError(l, expr.op, expr.loc)
            }
            
            Token.Type.REM -> when (val l = visit(expr.left).fromRef()) {
                is MNumber -> when (val r = visit(expr.right).fromRef()) {
                    is MNumber -> l % r
                    
                    else       -> invalidRightOperandError(r, expr.op, expr.loc)
                }
                
                is MArray  -> when (val r = visit(expr.right).fromRef()) {
                    is MNumber -> {
                        val index = l.indexOfFirst { it.value.toInt() == '%'.code }
                        
                        if (index >= 0) {
                            l[index].value = r
                        }
                        
                        l
                    }
                    
                    is MArray  -> {
                        for (i in r.indices) {
                            val index = l.indexOfFirst { it.value.toInt() == '%'.code }
                            
                            if (index >= 0) {
                                l[index].value = r[i].value
                            }
                        }
                        
                        l
                    }
                    
                    else       -> invalidRightOperandError(r, expr.op, expr.loc)
                }
                
                else       -> invalidLeftOperandError(l, expr.op, expr.loc)
            }
            
            Token.Type.LSS -> when (val l = visit(expr.left).fromRef()) {
                is MNumber -> when (val r = visit(expr.right).fromRef()) {
                    is MNumber -> l lss r
                    
                    else       -> invalidRightOperandError(r, expr.op, expr.loc)
                }
                
                else       -> invalidLeftOperandError(l, expr.op, expr.loc)
            }
            
            Token.Type.LEQ -> when (val l = visit(expr.left).fromRef()) {
                is MNumber -> when (val r = visit(expr.right).fromRef()) {
                    is MNumber -> l leq r
                    
                    else       -> invalidRightOperandError(r, expr.op, expr.loc)
                }
                
                else       -> invalidLeftOperandError(l, expr.op, expr.loc)
            }
            
            Token.Type.GRT -> when (val l = visit(expr.left).fromRef()) {
                is MNumber -> when (val r = visit(expr.right).fromRef()) {
                    is MNumber -> l grt r
                    
                    else       -> invalidRightOperandError(r, expr.op, expr.loc)
                }
                
                else       -> invalidLeftOperandError(l, expr.op, expr.loc)
            }
            
            Token.Type.GEQ -> when (val l = visit(expr.left).fromRef()) {
                is MNumber -> when (val r = visit(expr.right).fromRef()) {
                    is MNumber -> l geq r
                    
                    else       -> invalidRightOperandError(r, expr.op, expr.loc)
                }
                
                else       -> invalidLeftOperandError(l, expr.op, expr.loc)
            }
            
            Token.Type.EQU -> when (val l = visit(expr.left).fromRef()) {
                is MNumber -> when (val r = visit(expr.right).fromRef()) {
                    is MNumber -> l equ r
                    
                    else       -> invalidRightOperandError(r, expr.op, expr.loc)
                }
                
                else       -> invalidLeftOperandError(l, expr.op, expr.loc)
            }
            
            Token.Type.NEQ -> when (val l = visit(expr.left).fromRef()) {
                is MNumber -> when (val r = visit(expr.right).fromRef()) {
                    is MNumber -> l neq r
                    
                    else       -> invalidRightOperandError(r, expr.op, expr.loc)
                }
                
                else       -> invalidLeftOperandError(l, expr.op, expr.loc)
            }
            
            Token.Type.AND -> when (val l = visit(expr.left).fromRef()) {
                is MNumber -> when (val r = visit(expr.right).fromRef()) {
                    is MNumber -> l and r
                    
                    else       -> invalidRightOperandError(r, expr.op, expr.loc)
                }
                
                is MArray  -> when (val r = visit(expr.right).fromRef()) {
                    is MNumber -> MArray(l.toMutableList() + Ref(r))
                    
                    is MArray  -> MArray(l.toMutableList() + r.toMutableList())
                    
                    else       -> invalidRightOperandError(r, expr.op, expr.loc)
                }
                
                else       -> invalidLeftOperandError(l, expr.op, expr.loc)
            }
            
            Token.Type.ORR -> when (val l = visit(expr.left).fromRef()) {
                is MNumber -> when (val r = visit(expr.right).fromRef()) {
                    is MNumber -> l orr r
                    
                    else       -> invalidRightOperandError(r, expr.op, expr.loc)
                }
                
                else       -> invalidLeftOperandError(l, expr.op, expr.loc)
            }
            
            Token.Type.XOR -> when (val l = visit(expr.left).fromRef()) {
                is MNumber -> when (val r = visit(expr.right).fromRef()) {
                    is MNumber -> l xor r
                    
                    else       -> invalidRightOperandError(r, expr.op, expr.loc)
                }
                
                else       -> invalidLeftOperandError(l, expr.op, expr.loc)
            }
            
            Token.Type.BND -> when (val l = visit(expr.left).fromRef()) {
                is MNumber -> when (val r = visit(expr.right).fromRef()) {
                    is MNumber -> l bnd r
                    
                    else       -> invalidRightOperandError(r, expr.op, expr.loc)
                }
                
                else       -> invalidLeftOperandError(l, expr.op, expr.loc)
            }
            
            Token.Type.BOR -> when (val l = visit(expr.left).fromRef()) {
                is MNumber -> when (val r = visit(expr.right).fromRef()) {
                    is MNumber -> l bor r
                    
                    else       -> invalidRightOperandError(r, expr.op, expr.loc)
                }
                
                else       -> invalidLeftOperandError(l, expr.op, expr.loc)
            }
            
            Token.Type.SHL -> when (val l = visit(expr.left).fromRef()) {
                is MNumber -> when (val r = visit(expr.right).fromRef()) {
                    is MNumber -> l shl r
                    
                    else       -> invalidRightOperandError(r, expr.op, expr.loc)
                }
                
                else       -> invalidLeftOperandError(l, expr.op, expr.loc)
            }
            
            Token.Type.SHR -> when (val l = visit(expr.left).fromRef()) {
                is MNumber -> when (val r = visit(expr.right).fromRef()) {
                    is MNumber -> l shr r
                    
                    else       -> invalidRightOperandError(r, expr.op, expr.loc)
                }
                
                else       -> invalidLeftOperandError(l, expr.op, expr.loc)
            }
            
            Token.Type.USR -> when (val l = visit(expr.left).fromRef()) {
                is MNumber -> when (val r = visit(expr.right).fromRef()) {
                    is MNumber -> l usr r
                    
                    else       -> invalidRightOperandError(r, expr.op, expr.loc)
                }
                
                else       -> invalidLeftOperandError(l, expr.op, expr.loc)
            }
            
            Token.Type.ASN -> when (val l = visit(expr.left)) {
                is Ref    -> when (val r = visit(expr.right).fromRef()) {
                    is MNumber -> l.assign(r)
                    
                    is MArray  -> l.assign(r[0].value)
                    
                    else       -> invalidRightOperandError(r, expr.op, expr.loc)
                }
                
                is MArray -> when (val r = visit(expr.right).fromRef()) {
                    is MNumber -> l[0].value = r
                    
                    is MArray  -> {
                        val minSize = min(l.size, r.size)
                        
                        for (i in 0 until minSize) {
                            l[i].value = r[i].value
                        }
                    }
                    
                    else       -> invalidRightOperandError(r, expr.op, expr.loc)
                }
                
                else      -> invalidLeftOperandError(l, expr.op, expr.loc)
            }
            
            else           -> unexpectedError("BROKEN BINARY OPERATOR '${expr.op}'!")
        }
    }
    
    override fun visitTernaryExpr(expr: Expr.Ternary) =
        when (val test = visit(expr.test).fromRef()) {
            is MNumber -> if (test.toBoolean())
                visit(expr.yes)
            else
                visit(expr.no)
            
            else       -> invalidTestExprError(expr.test.loc)
        }
    
    override fun visitNumberExpr(expr: Expr.Number) =
        MNumber.Float(expr.value)
    
    override fun visitArrayExpr(expr: Expr.Array): Any {
        val elements = mutableListOf<MNumber>()
        
        for (subExpr in expr.elements) {
            elements.add(visit(subExpr).fromRef() as MNumber)
        }
        
        return MArray(elements.map { Ref(it) })
    }
    
    override fun visitSingleExpr(expr: Expr.Single): Any {
        var index = (visit(expr.index).fromRef() as? MNumber ?: invalidMemoryIndexError(expr.index.loc)).toFloat()
        
        if (index < 0) {
            index += config.size
        }
        
        return memory.peek()[index.toInt()]
    }
    
    override fun visitFixedRangeExpr(expr: Expr.FixedRange): Any {
        val list = mutableListOf<Ref>()
        
        var start = when (val e = visit(expr.start).fromRef()) {
            is MNumber -> e.toFloat()
            
            is Unit    -> 0F
            
            else       -> invalidRangeExprError("start", expr.start.loc)
        }
    
        if (start < 0) {
            start = config.size - start
        }
        
        var end = when (val e = visit(expr.end).fromRef()) {
            is MNumber -> e.toFloat()
            
            is Unit    -> memory.peek().size - 1F
            
            else       -> invalidRangeExprError("end", expr.end.loc)
        }
    
        if (end < 0) {
            end = config.size - end
        }
        
        val step = when (val e = visit(expr.step).fromRef()) {
            is MNumber -> e.toFloat()
            
            is Unit    -> 1F
            
            else       -> invalidRangeExprError("step", expr.step.loc)
        }
        
        var i = start
        
        while (i <= end) {
            list.add(memory.peek()[i.toInt()])
            
            i += step
        }
        
        return MArray(list)
    }
    
    override fun visitRelativeRangeExpr(expr: Expr.RelativeRange): Any {
        val list = mutableListOf<Ref>()
        
        var start = when (val e = visit(expr.start).fromRef()) {
            is MNumber -> e.toFloat()
            
            is Unit    -> 0F
            
            else       -> invalidRangeExprError("start", expr.start.loc)
        }
    
        if (start < 0) {
            start = config.size - start
        }
        
        val count = when (val e = visit(expr.count).fromRef()) {
            is MNumber -> e.toFloat()
            
            else       -> invalidRangeExprError("count", expr.count.loc)
        }
        
        val step = when (val e = visit(expr.step).fromRef()) {
            is MNumber -> e.toFloat()
            
            is Unit    -> 1F
            
            else       -> invalidRangeExprError("step", expr.step.loc)
        }
        
        var c = 0
        var i = start
        
        while (c < count) {
            list.add(memory.peek()[i.toInt()])
            
            c++
            i += step
        }
        
        return MArray(list)
    }
    
    override fun visitDynamicLiteralExpr(expr: Expr.DynamicLiteral): Any = when (expr.char) {
        'A'  -> config.args.toMArray()
        
        'C'  -> counter
        
        'L'  -> MNumber.Float()
        
        'R'  -> MNumber.Float(Math.random().toFloat())
        
        'S'  -> MNumber.Float(config.size.toFloat())
        
        else -> unexpectedError("Broken dynamic literal!")
    }
    
    override fun visitNoneStmt(stmt: Stmt.None) = Unit
    
    override fun visitNumberInStmt(stmt: Stmt.NumberIn) {
        val ref = visit(stmt.expr) as? Ref ?: invalidStatementArgumentError(stmt.expr.loc)
        
        val input = readLine()?.takeIf { it.isNotEmpty() } ?: ""
        
        ref.value = if (stmt.isIntMode)
            MNumber.Int(input.toIntOrNull() ?: invalidNumericalInputError(input, stmt.loc))
        else
            MNumber.Float(input.toFloatOrNull() ?: invalidNumericalInputError(input, stmt.loc))
    }
    
    override fun visitNumberOutStmt(stmt: Stmt.NumberOut) {
        val number = visit(stmt.expr).fromRef()
        
        number as? MNumber ?: invalidStatementArgumentError(stmt.expr.loc)
        
        if (stmt.isIntMode) {
            print(BigDecimal("${number.value}").stripTrailingZeros().toPlainString())
        }
        else {
            print(number)
        }
    }
    
    override fun visitTextInStmt(stmt: Stmt.TextIn) {
        if (inputQueue.isEmpty()) {
            val input = readLine()?.takeIf { it.isNotEmpty() } ?: return
            
            if (stmt.isIntMode) {
                inputQueue.addAll(input.map { MNumber.Int(it.code) } + MNumber.Int())
            }
            else {
                inputQueue.addAll(input.map { MNumber.Float(it.code.toFloat()) } + MNumber.Float())
            }
        }
        
        val number = visit(stmt.expr) as? Ref ?: invalidStatementArgumentError(stmt.expr.loc)
        
        number.value = inputQueue.removeFirst()
    }
    
    override fun visitTextOutStmt(stmt: Stmt.TextOut) {
        val number = visit(stmt.expr).fromRef() as? MNumber ?: invalidStatementArgumentError(stmt.expr.loc)
        
        print(number.toChar())
    }
    
    override fun visitLabelStmt(stmt: Stmt.Label) {
        val id = (visit(stmt.id).fromRef() as? MNumber ?: invalidStatementArgumentError(stmt.id.loc)).toFloat()
        
        labels[id] = counter.value.toInt()
    }
    
    override fun visitGotoStmt(stmt: Stmt.Goto) {
        val id = (visit(stmt.id).fromRef() as? MNumber ?: invalidStatementArgumentError(stmt.id.loc)).toFloat()
        
        counter.set(labels[id] ?: findLabel(id) ?: counter.value.toInt())
    }
    
    private fun findLabel(id: Float): Int? {
        for ((i, stmt) in stmts.withIndex()) {
            if (stmt is Stmt.Label) {
                val otherID = (visit(stmt.id).fromRef() as? MNumber ?: invalidStatementArgumentError(stmt.id.loc)).value
                
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
            (visit(stmt.condition).fromRef() as? MNumber
                ?: invalidStatementArgumentError(stmt.condition.loc)).toBoolean()
        
        if (condition) {
            counter.preIncrement()
        }
    }
    
    override fun visitGosubStmt(stmt: Stmt.Gosub) {
        val id = (visit(stmt.id).fromRef() as? MNumber ?: invalidStatementArgumentError(stmt.id.loc)).toFloat()
        
        callStack.push(counter.value.toInt())
        
        counter.set(labels[id] ?: findLabel(id) ?: counter.value.toInt())
    }
    
    override fun visitReturnStmt(stmt: Stmt.Return) {
        if (callStack.isNotEmpty()) {
            val last = callStack.pop()
            
            counter.set(last)
        }
    }
    
    override fun visitSystemArgStmt(stmt: Stmt.SystemArg) {
        val expr = visit(stmt.expr).fromRef() as? MNumber ?: invalidStatementArgumentError(stmt.expr.loc)
        
        systemInputQueue.add(expr.toFloat())
    }
    
    override fun visitSystemCallStmt(stmt: Stmt.SystemCall) {
        if (systemInputQueue.isNotEmpty()) {
            val start = systemInputQueue.removeFirst().toInt()
            
            var end = start
            
            do {
                end++
            }
            while (memory.peek()[end].value.toFloat() != 0F)
            
            val commandName = memory.peek()[start until end].ascii
            
            val command = Library[commandName] ?: undefinedCommandError(commandName, stmt.loc)
            
            val args = mutableListOf<Float>()
            
            for (i in 0 until command.arity) {
                args.add(systemInputQueue.removeFirst())
            }
            
            val result = command(this, args.toFloatArray())
            
            systemOutputQueue.addAll(result.toList())
        }
        
        if (systemOutputQueue.isNotEmpty()) {
            val number = visit(stmt.expr) as? Ref ?: invalidStatementArgumentError(stmt.expr.loc)
            
            number.value = MNumber.Float(systemOutputQueue.removeFirst())
        }
    }
    
    override fun visitMemoryPushStmt(stmt: Stmt.MemoryPush) {
        memory.push(MArray(config.size))
    }
    
    override fun visitMemoryPopStmt(stmt: Stmt.MemoryPop) {
        if (memory.size > 1) {
            memory.pop()
        }
    }
    
    override fun visitMemoryOutStmt(stmt: Stmt.MemoryOut) {
        memoryQueue.add(visit(stmt.expr).fromRef() as? MNumber ?: invalidStatementArgumentError(stmt.expr.loc))
    }
    
    override fun visitMemoryInStmt(stmt: Stmt.MemoryIn) {
        val ref = visit(stmt.expr) as? Ref ?: invalidStatementArgumentError(stmt.expr.loc)
        
        ref.value = memoryQueue.removeFirst()
    }
    
    override fun visitSingleAssignStmt(stmt: Stmt.SingleAssign) {
        val index =
            (visit(stmt.single.index).fromRef() as? MNumber
                ?: invalidMemoryIndexError(stmt.single.index.loc)).toInt()
        
        when (val expr = visit(stmt.expr).fromRef()) {
            is MNumber -> memory.peek()[index].value = expr
            
            is MArray  -> memory.peek()[index].value = expr[0].value
        }
    }
    
    override fun visitFixedRangeAssignStmt(stmt: Stmt.FixedRangeAssign) {
        var start = when (val expr = visit(stmt.range.start).fromRef()) {
            is MNumber -> expr.toFloat()
            
            is Unit    -> 0F
            
            else       -> invalidRangeExprError("start", stmt.range.start.loc)
        }
        
        if (start < 0) {
            start = config.size - start
        }
        
        var end = when (val expr = visit(stmt.range.end).fromRef()) {
            is MNumber -> expr.toFloat()
            
            is Unit    -> config.size.toFloat() - 1
            
            else       -> invalidRangeExprError("end", stmt.range.end.loc)
        }
        
        if (end < 0) {
            end = config.size - end
        }
        
        val step = when (val expr = visit(stmt.range.step).fromRef()) {
            is MNumber -> expr.toFloat()
            
            is Unit    -> 1F
            
            else       -> invalidRangeExprError("step", stmt.range.step.loc)
        }
        
        when (val expr = visit(stmt.expr).fromRef()) {
            is MNumber -> {
                var memoryIndex = start
                
                while (memoryIndex <= end) {
                    memory.peek()[memoryIndex.toInt()].value = expr
                    
                    memoryIndex += step
                }
            }
            
            is MArray  -> {
                var memoryIndex = start
                
                var arrayIndex = 0
                
                while (memoryIndex <= end && arrayIndex < expr.size) {
                    memory.peek()[memoryIndex.toInt()].value = expr[arrayIndex].value
                    
                    memoryIndex += step
                    
                    arrayIndex++
                }
            }
        }
    }
    
    override fun visitRelativeRangeAssignStmt(stmt: Stmt.RelativeRangeAssign) {
        val start = when (val expr = visit(stmt.range.start).fromRef()) {
            is MNumber -> expr.toInt()
            
            is Unit    -> 0
            
            else       -> invalidRangeExprError("start", stmt.range.start.loc)
        }
        
        val count = when (val expr = visit(stmt.range.count).fromRef()) {
            is MNumber -> expr.toInt()
            
            else       -> invalidRangeExprError("count", stmt.range.count.loc)
        }
        
        val step = when (val expr = visit(stmt.range.step).fromRef()) {
            is MNumber -> expr.toInt()
            
            is Unit    -> 1
            
            else       -> invalidRangeExprError("step", stmt.range.step.loc)
        }
        
        when (val expr = visit(stmt.expr).fromRef()) {
            is MNumber -> {
                var memoryIndex = start
                
                while (memoryIndex < start + count) {
                    memory.peek()[memoryIndex].value = expr
                    
                    memoryIndex += step
                }
            }
            
            is MArray  -> {
                var memoryIndex = start
                
                var arrayIndex = 0
                
                while (memoryIndex < start + count && arrayIndex < expr.size) {
                    memory.peek()[memoryIndex].value = expr[arrayIndex].value
                    
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
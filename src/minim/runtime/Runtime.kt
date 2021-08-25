package minim.runtime

import minim.parser.Expr
import minim.parser.Expr.Binary.Operator.*
import minim.parser.Expr.DynamicLiteral.Name.*
import minim.parser.Expr.Postfix.Operator.*
import minim.parser.Expr.Prefix.Operator.*
import minim.parser.Stmt
import minim.util.*
import java.math.BigDecimal
import java.util.*
import kotlin.collections.ArrayDeque
import kotlin.math.min
import kotlin.math.sign
import minim.parser.Expr.Postfix.Operator.Decrement as PostDecrement
import minim.parser.Expr.Postfix.Operator.Increment as PostIncrement
import minim.parser.Expr.Postfix.Operator.Inverted as PostInverted
import minim.parser.Expr.Postfix.Operator.Narrowed as PostNarrowed
import minim.parser.Expr.Postfix.Operator.Toggled as PostToggled
import minim.parser.Expr.Prefix.Operator.Decrement as PreDecrement
import minim.parser.Expr.Prefix.Operator.Increment as PreIncrement
import minim.parser.Expr.Prefix.Operator.Inverted as PreInverted
import minim.parser.Expr.Prefix.Operator.Narrowed as PreNarrowed
import minim.parser.Expr.Prefix.Operator.Toggled as PreToggled

class Runtime(private val config: Config, private val stmts: List<Stmt>) : Expr.Visitor<Any>, Stmt.Visitor<Unit> {
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
        
        println()
        memory.peek()[0..0xFF].printDebug()
        
        return memory.peek().first().value
    }
    
    override fun visitNoneExpr(expr: Expr.None) = Unit
    
    override fun visitPrefixExpr(expr: Expr.Prefix): Any {
        return when (expr.operator) {
            Negative     -> when (val e = visit(expr.expr).fromRef()) {
                is MNumber -> -e
                
                else       -> invalidPrefixOperandError(e, expr.operator, expr.loc)
            }
            
            Not          -> when (val e = visit(expr.expr).fromRef()) {
                is MNumber -> !e
                
                else       -> invalidPrefixOperandError(e, expr.operator, expr.loc)
            }
            
            Narrow       -> when (val e = visit(expr.expr).fromRef()) {
                is MNumber -> e.narrow()
                
                else       -> invalidPrefixOperandError(e, expr.operator, expr.loc)
            }
            
            Invert       -> when (val e = visit(expr.expr).fromRef()) {
                is MNumber -> e.inv()
                
                else       -> invalidPrefixOperandError(e, expr.operator, expr.loc)
            }
            
            PreIncrement -> when (val e = visit(expr.expr)) {
                is Ref -> e.preIncrement()
                
                else   -> invalidPrefixOperandError(e, expr.operator, expr.loc)
            }
            
            PreDecrement -> when (val e = visit(expr.expr)) {
                is Ref -> e.preDecrement()
                
                else   -> invalidPrefixOperandError(e, expr.operator, expr.loc)
            }
            
            PreNarrowed  -> when (val e = visit(expr.expr)) {
                is Ref -> e.preNarrow()
                
                else   -> invalidPrefixOperandError(e, expr.operator, expr.loc)
            }
            
            PreToggled   -> when (val e = visit(expr.expr)) {
                is Ref -> e.preToggle()
                
                else   -> invalidPrefixOperandError(e, expr.operator, expr.loc)
            }
            
            PreInverted  -> when (val e = visit(expr.expr)) {
                is Ref -> e.preInvert()
                
                else   -> invalidPrefixOperandError(e, expr.operator, expr.loc)
            }
        }
    }
    
    override fun visitPostfixExpr(expr: Expr.Postfix): Any {
        return when (expr.operator) {
            PostIncrement -> when (val e = visit(expr.expr)) {
                is Ref -> e.postIncrement()
                
                else   -> invalidPostfixOperandError(e, expr.operator, expr.loc)
            }
            
            PostDecrement -> when (val e = visit(expr.expr)) {
                is Ref -> e.postDecrement()
                
                else   -> invalidPostfixOperandError(e, expr.operator, expr.loc)
            }
            
            PostNarrowed  -> when (val e = visit(expr.expr)) {
                is Ref -> e.postNarrow()
                
                else   -> invalidPostfixOperandError(e, expr.operator, expr.loc)
            }
            
            PostToggled   -> when (val e = visit(expr.expr)) {
                is Ref -> e.postToggle()
                
                else   -> invalidPostfixOperandError(e, expr.operator, expr.loc)
            }
            
            PostInverted  -> when (val e = visit(expr.expr)) {
                is Ref -> e.postInvert()
                
                else   -> invalidPostfixOperandError(e, expr.operator, expr.loc)
            }
            
            FloatCast     -> when (val e = visit(expr.expr).fromRef()) {
                is MNumber -> MNumber.Float(e.toFloat())
                
                is MArray  -> for (ref in e) {
                    ref.value = MNumber.Float(ref.value.toFloat())
                }
                
                else       -> invalidPostfixOperandError(e, expr.operator, expr.loc)
            }
            
            IntegerCast   -> when (val e = visit(expr.expr).fromRef()) {
                is MNumber -> MNumber.Int(e.toInt())
                
                is MArray  -> for (ref in e) {
                    ref.value = MNumber.Int(ref.value.toInt())
                }
                
                else       -> invalidPostfixOperandError(e, expr.operator, expr.loc)
            }
            
            StringCast    -> when (val e = visit(expr.expr).fromRef()) {
                is MNumber -> e.toString().toMArray()
                
                else       -> invalidPostfixOperandError(e, expr.operator, expr.loc)
            }
        }
    }
    
    override fun visitBinaryExpr(expr: Expr.Binary): Any {
        return when (expr.operator) {
            Add                -> when (val l = visit(expr.left).fromRef()) {
                is MNumber -> when (val r = visit(expr.right).fromRef()) {
                    is MNumber -> l + r
                    
                    else       -> invalidRightOperandError(r, expr.operator, expr.loc)
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
                    
                    else       -> invalidRightOperandError(r, expr.operator, expr.loc)
                }
                
                else       -> invalidLeftOperandError(l, expr.operator, expr.loc)
            }
            
            Subtract           -> when (val l = visit(expr.left).fromRef()) {
                is MNumber -> when (val r = visit(expr.right).fromRef()) {
                    is MNumber -> l - r
                    
                    else       -> invalidRightOperandError(r, expr.operator, expr.loc)
                }
                
                else       -> invalidLeftOperandError(l, expr.operator, expr.loc)
            }
            
            Multiply           -> when (val l = visit(expr.left).fromRef()) {
                is MNumber -> when (val r = visit(expr.right).fromRef()) {
                    is MNumber -> l * r
                    
                    else       -> invalidRightOperandError(r, expr.operator, expr.loc)
                }
                
                else       -> invalidLeftOperandError(l, expr.operator, expr.loc)
            }
            
            Divide             -> when (val l = visit(expr.left).fromRef()) {
                is MNumber -> when (val r = visit(expr.right).fromRef()) {
                    is MNumber -> l / r
                    
                    else       -> invalidRightOperandError(r, expr.operator, expr.loc)
                }
                
                else       -> invalidLeftOperandError(l, expr.operator, expr.loc)
            }
            
            Modulus            -> when (val l = visit(expr.left).fromRef()) {
                is MNumber -> when (val r = visit(expr.right).fromRef()) {
                    is MNumber -> l % r
                    
                    else       -> invalidRightOperandError(r, expr.operator, expr.loc)
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
                    
                    else       -> invalidRightOperandError(r, expr.operator, expr.loc)
                }
                
                else       -> invalidLeftOperandError(l, expr.operator, expr.loc)
            }
            
            Less               -> when (val l = visit(expr.left).fromRef()) {
                is MNumber -> when (val r = visit(expr.right).fromRef()) {
                    is MNumber -> l lss r
                    
                    else       -> invalidRightOperandError(r, expr.operator, expr.loc)
                }
                
                else       -> invalidLeftOperandError(l, expr.operator, expr.loc)
            }
            
            LessEqual          -> when (val l = visit(expr.left).fromRef()) {
                is MNumber -> when (val r = visit(expr.right).fromRef()) {
                    is MNumber -> l leq r
                    
                    else       -> invalidRightOperandError(r, expr.operator, expr.loc)
                }
                
                else       -> invalidLeftOperandError(l, expr.operator, expr.loc)
            }
            
            Greater            -> when (val l = visit(expr.left).fromRef()) {
                is MNumber -> when (val r = visit(expr.right).fromRef()) {
                    is MNumber -> l grt r
                    
                    else       -> invalidRightOperandError(r, expr.operator, expr.loc)
                }
                
                else       -> invalidLeftOperandError(l, expr.operator, expr.loc)
            }
            
            GreaterEqual       -> when (val l = visit(expr.left).fromRef()) {
                is MNumber -> when (val r = visit(expr.right).fromRef()) {
                    is MNumber -> l geq r
                    
                    else       -> invalidRightOperandError(r, expr.operator, expr.loc)
                }
                
                else       -> invalidLeftOperandError(l, expr.operator, expr.loc)
            }
            
            Equal              -> when (val l = visit(expr.left).fromRef()) {
                is MNumber -> when (val r = visit(expr.right).fromRef()) {
                    is MNumber -> l equ r
                    
                    else       -> invalidRightOperandError(r, expr.operator, expr.loc)
                }
                
                else       -> invalidLeftOperandError(l, expr.operator, expr.loc)
            }
            
            NotEqual           -> when (val l = visit(expr.left).fromRef()) {
                is MNumber -> when (val r = visit(expr.right).fromRef()) {
                    is MNumber -> l neq r
                    
                    else       -> invalidRightOperandError(r, expr.operator, expr.loc)
                }
                
                else       -> invalidLeftOperandError(l, expr.operator, expr.loc)
            }
            
            And                -> {
                val l = visit(expr.left).fromRef()
                
                if (l is MNumber) {
                    if (!l.toBoolean()) {
                        return when (l) {
                            is MNumber.Float -> MNumber.Float(0F)
                            is MNumber.Int   -> MNumber.Int(0)
                        }
                    }
                }
                else {
                    invalidLeftOperandError(l, expr.operator, expr.loc)
                }
                
                val r = visit(expr.right).fromRef()
                
                if (r is MNumber) {
                    return when (r) {
                        is MNumber.Float -> MNumber.Float(r.toBoolean().toFloat())
                        is MNumber.Int   -> MNumber.Int(r.toBoolean().toInt())
                    }
                }
                else {
                    invalidRightOperandError(r, expr.operator, expr.loc)
                }
            }
            
            Or                 -> {
                val l = visit(expr.left).fromRef()
                
                if (l is MNumber) {
                    if (l.toBoolean()) {
                        return when (l) {
                            is MNumber.Float -> MNumber.Float(1F)
                            is MNumber.Int   -> MNumber.Int(1)
                        }
                    }
                }
                else {
                    invalidLeftOperandError(l, expr.operator, expr.loc)
                }
                
                val r = visit(expr.right).fromRef()
                
                if (r is MNumber) {
                    return when (r) {
                        is MNumber.Float -> MNumber.Float(r.toBoolean().toFloat())
                        is MNumber.Int   -> MNumber.Int(r.toBoolean().toInt())
                    }
                }
                else {
                    invalidRightOperandError(r, expr.operator, expr.loc)
                }
            }
            
            Xor                -> when (val l = visit(expr.left).fromRef()) {
                is MNumber -> when (val r = visit(expr.right).fromRef()) {
                    is MNumber -> l xor r
                    
                    else       -> invalidRightOperandError(r, expr.operator, expr.loc)
                }
                
                else       -> invalidLeftOperandError(l, expr.operator, expr.loc)
            }
            
            BitAnd             -> when (val l = visit(expr.left).fromRef()) {
                is MNumber -> when (val r = visit(expr.right).fromRef()) {
                    is MNumber -> l bnd r
                    
                    is MArray  -> MArray(mutableListOf(Ref(l)) + r.toMutableList())
                    
                    else       -> invalidRightOperandError(r, expr.operator, expr.loc)
                }
                
                is MArray  -> when (val r = visit(expr.right).fromRef()) {
                    is MNumber -> MArray(l.toMutableList() + Ref(r))
                    
                    is MArray  -> MArray(l.toMutableList() + r.toMutableList())
                    
                    else       -> invalidRightOperandError(r, expr.operator, expr.loc)
                }
                
                else       -> invalidLeftOperandError(l, expr.operator, expr.loc)
            }
            
            BitOr              -> when (val l = visit(expr.left).fromRef()) {
                is MNumber -> when (val r = visit(expr.right).fromRef()) {
                    is MNumber -> l bor r
                    
                    else       -> invalidRightOperandError(r, expr.operator, expr.loc)
                }
                
                else       -> invalidLeftOperandError(l, expr.operator, expr.loc)
            }
            
            ShiftLeft          -> when (val l = visit(expr.left).fromRef()) {
                is MNumber -> when (val r = visit(expr.right).fromRef()) {
                    is MNumber -> l shl r
                    
                    else       -> invalidRightOperandError(r, expr.operator, expr.loc)
                }
                
                else       -> invalidLeftOperandError(l, expr.operator, expr.loc)
            }
            
            ShiftRight         -> when (val l = visit(expr.left).fromRef()) {
                is MNumber -> when (val r = visit(expr.right).fromRef()) {
                    is MNumber -> l shr r
                    
                    else       -> invalidRightOperandError(r, expr.operator, expr.loc)
                }
                
                else       -> invalidLeftOperandError(l, expr.operator, expr.loc)
            }
            
            UnsignedShiftRight -> when (val l = visit(expr.left).fromRef()) {
                is MNumber -> when (val r = visit(expr.right).fromRef()) {
                    is MNumber -> l usr r
                    
                    else       -> invalidRightOperandError(r, expr.operator, expr.loc)
                }
                
                else       -> invalidLeftOperandError(l, expr.operator, expr.loc)
            }
            
            Assign             -> when (val l = visit(expr.left)) {
                is Ref    -> when (val r = visit(expr.right).fromRef()) {
                    is MNumber -> l.assign(r)
                    
                    is MArray  -> l.assign(r[0].value)
                    
                    else       -> invalidRightOperandError(r, expr.operator, expr.loc)
                }
                
                is MArray -> when (val r = visit(expr.right).fromRef()) {
                    is MNumber -> l[0].value = r
                    
                    is MArray  -> {
                        val minSize = min(l.size, r.size)
                        
                        for (i in 0 until minSize) {
                            l[i].value = r[i].value
                        }
                    }
                    
                    else       -> invalidRightOperandError(r, expr.operator, expr.loc)
                }
                
                else      -> invalidLeftOperandError(l, expr.operator, expr.loc)
            }
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
            start += config.size
        }
        
        var end = when (val e = visit(expr.end).fromRef()) {
            is MNumber -> e.toFloat()
            
            is Unit    -> memory.peek().size.toFloat()
            
            else       -> invalidRangeExprError("end", expr.end.loc)
        }
        
        if (end < 0) {
            end += config.size
        }
        
        val step = when (val e = visit(expr.step).fromRef()) {
            is MNumber -> e.toFloat()
            
            is Unit    -> 1F
            
            else       -> invalidRangeExprError("step", expr.step.loc)
        }
        
        if (step < 0 && start < end) {
            val temp = start
            start = end - 1
            end = temp - 1
        }
        
        var i = start
        
        while ((i - end).sign == -step.sign) {
            println((i - end).sign)
            println(step.sign)
            
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
            start += config.size
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
    
    override fun visitDynamicLiteralExpr(expr: Expr.DynamicLiteral): Any = when (expr.name) {
        A -> config.args.toMArray()
        
        C -> counter
        
        R -> MNumber.Float(Math.random().toFloat())
        
        S -> MNumber.Float(config.size.toFloat())
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
    
    override fun visitTextFlushStmt(stmt: Stmt.TextFlush) {
        inputQueue.clear()
    }
    
    override fun visitLabelStmt(stmt: Stmt.Label) {
        val id = (visit(stmt.id).fromRef() as? MNumber ?: invalidStatementArgumentError(stmt.id.loc)).toFloat()
        
        labels[id] = counter.value.toInt()
    }
    
    override fun visitGotoStmt(stmt: Stmt.Goto) {
        val id = (visit(stmt.id).fromRef() as? MNumber ?: invalidStatementArgumentError(stmt.id.loc)).toFloat()
        
        var pos = labels[id] ?: findLabel(id)
        
        if (pos == null) {
            pos = when (val fallback = visit(stmt.fallback).fromRef()) {
                is MNumber -> labels[fallback.toFloat()] ?: findLabel(fallback.toFloat())
                
                is Unit    -> null
                
                else       -> invalidStatementArgumentError(stmt.fallback.loc)
            }
        }
        
        counter.set(pos ?: counter.value.toInt())
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
        
        var pos = labels[id] ?: findLabel(id)
        
        if (pos == null) {
            pos = when (val fallback = visit(stmt.fallback).fromRef()) {
                is MNumber -> labels[fallback.toFloat()] ?: findLabel(fallback.toFloat())
                
                is Unit    -> null
                
                else       -> invalidStatementArgumentError(stmt.fallback.loc)
            }
        }
        
        callStack.push(counter.value.toInt())
        
        counter.set(pos ?: counter.value.toInt())
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
    
    override fun visitSystemFlushStmt(stmt: Stmt.SystemFlush) {
        systemInputQueue.clear()
        systemOutputQueue.clear()
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
    
    override fun visitMemoryFlushStmt(stmt: Stmt.MemoryFlush) {
        memoryQueue.clear()
    }
    
    override fun visitSingleAssignStmt(stmt: Stmt.SingleAssign) {
        var index =
            (visit(stmt.single.index).fromRef() as? MNumber
                ?: invalidMemoryIndexError(stmt.single.index.loc)).toInt()
        
        if (index < 0) {
            index += config.size
        }
        
        when (val expr = visit(stmt.expr).fromRef()) {
            is MNumber -> memory.peek()[index].value = expr
            
            is MArray  -> memory.peek()[index].value = expr[0].value
        }
    }
    
    override fun visitFixedRangeAssignStmt(stmt: Stmt.FixedRangeAssign) {
        var start = when (val e = visit(stmt.range.start).fromRef()) {
            is MNumber -> e.toFloat()
            
            is Unit    -> 0F
            
            else       -> invalidRangeExprError("start", stmt.range.start.loc)
        }
        
        if (start < 0) {
            start += config.size
        }
        
        var end = when (val e = visit(stmt.range.end).fromRef()) {
            is MNumber -> e.toFloat()
            
            is Unit    -> memory.peek().size.toFloat()
            
            else       -> invalidRangeExprError("end", stmt.range.end.loc)
        }
        
        if (end < 0) {
            end += config.size
        }
        
        val step = when (val e = visit(stmt.range.step).fromRef()) {
            is MNumber -> e.toFloat()
            
            is Unit    -> 1F
            
            else       -> invalidRangeExprError("step", stmt.range.step.loc)
        }
        
        if (step < 0 && start < end) {
            val temp = start
            start = end - 1
            end = temp - 1
        }
        
        when (val expr = visit(stmt.expr).fromRef()) {
            is MNumber -> {
                var memoryIndex = start
                
                while ((memoryIndex - end).sign == -step.sign) {
                    memory.peek()[memoryIndex.toInt()].value = expr
                    
                    memoryIndex += step
                }
            }
            
            is MArray  -> {
                var memoryIndex = start
                
                var arrayIndex = 0
                
                while ((memoryIndex - end).sign == -step.sign && arrayIndex < expr.size) {
                    memory.peek()[memoryIndex.toInt()].value = expr[arrayIndex].value
                    
                    memoryIndex += step
                    
                    arrayIndex++
                }
            }
        }
    }
    
    override fun visitRelativeRangeAssignStmt(stmt: Stmt.RelativeRangeAssign) {
        var start = when (val e = visit(stmt.range.start).fromRef()) {
            is MNumber -> e.toFloat()
            
            is Unit    -> 0F
            
            else       -> invalidRangeExprError("start", stmt.range.start.loc)
        }
        
        if (start < 0) {
            start += config.size
        }
        
        val count = when (val expr = visit(stmt.range.count).fromRef()) {
            is MNumber -> expr.toFloat()
            
            else       -> invalidRangeExprError("count", stmt.range.count.loc)
        }
        
        val step = when (val expr = visit(stmt.range.step).fromRef()) {
            is MNumber -> expr.toFloat()
            
            is Unit    -> 1F
            
            else       -> invalidRangeExprError("step", stmt.range.step.loc)
        }
        
        when (val expr = visit(stmt.expr).fromRef()) {
            is MNumber -> {
                var memoryIndex = start
                
                while (memoryIndex < start + count) {
                    memory.peek()[memoryIndex.toInt()].value = expr
                    
                    memoryIndex += step
                }
            }
            
            is MArray  -> {
                var memoryIndex = start
                
                var arrayIndex = 0
                
                while (memoryIndex < start + count && arrayIndex < expr.size) {
                    memory.peek()[memoryIndex.toInt()].value = expr[arrayIndex].value
                    
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
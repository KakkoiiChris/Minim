package minim.runtime

import minim.parser.Expr
import minim.parser.Expr.Binary.Operator.*
import minim.parser.Expr.DynamicLiteral.Name.*
import minim.parser.Expr.Postfix.Operator.*
import minim.parser.Expr.Prefix.Operator.*
import minim.parser.Program
import minim.parser.Stmt
import minim.util.*
import java.math.BigDecimal
import java.util.*
import kotlin.collections.ArrayDeque
import kotlin.math.min
import kotlin.math.sign

import minim.parser.Expr.Postfix.Operator.DECREMENT as POST_DECREMENT
import minim.parser.Expr.Postfix.Operator.INCREMENT as POST_INCREMENT
import minim.parser.Expr.Postfix.Operator.INVERTED as POST_INVERTED
import minim.parser.Expr.Postfix.Operator.NARROWED as POST_NARROWED
import minim.parser.Expr.Postfix.Operator.TOGGLED as POST_TOGGLED
import minim.parser.Expr.Prefix.Operator.DECREMENT as PRE_DECREMENT
import minim.parser.Expr.Prefix.Operator.INCREMENT as PRE_INCREMENT
import minim.parser.Expr.Prefix.Operator.INVERTED as PRE_INVERTED
import minim.parser.Expr.Prefix.Operator.NARROWED as PRE_NARROWED
import minim.parser.Expr.Prefix.Operator.TOGGLED as PRE_TOGGLED

/**
 * The third stage of the interpreter; a visitor-based tree walker that executes the parsed statements.
 *
 * @param config the configuration data for this interpreter
 * @param program the [program][Program] to execute
 */
class Runtime(private val config: Config, private var program: Program) : Expr.Visitor<Any>, Stmt.Visitor<Unit> {
    /**
     * A stack of memory scopes.
     */
    val memory = Stack<MinimArray>()

    /**
     * A mapping of all encountered or looked-up goto labels, mapping the label id to the position of the label within the [list of statements][program].
     */
    private val labels = mutableMapOf<Float, Int>()

    /**
     * A stack of return locations for the gosub/return statements.
     */
    private val callStack = Stack<Int>()

    /**
     * The program counter.
     */
    private val counter = Reference(MinimNumber.Int())

    /**
     * Shortcut for getting the value of the program counter.
     */
    private val pos get() = counter.value.toInt()

    /**
     * A queue used for text input.
     */
    private val inputQueue = ArrayDeque<MinimNumber<*>>()

    /**
     * A queue used for passing arguments to a system function.
     */
    private val systemInputQueue = ArrayDeque<Float>()

    /**
     * A queue used for receiving the results of a system function.
     */
    private val systemOutputQueue = ArrayDeque<Float>()

    /**
     * A queue used for passing values between memory scopes.
     */
    private val memoryQueue = ArrayDeque<MinimNumber<*>>()

    init {
        memory.push(MinimArray(config.size))
    }

    /**
     * Executes the [statements][program] in sequential order, and gets the last value of memory index `0`.
     *
     * @return memory index `0`
     */
    fun run(): MinimNumber<*> {
        while (pos < program.size) {
            visit(program[pos])

            counter.preIncrement()
        }

        if (config.debug) {
            println()

            memory.peek().printDebugTable()
        }

        return memory.peek().first().value
    }

    /**
     * Sets a new list of [statements][Stmt] to run, and sets the program counter to `0`, for use in REPL mode.
     *
     * @param program the new [program][Program] to run
     */
    fun reset(program: Program) {
        this.program = program

        counter.set(0)
    }

    /**
     * Simply gets [Unit], used to indicate the default value with the ranged memory accessors.
     *
     * @param expr the expression to evaluate
     *
     * @return [Unit]
     */
    override fun visitNoneExpr(expr: Expr.None) = Unit

    /**
     * Evaluates the result of a single prefix operator.
     *
     * @param expr the expression to evaluate
     */
    override fun visitPrefixExpr(expr: Expr.Prefix): Any {
        return when (expr.operator) {
            NEGATE        -> when (val e = visit(expr.expr).fromReference()) {
                is MinimNumber<*> -> -e

                else              -> invalidPrefixOperandError(e, expr.operator, expr.loc)
            }

            NOT           -> when (val e = visit(expr.expr).fromReference()) {
                is MinimNumber<*> -> !e

                else              -> invalidPrefixOperandError(e, expr.operator, expr.loc)
            }

            NARROW        -> when (val e = visit(expr.expr).fromReference()) {
                is MinimNumber<*> -> e.narrow()

                else              -> invalidPrefixOperandError(e, expr.operator, expr.loc)
            }

            INVERT        -> when (val e = visit(expr.expr).fromReference()) {
                is MinimNumber<*> -> e.inv()

                else              -> invalidPrefixOperandError(e, expr.operator, expr.loc)
            }

            PRE_INCREMENT -> when (val e = visit(expr.expr)) {
                is Reference -> e.preIncrement()

                else         -> invalidPrefixOperandError(e, expr.operator, expr.loc)
            }

            PRE_DECREMENT -> when (val e = visit(expr.expr)) {
                is Reference -> e.preDecrement()

                else         -> invalidPrefixOperandError(e, expr.operator, expr.loc)
            }

            PRE_NARROWED  -> when (val e = visit(expr.expr)) {
                is Reference -> e.preNarrow()

                else         -> invalidPrefixOperandError(e, expr.operator, expr.loc)
            }

            PRE_TOGGLED   -> when (val e = visit(expr.expr)) {
                is Reference -> e.preToggle()

                else         -> invalidPrefixOperandError(e, expr.operator, expr.loc)
            }

            PRE_INVERTED  -> when (val e = visit(expr.expr)) {
                is Reference -> e.preInvert()

                else         -> invalidPrefixOperandError(e, expr.operator, expr.loc)
            }
        }
    }

    /**
     * Evaluates the result of a single postfix operator.
     *
     * @param expr the expression to evaluate
     */
    override fun visitPostfixExpr(expr: Expr.Postfix): Any {
        return when (expr.operator) {
            POST_INCREMENT -> when (val e = visit(expr.expr)) {
                is Reference -> e.postIncrement()

                else         -> invalidPostfixOperandError(e, expr.operator, expr.loc)
            }

            POST_DECREMENT -> when (val e = visit(expr.expr)) {
                is Reference -> e.postDecrement()

                else         -> invalidPostfixOperandError(e, expr.operator, expr.loc)
            }

            POST_NARROWED  -> when (val e = visit(expr.expr)) {
                is Reference -> e.postNarrow()

                else         -> invalidPostfixOperandError(e, expr.operator, expr.loc)
            }

            POST_TOGGLED   -> when (val e = visit(expr.expr)) {
                is Reference -> e.postToggle()

                else         -> invalidPostfixOperandError(e, expr.operator, expr.loc)
            }

            POST_INVERTED  -> when (val e = visit(expr.expr)) {
                is Reference -> e.postInvert()

                else         -> invalidPostfixOperandError(e, expr.operator, expr.loc)
            }

            FLOAT_CAST     -> when (val e = visit(expr.expr).fromReference()) {
                is MinimNumber<*> -> MinimNumber.Float(e.toFloat())

                is MinimArray     -> for (ref in e) {
                    ref.value = MinimNumber.Float(ref.value.toFloat())
                }

                else              -> invalidPostfixOperandError(e, expr.operator, expr.loc)
            }

            INTEGER_CAST   -> when (val e = visit(expr.expr).fromReference()) {
                is MinimNumber<*> -> MinimNumber.Int(e.toInt())

                is MinimArray     -> for (ref in e) {
                    ref.value = MinimNumber.Int(ref.value.toInt())
                }

                else              -> invalidPostfixOperandError(e, expr.operator, expr.loc)
            }

            STRING_CAST    -> when (val e = visit(expr.expr).fromReference()) {
                is MinimNumber<*> -> e.toString().toMArray()

                else              -> invalidPostfixOperandError(e, expr.operator, expr.loc)
            }
        }
    }

    /**
     * Evaluates the result of a single binary operator.
     *
     * @param expr the expression to evaluate
     */
    override fun visitBinaryExpr(expr: Expr.Binary): Any {
        return when (expr.operator) {
            ADD                  -> when (val l = visit(expr.left).fromReference()) {
                is MinimNumber<*> -> when (val r = visit(expr.right).fromReference()) {
                    is MinimNumber<*> -> l + r

                    else              -> invalidRightOperandError(r, expr.operator, expr.loc)
                }

                is MinimArray     -> when (val r = visit(expr.right).fromReference()) {
                    is MinimNumber<*> -> MinimArray(l.toMutableList().let {
                        while (it.last().value.toInt() == 0) {
                            it.removeLast()
                        }
                        it
                    } + Reference(r))

                    is MinimArray     -> MinimArray(l.toMutableList().let {
                        while (it.last().value.toInt() == 0) {
                            it.removeLast()
                        }
                        it
                    } + r.toMutableList())

                    else              -> invalidRightOperandError(r, expr.operator, expr.loc)
                }

                else              -> invalidLeftOperandError(l, expr.operator, expr.loc)
            }

            SUBTRACT             -> when (val l = visit(expr.left).fromReference()) {
                is MinimNumber<*> -> when (val r = visit(expr.right).fromReference()) {
                    is MinimNumber<*> -> l - r

                    else              -> invalidRightOperandError(r, expr.operator, expr.loc)
                }

                else              -> invalidLeftOperandError(l, expr.operator, expr.loc)
            }

            MULTIPLY             -> when (val l = visit(expr.left).fromReference()) {
                is MinimNumber<*> -> when (val r = visit(expr.right).fromReference()) {
                    is MinimNumber<*> -> l * r

                    else              -> invalidRightOperandError(r, expr.operator, expr.loc)
                }

                else              -> invalidLeftOperandError(l, expr.operator, expr.loc)
            }

            DIVIDE               -> when (val l = visit(expr.left).fromReference()) {
                is MinimNumber<*> -> when (val r = visit(expr.right).fromReference()) {
                    is MinimNumber<*> -> l / r

                    else              -> invalidRightOperandError(r, expr.operator, expr.loc)
                }

                else              -> invalidLeftOperandError(l, expr.operator, expr.loc)
            }

            MODULUS              -> when (val l = visit(expr.left).fromReference()) {
                is MinimNumber<*> -> when (val r = visit(expr.right).fromReference()) {
                    is MinimNumber<*> -> l % r

                    else              -> invalidRightOperandError(r, expr.operator, expr.loc)
                }

                is MinimArray     -> when (val r = visit(expr.right).fromReference()) {
                    is MinimNumber<*> -> {
                        val index = l.indexOfFirst { it.value.toInt() == '%'.code }

                        if (index >= 0) {
                            l[index].value = r
                        }

                        l
                    }

                    is MinimArray     -> {
                        for (i in r.indices) {
                            val index = l.indexOfFirst { it.value.toInt() == '%'.code }

                            if (index >= 0) {
                                l[index].value = r[i].value
                            }
                        }

                        l
                    }

                    else              -> invalidRightOperandError(r, expr.operator, expr.loc)
                }

                else              -> invalidLeftOperandError(l, expr.operator, expr.loc)
            }

            LESS                 -> when (val l = visit(expr.left).fromReference()) {
                is MinimNumber<*> -> when (val r = visit(expr.right).fromReference()) {
                    is MinimNumber<*> -> l lss r

                    else              -> invalidRightOperandError(r, expr.operator, expr.loc)
                }

                else              -> invalidLeftOperandError(l, expr.operator, expr.loc)
            }

            LESS_EQUAL           -> when (val l = visit(expr.left).fromReference()) {
                is MinimNumber<*> -> when (val r = visit(expr.right).fromReference()) {
                    is MinimNumber<*> -> l leq r

                    else              -> invalidRightOperandError(r, expr.operator, expr.loc)
                }

                else              -> invalidLeftOperandError(l, expr.operator, expr.loc)
            }

            GREATER              -> when (val l = visit(expr.left).fromReference()) {
                is MinimNumber<*> -> when (val r = visit(expr.right).fromReference()) {
                    is MinimNumber<*> -> l grt r

                    else              -> invalidRightOperandError(r, expr.operator, expr.loc)
                }

                else              -> invalidLeftOperandError(l, expr.operator, expr.loc)
            }

            GREATER_EQUAL        -> when (val l = visit(expr.left).fromReference()) {
                is MinimNumber<*> -> when (val r = visit(expr.right).fromReference()) {
                    is MinimNumber<*> -> l geq r

                    else              -> invalidRightOperandError(r, expr.operator, expr.loc)
                }

                else              -> invalidLeftOperandError(l, expr.operator, expr.loc)
            }

            EQUAL                -> when (val l = visit(expr.left).fromReference()) {
                is MinimNumber<*> -> when (val r = visit(expr.right).fromReference()) {
                    is MinimNumber<*> -> l equ r

                    else              -> invalidRightOperandError(r, expr.operator, expr.loc)
                }

                else              -> invalidLeftOperandError(l, expr.operator, expr.loc)
            }

            NOT_EQUAL            -> when (val l = visit(expr.left).fromReference()) {
                is MinimNumber<*> -> when (val r = visit(expr.right).fromReference()) {
                    is MinimNumber<*> -> l neq r

                    else              -> invalidRightOperandError(r, expr.operator, expr.loc)
                }

                else              -> invalidLeftOperandError(l, expr.operator, expr.loc)
            }

            AND                  -> {
                val l = visit(expr.left).fromReference()

                if (l is MinimNumber<*>) {
                    if (!l.toBoolean()) {
                        return when (l) {
                            is MinimNumber.Float -> MinimNumber.Float(0F)
                            is MinimNumber.Int   -> MinimNumber.Int(0)
                        }
                    }
                }
                else {
                    invalidLeftOperandError(l, expr.operator, expr.loc)
                }

                val r = visit(expr.right).fromReference()

                if (r is MinimNumber<*>) {
                    return when (r) {
                        is MinimNumber.Float -> MinimNumber.Float(r.toBoolean().toFloat())
                        is MinimNumber.Int   -> MinimNumber.Int(r.toBoolean().toInt())
                    }
                }
                else {
                    invalidRightOperandError(r, expr.operator, expr.loc)
                }
            }

            OR                   -> {
                val l = visit(expr.left).fromReference()

                if (l is MinimNumber<*>) {
                    if (l.toBoolean()) {
                        return when (l) {
                            is MinimNumber.Float -> MinimNumber.Float(1F)
                            is MinimNumber.Int   -> MinimNumber.Int(1)
                        }
                    }
                }
                else {
                    invalidLeftOperandError(l, expr.operator, expr.loc)
                }

                val r = visit(expr.right).fromReference()

                if (r is MinimNumber<*>) {
                    return when (r) {
                        is MinimNumber.Float -> MinimNumber.Float(r.toBoolean().toFloat())
                        is MinimNumber.Int   -> MinimNumber.Int(r.toBoolean().toInt())
                    }
                }
                else {
                    invalidRightOperandError(r, expr.operator, expr.loc)
                }
            }

            BIT_XOR              -> when (val l = visit(expr.left).fromReference()) {
                is MinimNumber<*> -> when (val r = visit(expr.right).fromReference()) {
                    is MinimNumber<*> -> l xor r

                    else              -> invalidRightOperandError(r, expr.operator, expr.loc)
                }

                else              -> invalidLeftOperandError(l, expr.operator, expr.loc)
            }

            BIT_AND              -> when (val l = visit(expr.left).fromReference()) {
                is MinimNumber<*> -> when (val r = visit(expr.right).fromReference()) {
                    is MinimNumber<*> -> l bnd r

                    is MinimArray     -> MinimArray(mutableListOf(Reference(l)) + r.toMutableList())

                    else              -> invalidRightOperandError(r, expr.operator, expr.loc)
                }

                is MinimArray     -> when (val r = visit(expr.right).fromReference()) {
                    is MinimNumber<*> -> MinimArray(l.toMutableList() + Reference(r))

                    is MinimArray     -> MinimArray(l.toMutableList() + r.toMutableList())

                    else              -> invalidRightOperandError(r, expr.operator, expr.loc)
                }

                else              -> invalidLeftOperandError(l, expr.operator, expr.loc)
            }

            BIT_OR               -> when (val l = visit(expr.left).fromReference()) {
                is MinimNumber<*> -> when (val r = visit(expr.right).fromReference()) {
                    is MinimNumber<*> -> l bor r

                    else              -> invalidRightOperandError(r, expr.operator, expr.loc)
                }

                else              -> invalidLeftOperandError(l, expr.operator, expr.loc)
            }

            SHIFT_LEFT           -> when (val l = visit(expr.left).fromReference()) {
                is MinimNumber<*> -> when (val r = visit(expr.right).fromReference()) {
                    is MinimNumber<*> -> l shl r

                    else              -> invalidRightOperandError(r, expr.operator, expr.loc)
                }

                else              -> invalidLeftOperandError(l, expr.operator, expr.loc)
            }

            SHIFT_RIGHT          -> when (val l = visit(expr.left).fromReference()) {
                is MinimNumber<*> -> when (val r = visit(expr.right).fromReference()) {
                    is MinimNumber<*> -> l shr r

                    else              -> invalidRightOperandError(r, expr.operator, expr.loc)
                }

                else              -> invalidLeftOperandError(l, expr.operator, expr.loc)
            }

            UNSIGNED_SHIFT_RIGHT -> when (val l = visit(expr.left).fromReference()) {
                is MinimNumber<*> -> when (val r = visit(expr.right).fromReference()) {
                    is MinimNumber<*> -> l usr r

                    else              -> invalidRightOperandError(r, expr.operator, expr.loc)
                }

                else              -> invalidLeftOperandError(l, expr.operator, expr.loc)
            }

            ASSIGN               -> when (val l = visit(expr.left)) {
                is Reference  -> when (val r = visit(expr.right).fromReference()) {
                    is MinimNumber<*> -> l.assign(r)

                    is MinimArray     -> l.assign(r[0].value)

                    else              -> invalidRightOperandError(r, expr.operator, expr.loc)
                }

                is MinimArray -> when (val r = visit(expr.right).fromReference()) {
                    is MinimNumber<*> -> l[0].value = r

                    is MinimArray     -> {
                        val minSize = min(l.size, r.size)

                        for (i in 0 until minSize) {
                            l[i].value = r[i].value
                        }
                    }

                    else              -> invalidRightOperandError(r, expr.operator, expr.loc)
                }

                else          -> invalidLeftOperandError(l, expr.operator, expr.loc)
            }
        }
    }

    /**
     * Evaluates the result of one of two [expressions][Expr] based on the truthiness of another.
     *
     * @param expr the expression to evaluate
     *
     * @return the [trueExpr][Expr.Ternary.trueExpr] if [condition][Expr.Ternary.condition] is true, or [falseExpr][Expr.Ternary.falseExpr] otherwise
     */
    override fun visitTernaryExpr(expr: Expr.Ternary) =
        when (val test = visit(expr.condition).fromReference()) {
            is MinimNumber<*> -> if (test.toBoolean())
                visit(expr.trueExpr)
            else
                visit(expr.falseExpr)

            else              -> invalidTernaryConditionError(expr.condition.loc)
        }

    /**
     * Evaluates a number.
     *
     * @param expr the expression to evaluate
     *
     * @return one [MinimNumber]
     */
    override fun visitNumberExpr(expr: Expr.Number) =
        MinimNumber.Float(expr.value)

    /**
     * Evaluates several expressions and wraps them in an array.
     *
     * @param expr the expression to evaluate
     *
     * @return one [MinimArray]
     */
    override fun visitArrayExpr(expr: Expr.Array): Any {
        val elements = mutableListOf<MinimNumber<*>>()

        for (subExpr in expr.elements) {
            elements.add(visit(subExpr).fromReference() as MinimNumber<*>)
        }

        return MinimArray(elements.map { Reference(it) })
    }

    /**
     * Accesses one memory location.
     *
     * @param expr the expression to evaluate
     *
     * @return one [MinimNumber]
     */
    override fun visitSingleExpr(expr: Expr.Single): Any {
        var index =
            (visit(expr.index).fromReference() as? MinimNumber<*> ?: invalidMemoryIndexError(expr.index.loc)).toFloat()

        if (index < 0) {
            index += config.size
        }

        if (index >= config.size) {
            memoryIndexOutOfBoundsError(index, config.size, expr.index.loc)
        }

        return memory.peek()[index.toInt()]
    }

    /**
     * Accesses a fixed range of memory locations.
     *
     * @param expr the expression to evaluate
     *
     * @return one [MinimArray]
     */
    override fun visitFixedRangeExpr(expr: Expr.FixedRange): Any {
        val list = mutableListOf<Reference>()

        var start = when (val e = visit(expr.start).fromReference()) {
            is MinimNumber<*> -> e.toFloat()

            is Unit           -> 0F

            else              -> invalidRangeSubExprError("start", expr.start.loc)
        }

        if (start < 0) {
            start += config.size
        }

        var end = when (val e = visit(expr.end).fromReference()) {
            is MinimNumber<*> -> e.toFloat()

            is Unit           -> memory.peek().size.toFloat()

            else              -> invalidRangeSubExprError("end", expr.end.loc)
        }

        if (end < 0) {
            end += config.size
        }

        val step = when (val e = visit(expr.step).fromReference()) {
            is MinimNumber<*> -> e.toFloat()

            is Unit           -> 1F

            else              -> invalidRangeSubExprError("step", expr.step.loc)
        }

        if (step < 0 && start < end) {
            val temp = start
            start = end - 1
            end = temp - 1
        }

        var i = start

        while ((i - end).sign == -step.sign) {
            list.add(memory.peek()[i.toInt()])

            i += step
        }

        return MinimArray(list)
    }

    /**
     * Accesses a relative range of memory locations.
     *
     * @param expr the expression to evaluate
     *
     * @return one [MinimArray]
     */
    override fun visitRelativeRangeExpr(expr: Expr.RelativeRange): Any {
        val list = mutableListOf<Reference>()

        var start = when (val e = visit(expr.start).fromReference()) {
            is MinimNumber<*> -> e.toFloat()

            is Unit           -> 0F

            else              -> invalidRangeSubExprError("start", expr.start.loc)
        }

        if (start < 0) {
            start += config.size
        }

        val count = when (val e = visit(expr.count).fromReference()) {
            is MinimNumber<*> -> e.toFloat()

            else              -> invalidRangeSubExprError("count", expr.count.loc)
        }

        val step = when (val e = visit(expr.step).fromReference()) {
            is MinimNumber<*> -> e.toFloat()

            is Unit           -> 1F

            else              -> invalidRangeSubExprError("step", expr.step.loc)
        }

        var c = 0
        var i = start

        while (c < count) {
            list.add(memory.peek()[i.toInt()])

            c++
            i += step
        }

        return MinimArray(list)
    }

    /**
     * Evaluates a single [dynamic literal][Expr.DynamicLiteral].
     *
     * @param expr the expression to evaluate
     *
     * @return one [MinimNumber] or [MinimArray]
     */
    override fun visitDynamicLiteralExpr(expr: Expr.DynamicLiteral): Any = when (expr.name) {
        A -> config.args.toMArray()

        C -> counter

        L -> MinimNumber.Int(config.args.length)

        R -> MinimNumber.Float(Math.random().toFloat())

        S -> MinimNumber.Int(config.size)
    }

    /**
     * An empty statement; does nothing.
     *
     * @param stmt the statement to evaluate
     */
    override fun visitNoneStmt(stmt: Stmt.None) = Unit

    /**
     * Receives numerical input from the console and puts it into memory.
     *
     * @param stmt the statement to evaluate
     */
    override fun visitNumberInStmt(stmt: Stmt.NumberIn) {
        val reference = visit(stmt.expr) as? Reference ?: invalidStatementArgumentError(stmt.expr.loc)

        val input = readln().takeIf { it.isNotEmpty() } ?: ""

        reference.value = if (stmt.isIntMode)
            MinimNumber.Int(input.toIntOrNull() ?: invalidNumberInputError(input, stmt.loc))
        else
            MinimNumber.Float(input.toFloatOrNull() ?: invalidNumberInputError(input, stmt.loc))
    }

    /**
     * Prints the result of an [expression][Expr] to the console as a number.
     *
     * @param stmt the statement to evaluate
     */
    override fun visitNumberOutStmt(stmt: Stmt.NumberOut) {
        val number = visit(stmt.expr).fromReference()

        number as? MinimNumber<*> ?: invalidStatementArgumentError(stmt.expr.loc)

        if (stmt.isIntMode) {
            print(BigDecimal("${number.value}").stripTrailingZeros().toPlainString())
        }
        else {
            print(number)
        }
    }

    /**
     * Receives text input from the console, puts the characters into the [input queue][inputQueue], and puts one value for the input queue into memory.
     *
     * @param stmt the statement to evaluate
     */
    override fun visitTextInStmt(stmt: Stmt.TextIn) {
        if (inputQueue.isEmpty()) {
            val input = readln().takeIf { it.isNotEmpty() } ?: return

            if (stmt.isIntMode) {
                inputQueue.addAll(input.map { MinimNumber.Int(it.code) } + MinimNumber.Int())
            }
            else {
                inputQueue.addAll(input.map { MinimNumber.Float(it.code.toFloat()) } + MinimNumber.Float())
            }
        }

        val number = visit(stmt.expr) as? Reference ?: invalidStatementArgumentError(stmt.expr.loc)

        number.value = inputQueue.removeFirst()
    }

    /**
     * Prints the result of an [expression][Expr] to the console as a unicode character.
     *
     * @param stmt the statement to evaluate
     */
    override fun visitTextOutStmt(stmt: Stmt.TextOut) {
        val number = visit(stmt.expr).fromReference() as? MinimNumber<*> ?: invalidStatementArgumentError(stmt.expr.loc)

        print(number.toChar())
    }

    /**
     * Clears the [input queue][inputQueue].
     *
     * @param stmt the statement to evaluate
     */
    override fun visitTextFlushStmt(stmt: Stmt.TextFlush) {
        inputQueue.clear()
    }

    /**
     * Defines a goto label.
     *
     * @param stmt the statement to evaluate
     */
    override fun visitLabelStmt(stmt: Stmt.Label) {
        val id =
            (visit(stmt.id).fromReference() as? MinimNumber<*> ?: invalidStatementArgumentError(stmt.id.loc)).toFloat()

        labels[id] = counter.value.toInt()
    }

    /**
     * Jumps the program to the label with the given [id][Stmt.Goto.id].
     *
     * @param stmt the statement to evaluate
     */
    override fun visitGotoStmt(stmt: Stmt.Goto) {
        val id =
            (visit(stmt.id).fromReference() as? MinimNumber<*> ?: invalidStatementArgumentError(stmt.id.loc)).toFloat()

        counter.set(labels[id] ?: findLabel(id) ?: pos)
    }

    /**
     * Gets the location of the given label [id] if it has not been encountered, and add it to the [label map][labels].
     *
     * @param id the label id to find
     *
     * @return the location within [program] of the given label statement, or `null` otherwise
     */
    private fun findLabel(id: Float): Int? {
        var i = 0

        while (i < program.size) {
            val pos = (i++ + pos) % program.size

            val stmt = program[pos]

            if (stmt is Stmt.Label) {
                val otherID = (visit(stmt.id).fromReference() as? MinimNumber<*>
                        ?: invalidStatementArgumentError(stmt.id.loc)).value

                if (id == otherID) {
                    labels[id] = pos

                    return pos
                }
            }
        }

        return null
    }

    /**
     * Skips the next [statement][Stmt] if the given expression is nonzero.
     *
     * @param stmt the statement to evaluate
     */
    override fun visitJumpStmt(stmt: Stmt.Jump) {
        val condition =
            (visit(stmt.condition).fromReference() as? MinimNumber<*>
                    ?: invalidStatementArgumentError(stmt.condition.loc)).toBoolean()

        if (condition) {
            counter.preIncrement()
        }
    }

    /**
     * Pushes the current [counter] value to the [call stack][callStack], and jumps the program to the label with the given [id][Stmt.Goto.id].
     *
     * @param stmt the statement to evaluate
     */
    override fun visitGosubStmt(stmt: Stmt.Gosub) {
        val id =
            (visit(stmt.id).fromReference() as? MinimNumber<*> ?: invalidStatementArgumentError(stmt.id.loc)).toFloat()

        callStack.push(counter.value.toInt())

        counter.set(labels[id] ?: findLabel(id) ?: pos)
    }

    /**
     * Pops the top value off of the [call stack][callStack], and jumps to that position in the program.
     *
     * @param stmt the statement to evaluate
     */
    override fun visitReturnStmt(stmt: Stmt.Return) {
        if (callStack.isNotEmpty()) {
            val last = callStack.pop()

            counter.set(last)
        }
    }

    /**
     * Puts the result of an [expression][Expr] into the [system input queue][systemInputQueue].
     *
     * @param stmt the statement to evaluate
     */
    override fun visitSystemArgStmt(stmt: Stmt.SystemArg) {
        when (val expr = visit(stmt.expr).fromReference()) {
            is MinimNumber<*> -> systemInputQueue.add(expr.toFloat())

            is MinimArray     -> for (ref in expr) {
                systemInputQueue.add(ref.value.toFloat())
            }

            else              -> invalidStatementArgumentError(stmt.expr.loc)
        }
    }

    /**
     * If the [system input queue][systemInputQueue] is not empty, it takes the first value from the queue, scans memory from that location for the name of the [system function][Library.Function] to call, takes the necessary amount of arguments from the input queue, invokes the function, and puts the results of the function into the [system output queue][systemOutputQueue].
     *
     * If the system output queue is not empty, it takes one value from the queue and puts it into memory.
     *
     * @param stmt the statement to evaluate
     */
    override fun visitSystemYieldStmt(stmt: Stmt.SystemYield) {
        if (systemInputQueue.isNotEmpty()) {
            val start = systemInputQueue.removeFirst().toInt()

            val commandName = memory.peek()!!.scanString(start)

            val command = Library[commandName] ?: undefinedCommandError(commandName, stmt.loc)

            val args = mutableListOf<Float>()

            for (i in 0 until command.arity) {
                args.add(systemInputQueue.removeFirst())
            }

            val result = command(this, args.toFloatArray())

            systemOutputQueue.addAll(result.toList())
        }

        if (systemOutputQueue.isNotEmpty()) {
            val number = visit(stmt.expr) as? Reference ?: invalidStatementArgumentError(stmt.expr.loc)

            val value = systemOutputQueue.removeFirst()

            number.value = if (stmt.isIntMode)
                MinimNumber.Int(value.toInt())
            else
                MinimNumber.Float(value)
        }
    }

    /**
     * Behaves the same as the [system yield statement][visitSystemYieldStmt], but does not put any [system function][Library.Function] results into the [system output queue][systemOutputQueue].
     *
     * @param stmt the statement to evaluate
     */
    override fun visitSystemCallStmt(stmt: Stmt.SystemCall) {
        if (systemInputQueue.isNotEmpty()) {
            val start = systemInputQueue.removeFirst().toInt()

            val commandName = memory.peek()!!.scanString(start)

            val command = Library[commandName] ?: undefinedCommandError(commandName, stmt.loc)

            val args = mutableListOf<Float>()

            for (i in 0 until command.arity) {
                args.add(systemInputQueue.removeFirst())
            }

            command(this, args.toFloatArray())
        }
    }

    /**
     * Clears the [system input queue][systemInputQueue] and [system output queue][systemOutputQueue].
     *
     * @param stmt the statement to evaluate
     */
    override fun visitSystemFlushStmt(stmt: Stmt.SystemFlush) {
        systemInputQueue.clear()
        systemOutputQueue.clear()
    }

    /**
     * Pushes a clean memory scope to the [memory stack][memory].
     *
     * @param stmt the statement to evaluate
     */
    override fun visitMemoryPushStmt(stmt: Stmt.MemoryPush) {
        memory.push(MinimArray(config.size))
    }

    /**
     * Pops the current memory scope from the [memory stack][memory].
     *
     * @param stmt the statement to evaluate
     */
    override fun visitMemoryPopStmt(stmt: Stmt.MemoryPop) {
        if (memory.size > 1) {
            memory.pop()
        }
    }

    /**
     * Puts the result of an [expression][Expr] into the [memory queue][memoryQueue].
     *
     * @param stmt the statement to evaluate
     */
    override fun visitMemoryOutStmt(stmt: Stmt.MemoryOut) {
        memoryQueue.add(
            visit(stmt.expr).fromReference() as? MinimNumber<*> ?: invalidStatementArgumentError(stmt.expr.loc)
        )
    }

    /**
     * Puts a single value from the [memory queue][memoryQueue] into memory.
     *
     * @param stmt the statement to evaluate
     */
    override fun visitMemoryInStmt(stmt: Stmt.MemoryIn) {
        val reference = visit(stmt.expr) as? Reference ?: invalidStatementArgumentError(stmt.expr.loc)

        reference.value = memoryQueue.removeFirst()
    }

    /**
     * Clears the [memory queue][memoryQueue].
     *
     * @param stmt the statement to evaluate
     */
    override fun visitMemoryFlushStmt(stmt: Stmt.MemoryFlush) {
        memoryQueue.clear()
    }

    /**
     * Assigns a single memory location.
     *
     * @param stmt the statement to evaluate
     */
    override fun visitSingleAssignStmt(stmt: Stmt.SingleAssign) {
        var index =
            (visit(stmt.single.index).fromReference() as? MinimNumber<*>
                    ?: invalidMemoryIndexError(stmt.single.index.loc)).toInt()

        if (index < 0) {
            index += config.size
        }

        when (val expr = visit(stmt.expr).fromReference()) {
            is MinimNumber<*> -> memory.peek()[index].value = expr

            is MinimArray     -> memory.peek()[index].value = expr[0].value
        }
    }

    /**
     * Assigns a fixed range of memory locations.
     *
     * @param stmt the statement to evaluate
     */
    override fun visitFixedRangeAssignStmt(stmt: Stmt.FixedRangeAssign) {
        var start = when (val e = visit(stmt.range.start).fromReference()) {
            is MinimNumber<*> -> e.toFloat()

            is Unit           -> 0F

            else              -> invalidRangeSubExprError("start", stmt.range.start.loc)
        }

        if (start < 0) {
            start += config.size
        }

        var end = when (val e = visit(stmt.range.end).fromReference()) {
            is MinimNumber<*> -> e.toFloat()

            is Unit           -> memory.peek().size.toFloat()

            else              -> invalidRangeSubExprError("end", stmt.range.end.loc)
        }

        if (end < 0) {
            end += config.size
        }

        val step = when (val e = visit(stmt.range.step).fromReference()) {
            is MinimNumber<*> -> e.toFloat()

            is Unit           -> 1F

            else              -> invalidRangeSubExprError("step", stmt.range.step.loc)
        }

        if (step < 0 && start < end) {
            val temp = start
            start = end - 1
            end = temp - 1
        }

        when (val expr = visit(stmt.expr).fromReference()) {
            is MinimNumber<*> -> {
                var memoryIndex = start

                while ((memoryIndex - end).sign == -step.sign) {
                    memory.peek()[memoryIndex.toInt()].value = expr

                    memoryIndex += step
                }
            }

            is MinimArray     -> {
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

    /**
     * Assigns a relative range of memory locations.
     *
     * @param stmt the statement to evaluate
     */
    override fun visitRelativeRangeAssignStmt(stmt: Stmt.RelativeRangeAssign) {
        var start = when (val e = visit(stmt.range.start).fromReference()) {
            is MinimNumber<*> -> e.toFloat()

            is Unit           -> 0F

            else              -> invalidRangeSubExprError("start", stmt.range.start.loc)
        }

        if (start < 0) {
            start += config.size
        }

        val count = when (val expr = visit(stmt.range.count).fromReference()) {
            is MinimNumber<*> -> expr.toFloat()

            else              -> invalidRangeSubExprError("count", stmt.range.count.loc)
        }

        val step = when (val expr = visit(stmt.range.step).fromReference()) {
            is MinimNumber<*> -> expr.toFloat()

            is Unit           -> 1F

            else              -> invalidRangeSubExprError("step", stmt.range.step.loc)
        }

        when (val expr = visit(stmt.expr).fromReference()) {
            is MinimNumber<*> -> {
                var memoryIndex = start

                while (memoryIndex < start + count) {
                    memory.peek()[memoryIndex.toInt()].value = expr

                    memoryIndex += step
                }
            }

            is MinimArray     -> {
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

    /**
     * Evaluates an [expression][Expr].
     *
     * @param stmt the statement to evaluate
     */
    override fun visitExpressionStmt(stmt: Stmt.Expression) {
        visit(stmt.expr)
    }
}
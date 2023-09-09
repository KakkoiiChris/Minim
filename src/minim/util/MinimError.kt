package minim.util

import minim.lexer.Lexer
import minim.lexer.Location
import minim.lexer.Type
import minim.parser.Expr
import minim.parser.Parser
import minim.runtime.Runtime

/**
 *The base for all interpreter errors.
 *
 * @param stage the stage at which this error occurred
 * @param msg the sub-message for this error
 * @param loc the location where this error occurred
 */
class MinimError(stage: Stage, msg: String, loc: Location) : Throwable("${stage.name} Error $loc :: $msg") {
    /**
     * Enumeration of stages where [MinimError]s can be thrown.
     */
    enum class Stage { Lexer, Parser, Runtime }
}

/**
 * Any error thrown by the [Lexer].
 *
 * @param msg the sub-message for this error
 * @param loc the location where this error occurred
 */
private fun lexerError(msg: String, loc: Location): Nothing =
    throw MinimError(MinimError.Stage.Lexer, msg, loc)

/**
 * Thrown by the [Lexer] when it encounters an invalid character.
 *
 * @param invalid the invalid character
 * @param loc the location where this error occurred
 */
fun invalidCharError(invalid: Char, loc: Location): Nothing =
    lexerError("Character '$invalid' is invalid!", loc)

/**
 * Thrown by the [Lexer] when it encounters an invalid escape character.
 *
 * @param invalid the invalid escape character
 * @param loc the location where this error occurred
 */
fun invalidEscapeError(invalid: Char, loc: Location): Nothing =
    lexerError("Character escape '\\$invalid' is invalid!", loc)

/**
 * Thrown by the [Lexer] when it encounters an invalid number literal.
 *
 * @param invalid the invalid number literal
 * @param loc the location where this error occurred
 */
fun invalidNumberError(invalid: String, loc: Location): Nothing =
    lexerError("Number '$invalid' is invalid!", loc)

/**
 * Any error thrown by the [Parser].
 *
 * @param msg the sub-message for this error
 * @param loc the location where this error occurred
 */
private fun parserError(msg: String, loc: Location): Nothing =
    throw MinimError(MinimError.Stage.Parser, msg, loc)

/**
 * Thrown by the [Parser] when it parses an array literal inside another array literal.
 *
 * @param loc the location where this error occurred
 */
fun invalidArrayElementError(loc: Location): Nothing =
    parserError("Array elements must be single values!", loc)

/**
 * Thrown by the [Parser] when the statement header has an invalid character.
 *
 * @param invalid the invalid statement header
 * @param loc the location where this error occurred
 */
fun invalidStatementHeaderError(invalid: String, loc: Location): Nothing =
    parserError("Statement header '$invalid' is invalid!", loc)

/**
 * Thrown by the [Parser] encounters an invalid character for a terminal expression.
 *
 * @param invalid the invalid token type
 * @param loc the location where this error occurred
 */
fun invalidTerminalError(invalid: Type, loc: Location): Nothing =
    parserError("Terminal beginning with '${invalid.rep}' is invalid!", loc)

/**
 * Thrown when the [Parser] does not encounter an expression for the relative range count.
 *
 * @param loc the location where this error occurred
 */
fun noRelativeRangeCountError(loc: Location): Nothing =
    parserError("Relative range count cannot be omitted!", loc)

/**
 * Thrown by the [Parser] when it encounters the wrong token type.
 *
 * @param invalid the invalid token type
 * @param expected the expected token type
 * @param loc the location where this error occurred
 */
fun unexpectedTypeError(invalid: Type, expected: Type, loc: Location): Nothing =
    parserError("Type '${invalid.rep}' is invalid; expected '${expected.rep}'!", loc)

/**
 * Any error thrown by the [Runtime].
 *
 * @param msg the sub-message for this error
 * @param loc the location where this error occurred
 */
private fun runtimeError(msg: String, loc: Location): Nothing =
    throw MinimError(MinimError.Stage.Runtime, msg, loc)

/**
 * Thrown by the [Runtime] when the left operand of a binary operator is invalid.
 *
 * @param invalid the invalid operand
 * @param operator the operator for the expression
 * @param loc the location where this error occurred
 */
fun invalidLeftOperandError(invalid: Any, operator: Expr.Binary.Operator, loc: Location): Nothing =
    runtimeError("Left operand '$invalid' for '$operator' operator is invalid!", loc)

/**
 * Thrown by the [Runtime] when any memory index is not a number.
 *
 * @param loc the location where this error occurred
 */
fun invalidMemoryIndexError(loc: Location): Nothing =
    runtimeError("Memory index must be a number!", loc)

/**
 * Thrown by the [Runtime] when the numeric input statement isn't a valid number.
 *
 * @param invalid the invalid input
 * @param loc the location where this error occurred
 */
fun invalidNumberInputError(invalid: String, loc: Location): Nothing =
    runtimeError("Input '$invalid' does not conform to a number!", loc)

/**
 * Thrown by the [Runtime] when the operand of a postfix operator is invalid.
 *
 * @param invalid the invalid operand
 * @param operator the operator for the expression
 * @param loc the location where this error occurred
 */
fun invalidPostfixOperandError(invalid: Any, operator: Expr.Postfix.Operator, loc: Location): Nothing =
    runtimeError("Operand '$invalid' for postfix '$operator' operator is invalid!", loc)

/**
 * Thrown by the [Runtime] when the operand of a prefix operator is invalid.
 *
 * @param invalid the invalid operand
 * @param operator the operator for the expression
 * @param loc the location where this error occurred
 */
fun invalidPrefixOperandError(invalid: Any, operator: Expr.Prefix.Operator, loc: Location): Nothing =
    runtimeError("Operand '$invalid' for prefix '$operator' operator is invalid!", loc)

/**
 * Thrown by the [Runtime] when any range subexpression is not a number.
 *
 * @param name the name of the invalid range subexpression
 * @param loc the location where this error occurred
 */
fun invalidRangeSubExprError(name: String, loc: Location): Nothing =
    runtimeError("Range '$name' expression must be a number!", loc)

/**
 * Thrown by the [Runtime] when the right operand of a binary operator is invalid.
 *
 * @param invalid the invalid operand
 * @param operator the operator for the expression
 * @param loc the location where this error occurred
 */
fun invalidRightOperandError(invalid: Any, operator: Expr.Binary.Operator, loc: Location): Nothing =
    runtimeError("Right operand '$invalid' for '$operator' operator is invalid!", loc)

/**
 * Thrown by the [Runtime] when any statement argument is not a number.
 *
 * @param loc the location where this error occurred
 */
fun invalidStatementArgumentError(loc: Location): Nothing =
    runtimeError("Statement argument must be a number!", loc)

/**
 * Thrown by the [Runtime] when the condition of a ternary expression is not a number.
 *
 * @param loc the location where this error occurred
 */
fun invalidTernaryConditionError(loc: Location): Nothing =
    runtimeError("Test expression must be a number!", loc)

/**
 * Thrown by the [Runtime] when the memory index being accessed is greater than memory size.
 *
 * @param index the index accessed
 * @param size the size of the memory scope
 * @param loc the location where this error occurred
 */
fun memoryIndexOutOfBoundsError(index: Float, size: Int, loc: Location): Nothing =
    runtimeError("Memory index '$index' is in excess of memory size '$size'!", loc)

/**
 * Thrown by the [Runtime] when no system function of a given name exists.
 *
 * @param name the name of the undefined system function
 * @param loc the location where this error occurred
 */
fun undefinedCommandError(name: String, loc: Location): Nothing =
    runtimeError("System command '$name' is not defined!", loc)

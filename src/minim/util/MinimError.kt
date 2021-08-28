package minim.util

import minim.lexer.Location
import minim.lexer.Token
import minim.parser.Expr

class MinimError(stage: Stage, msg: String, loc: Location) : Throwable("${stage.name} Error @ $loc :: $msg") {
    enum class Stage { Lexer, Parser, Runtime }
}

fun lexerError(msg: String, loc: Location): Nothing =
    throw MinimError(MinimError.Stage.Lexer, msg, loc)

fun invalidCharError(char: Char, loc: Location): Nothing =
    lexerError("Character '$char' is invalid!", loc)

fun invalidEscapeError(escape: Char, loc: Location): Nothing =
    lexerError("Character escape '\\$escape' is invalid!", loc)

fun invalidNumberError(literal: String, loc: Location): Nothing =
    lexerError("Number '$literal' is invalid!", loc)

fun parserError(msg: String, loc: Location): Nothing =
    throw MinimError(MinimError.Stage.Parser, msg, loc)

fun noRelativeRangeCountError(loc: Location): Nothing =
    parserError("Relative range count cannot be omitted!", loc)

fun invalidArrayElementError(loc: Location): Nothing =
    parserError("Array elements must be single values!", loc)

fun invalidStatementHeaderError(header: String, loc: Location): Nothing =
    parserError("Statement header '$header' is invalid!", loc)

fun invalidTerminalError(invalid: Token.Type, loc: Location): Nothing =
    parserError("Terminal beginning with '$invalid' is invalid!", loc)

fun invalidTypeError(invalid: Token.Type, expected: Token.Type, loc: Location): Nothing =
    parserError("Type '$invalid' is invalid; expected '$expected'!", loc)

fun runtimeError(msg: String, loc: Location): Nothing =
    throw MinimError(MinimError.Stage.Runtime, msg, loc)

fun invalidLeftOperandError(operand: Any, operator: Expr.Binary.Operator, loc: Location): Nothing =
    runtimeError("Left operand '$operand' for '$operator' operator is invalid!", loc)

fun invalidMemoryIndexError(loc: Location): Nothing =
    runtimeError("Memory index must be a number!", loc)

fun invalidNumericalInputError(input: String, loc: Location): Nothing =
    runtimeError("Input '$input' does not conform to a number!", loc)

fun invalidRangeExprError(name: String, loc: Location): Nothing =
    runtimeError("Range $name expression must yield a number!", loc)

fun invalidRightOperandError(operand: Any, operator: Expr.Binary.Operator, loc: Location): Nothing =
    runtimeError("Right operand '$operand' for '$operator' operator is invalid!", loc)

fun invalidTestExprError(loc: Location): Nothing =
    runtimeError("Test expression must be a boolean!", loc)

fun invalidPrefixOperandError(operand: Any, operator: Expr.Prefix.Operator, loc: Location): Nothing =
    runtimeError("Operand '$operand' for prefix '$operator' operator is invalid!", loc)

fun invalidPostfixOperandError(operand: Any, operator: Expr.Postfix.Operator, loc: Location): Nothing =
    runtimeError("Operand '$operand' for postfix '$operator' operator is invalid!", loc)

fun invalidStatementArgumentError(loc: Location): Nothing =
    runtimeError("Statement argument must be a single number!", loc)

fun undefinedCommandError(name: String, loc: Location): Nothing =
    runtimeError("System command '$name' is not defined!", loc)

fun undefinedLabelError(id: Float, loc: Location): Nothing =
    runtimeError("Label $id/'${id.toInt().toChar().slashify()}' is undefined!", loc)
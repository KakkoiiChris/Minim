package minim.util

import minim.lexer.Location
import minim.lexer.Token

class MinimError(stage: Stage, msg: String, loc: Location) : Throwable("ERROR @ ${stage.name}: $msg @ $loc"){
    enum class Stage {
        GENERAL,
        LEXER,
        PARSER,
        RUNTIME
    }
}

fun generalError(msg: String, loc: Location): Nothing =
    throw MinimError(MinimError.Stage.GENERAL, msg, loc)

fun unexpectedError(msg: String): Nothing =
    generalError(msg, Location.none)

fun lexerError(msg: String, loc: Location): Nothing =
    throw MinimError(MinimError.Stage.LEXER, msg, loc)

fun invalidCharError(char: Char, loc: Location): Nothing =
    lexerError("Character '$char' is invalid!", loc)

fun invalidEscapeError(escape: Char, loc: Location): Nothing =
    lexerError("Character escape '\\$escape' is invalid!", loc)

fun invalidNumberError(literal: String, loc: Location): Nothing =
    lexerError("Number '$literal' is invalid!", loc)

fun parserError(msg: String, loc: Location): Nothing =
    throw MinimError(MinimError.Stage.PARSER, msg, loc)

fun invalidArrayElementError(loc: Location): Nothing =
    parserError("Array elements must be single values!", loc)

fun invalidTerminalError(invalid: Token.Type, loc: Location): Nothing =
    parserError("Terminal beginning with '$invalid' is invalid!", loc)

fun invalidTypeError(invalid: Token.Type, expected: Token.Type, loc: Location): Nothing =
    parserError("Type '$invalid' is invalid; expected '$expected'!", loc)

fun runtimeError(msg: String, loc: Location): Nothing =
    throw MinimError(MinimError.Stage.RUNTIME, msg, loc)

fun invalidLeftOperandError(operand: Any, op: Token.Type, loc: Location): Nothing =
    runtimeError("Left operand '$operand' for binary operator '$op' is invalid!", loc)

fun invalidMemoryIndexError(loc: Location):Nothing=
    runtimeError("Memory index must be a number!", loc)

fun invalidRightOperandError(operand: Any, op: Token.Type, loc: Location): Nothing =
    runtimeError("Right operand '$operand' for binary operator '$op' is invalid!", loc)

fun invalidTestExprError(loc: Location): Nothing =
    runtimeError("Test expression must be a boolean!", loc)

fun invalidUnaryOperandError(operand: Any, op: Token.Type, loc: Location): Nothing =
    runtimeError("Operand '$operand' for unary operator '$op' is invalid!", loc)

fun undefinedLabelError(id: Int, loc: Location): Nothing =
    runtimeError("Label $id/'${id.toChar()}' is undefined!", loc)
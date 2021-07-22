package minim.runtime

import minim.util.toBoolean
import minim.util.toFloat
import java.math.BigDecimal

class MNumber(var value: Float = 0F) {
    operator fun unaryMinus() =
        MNumber(-value)
    
    operator fun not() =
        MNumber((!value.toBoolean()).toFloat())
    
    fun narrow() =
        MNumber(value.toBoolean().toFloat())
    
    fun inv() =
        MNumber(value.toInt().inv().toFloat())
    
    operator fun plus(x: MNumber) =
        MNumber(value + x.value)
    
    operator fun minus(x: MNumber) =
        MNumber(value - x.value)
    
    operator fun times(x: MNumber) =
        MNumber(value * x.value)
    
    operator fun div(x: MNumber) =
        MNumber(value / x.value)
    
    operator fun rem(x: MNumber) =
        MNumber(value % x.value)
    
    infix fun lss(x: MNumber) =
        MNumber((value < x.value).toFloat())
    
    infix fun leq(x: MNumber) =
        MNumber((value <= x.value).toFloat())
    
    infix fun grt(x: MNumber) =
        MNumber((value > x.value).toFloat())
    
    infix fun geq(x: MNumber) =
        MNumber((value >= x.value).toFloat())
    
    infix fun equ(x: MNumber) =
        MNumber((value == x.value).toFloat())
    
    infix fun neq(x: MNumber) =
        MNumber((value != x.value).toFloat())
    
    fun toChar() =
        value.toInt().toChar()
    
    override fun toString(): String =
        BigDecimal("$value").stripTrailingZeros().toPlainString()
}
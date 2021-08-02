package minim.runtime

import minim.util.toBoolean
import minim.util.toFloat
import minim.util.toInt
import java.math.BigDecimal

sealed interface MNumber {
    val value: Any
    
    operator fun unaryMinus(): MNumber
    
    operator fun not(): MNumber
    
    fun narrow(): MNumber
    
    fun inv(): MNumber
    
    operator fun plus(x: MNumber): MNumber
    
    operator fun minus(x: MNumber): MNumber
    
    operator fun times(x: MNumber): MNumber
    
    operator fun div(x: MNumber): MNumber
    
    operator fun rem(x: MNumber): MNumber
    
    infix fun lss(x: MNumber): MNumber
    
    infix fun leq(x: MNumber): MNumber
    
    infix fun grt(x: MNumber): MNumber
    
    infix fun geq(x: MNumber): MNumber
    
    infix fun equ(x: MNumber): MNumber
    
    infix fun neq(x: MNumber): MNumber
    
    infix fun bnd(x: MNumber): MNumber
    
    infix fun xor(x: MNumber): MNumber
    
    infix fun bor(x: MNumber): MNumber
    
    infix fun and(x: MNumber): MNumber
    
    infix fun orr(x: MNumber): MNumber
    
    infix fun shl(x: MNumber): MNumber
    
    infix fun shr(x: MNumber): MNumber
    
    infix fun usr(x: MNumber): MNumber
    
    fun toBoolean(): Boolean
    
    fun toChar(): Char
    
    fun toFloat(): kotlin.Float
    
    fun toInt(): kotlin.Int
    
    class Float(override val value: kotlin.Float = 0F) : MNumber {
        override operator fun unaryMinus() =
            Float(-value)
        
        override operator fun not() =
            Float((!value.toBoolean()).toFloat())
        
        override fun narrow() =
            Float(value.toBoolean().toFloat())
        
        override fun inv() =
            Float(value.toInt().inv().toFloat())
        
        override operator fun plus(x: MNumber) = when (x) {
            is Float -> Float(value + x.value)
            is Int   -> Float(value + x.value)
        }
        
        override operator fun minus(x: MNumber) = when (x) {
            is Float -> Float(value - x.value)
            is Int   -> Float(value - x.value)
        }
        
        override operator fun times(x: MNumber) = when (x) {
            is Float -> Float(value * x.value)
            is Int   -> Float(value * x.value)
        }
        
        override operator fun div(x: MNumber) = when (x) {
            is Float -> Float(value / x.value)
            is Int   -> Float(value / x.value)
        }
        
        override operator fun rem(x: MNumber) = when (x) {
            is Float -> Float(value % x.value)
            is Int   -> Float(value % x.value)
        }
        
        override infix fun lss(x: MNumber) = when (x) {
            is Float -> Float((value < x.value).toFloat())
            is Int   -> Float((value < x.value).toFloat())
        }
        
        override infix fun leq(x: MNumber) = when (x) {
            is Float -> Float((value <= x.value).toFloat())
            is Int   -> Float((value <= x.value).toFloat())
        }
        
        override infix fun grt(x: MNumber) = when (x) {
            is Float -> Float((value > x.value).toFloat())
            is Int   -> Float((value > x.value).toFloat())
        }
        
        override infix fun geq(x: MNumber) = when (x) {
            is Float -> Float((value >= x.value).toFloat())
            is Int   -> Float((value >= x.value).toFloat())
        }
        
        override infix fun equ(x: MNumber) = when (x) {
            is Float -> Float((value == x.value).toFloat())
            is Int   -> Float((value == x.value.toFloat()).toFloat())
        }
        
        override infix fun neq(x: MNumber) = when (x) {
            is Float -> Float((value != x.value).toFloat())
            is Int   -> Float((value != x.value.toFloat()).toFloat())
        }
        
        override fun bnd(x: MNumber) = when (x) {
            is Float -> Float((value.toInt() and x.value.toInt()).toFloat())
            is Int   -> Float((value.toInt() and x.value).toFloat())
        }
        
        override fun xor(x: MNumber) = when (x) {
            is Float -> Float((value.toInt() xor x.value.toInt()).toFloat())
            is Int   -> Float((value.toInt() xor x.value).toFloat())
        }
        
        override fun bor(x: MNumber) = when (x) {
            is Float -> Float((value.toInt() or x.value.toInt()).toFloat())
            is Int   -> Float((value.toInt() or x.value).toFloat())
        }
        
        override fun and(x: MNumber) = when (x) {
            is Float -> Float((value.toBoolean() && x.value.toBoolean()).toFloat())
            is Int   -> Float((value.toBoolean() && x.value.toBoolean()).toFloat())
        }
        
        override fun orr(x: MNumber) = when (x) {
            is Float -> Float((value.toBoolean() || x.value.toBoolean()).toFloat())
            is Int   -> Float((value.toBoolean() || x.value.toBoolean()).toFloat())
        }
        
        override infix fun shl(x: MNumber) = when (x) {
            is Float -> Float((value.toInt() shl x.value.toInt()).toFloat())
            is Int   -> Float((value.toInt() shl x.value).toFloat())
        }
        
        override infix fun shr(x: MNumber) = when (x) {
            is Float -> Float((value.toInt() shr x.value.toInt()).toFloat())
            is Int   -> Float((value.toInt() shr x.value).toFloat())
        }
        
        override infix fun usr(x: MNumber) = when (x) {
            is Float -> Float((value.toInt() ushr x.value.toInt()).toFloat())
            is Int   -> Float((value.toInt() ushr x.value).toFloat())
        }
        
        override fun toBoolean() =
            value.toBoolean()
        
        override fun toChar() =
            value.toInt().toChar()
        
        override fun toFloat() =
            value
        
        override fun toInt() =
            value.toInt()
        
        override fun toString(): String =
            BigDecimal("$value").stripTrailingZeros().toPlainString()
    }
    
    class Int(override val value: kotlin.Int = 0) : MNumber {
        override operator fun unaryMinus() =
            Int(-value)
        
        override operator fun not() =
            Int((!value.toBoolean()).toInt())
        
        override fun narrow() =
            Int(value.toBoolean().toInt())
        
        override fun inv() =
            Int(value.inv())
        
        override operator fun plus(x: MNumber) = when (x) {
            is Float -> Int(value + x.value.toInt())
            is Int   -> Int(value + x.value)
        }
        
        override operator fun minus(x: MNumber) = when (x) {
            is Float -> Int(value - x.value.toInt())
            is Int   -> Int(value - x.value)
        }
        
        override operator fun times(x: MNumber) = when (x) {
            is Float -> Int(value * x.value.toInt())
            is Int   -> Int(value * x.value)
        }
        
        override operator fun div(x: MNumber) = when (x) {
            is Float -> Int(value / x.value.toInt())
            is Int   -> Int(value / x.value)
        }
        
        override operator fun rem(x: MNumber) = when (x) {
            is Float -> Int(value % x.value.toInt())
            is Int   -> Int(value % x.value)
        }
        
        override infix fun lss(x: MNumber) = when (x) {
            is Float -> Int((value < x.value).toInt())
            is Int   -> Int((value < x.value).toInt())
        }
        
        override infix fun leq(x: MNumber) = when (x) {
            is Float -> Int((value <= x.value).toInt())
            is Int   -> Int((value <= x.value).toInt())
        }
        
        override infix fun grt(x: MNumber) = when (x) {
            is Float -> Int((value > x.value).toInt())
            is Int   -> Int((value > x.value).toInt())
        }
        
        override infix fun geq(x: MNumber) = when (x) {
            is Float -> Int((value >= x.value).toInt())
            is Int   -> Int((value >= x.value).toInt())
        }
        
        override infix fun equ(x: MNumber) = when (x) {
            is Float -> Int((value == x.value.toInt()).toInt())
            is Int   -> Int((value == x.value).toInt())
        }
        
        override infix fun neq(x: MNumber) = when (x) {
            is Float -> Int((value != x.value.toInt()).toInt())
            is Int   -> Int((value != x.value).toInt())
        }
        
        override fun bnd(x: MNumber) = when (x) {
            is Float -> Int((value and x.value.toInt()))
            is Int   -> Int(value and x.value)
        }
        
        override fun xor(x: MNumber) = when (x) {
            is Float -> Int((value xor x.value.toInt()))
            is Int   -> Int(value xor x.value)
        }
        
        override fun bor(x: MNumber) = when (x) {
            is Float -> Int((value or x.value.toInt()))
            is Int   -> Int(value or x.value)
        }
        
        override fun and(x: MNumber) = when (x) {
            is Float -> Int((value.toBoolean() && x.value.toBoolean()).toInt())
            is Int   -> Int((value.toBoolean() && x.value.toBoolean()).toInt())
        }
        
        override fun orr(x: MNumber) = when (x) {
            is Float -> Int((value.toBoolean() || x.value.toBoolean()).toInt())
            is Int   -> Int((value.toBoolean() || x.value.toBoolean()).toInt())
        }
        
        override infix fun shl(x: MNumber) = when (x) {
            is Float -> Int(value shl x.value.toInt())
            is Int   -> Int(value shl x.value)
        }
        
        override infix fun shr(x: MNumber) = when (x) {
            is Float -> Int(value shr x.value.toInt())
            is Int   -> Int(value shr x.value)
        }
        
        override infix fun usr(x: MNumber) = when (x) {
            is Float -> Int(value ushr x.value.toInt())
            is Int   -> Int(value ushr x.value)
        }
        
        override fun toBoolean() =
            value.toBoolean()
        
        override fun toChar() =
            value.toChar()
        
        override fun toFloat() =
            value.toFloat()
        
        override fun toInt() =
            value
        
        override fun toString() =
            value.toString()
    }
}
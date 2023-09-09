package minim.runtime

import minim.runtime.MinimNumber.Float
import minim.runtime.MinimNumber.Int
import minim.util.toBoolean
import minim.util.toFloat
import minim.util.toInt

/**
 * An interface whose implementations act as a sum type for a [Float] or [Int] value.
 */
sealed interface MinimNumber<X> {
    /**
     * Gets the internal value of this instance.
     */
    val value: X
    
    /**
     * Gets the negative value of this number.
     *
     * @return ex. - 5 -> -5
     */
    operator fun unaryMinus(): MinimNumber<*>
    
    /**
     * Gets the logical opposite value of this number.
     *
     * @return ex. ! 5 -> 0
     */
    operator fun not(): MinimNumber<*>
    
    /**
     * Gets the logical identity (truthiness) value of this number.
     *
     * @return ex. ? 5 -> 1
     */
    fun narrow(): MinimNumber<*>
    
    /**
     * Gets the bitwise inversion value of this number.
     *
     * @return ex. ~ 5 -> -6
     */
    fun inv(): MinimNumber<*>
    
    /**
     * Gets the value of this number plus x.
     *
     * @return ex. 5 + 7 -> 12
     */
    operator fun plus(x: MinimNumber<*>): MinimNumber<*>
    
    /**
     * Gets the value of this number minus x.
     *
     * @return ex. 5 - 7 -> -2
     */
    operator fun minus(x: MinimNumber<*>): MinimNumber<*>
    
    /**
     * Gets the value of this number times x.
     *
     * @return ex. 5 * 7 -> 35
     */
    operator fun times(x: MinimNumber<*>): MinimNumber<*>
    
    /**
     * Gets the value of this number divided by x.
     *
     * @return ex. 5 / 7 -> 0, 5.0 / 7.0 -> 0.71428573
     */
    operator fun div(x: MinimNumber<*>): MinimNumber<*>
    
    /**
     * Gets the value of this number modulo x.
     *
     * @return ex. 5 % 7 -> 5
     */
    operator fun rem(x: MinimNumber<*>): MinimNumber<*>
    
    /**
     * Gets the truthy value of this number less than x.
     *
     * @return ex. 5 < 7 -> 1
     */
    infix fun lss(x: MinimNumber<*>): MinimNumber<*>
    
    /**
     * Gets the truthy value of this number less than or equal to x.
     *
     * @return ex. 5 <= 7 -> 1
     */
    infix fun leq(x: MinimNumber<*>): MinimNumber<*>
    
    /**
     * Gets the truthy value of this number greater than x.
     *
     * @return ex. 5 > 7 -> 0
     */
    infix fun grt(x: MinimNumber<*>): MinimNumber<*>
    
    /**
     * Gets the truthy value of this number greater than or equal to x.
     *
     * @return ex. 5 >= 7 -> 0
     */
    infix fun geq(x: MinimNumber<*>): MinimNumber<*>
    
    /**
     * Gets the truthy value of this number equal to x.
     *
     * @return ex. 5 == 7 -> 0
     */
    infix fun equ(x: MinimNumber<*>): MinimNumber<*>
    
    /**
     * Gets the truthy value of this number not equal to x.
     *
     * @return ex. 5 <> 7 -> 1
     */
    infix fun neq(x: MinimNumber<*>): MinimNumber<*>
    
    /**
     * Gets the value of and-ing the bits of this number and x.
     *
     * @return ex. 5 & 7 -> 5
     */
    infix fun bnd(x: MinimNumber<*>): MinimNumber<*>
    
    /**
     * Gets the value of exclusive-or-ing the bits of this number and x.
     *
     * @return ex. 5 ^ 7 -> 2
     */
    infix fun xor(x: MinimNumber<*>): MinimNumber<*>
    
    /**
     * Gets the value of or-ing the bits of this number and x.
     *
     * @return ex. 5 | 7 -> 7
     */
    infix fun bor(x: MinimNumber<*>): MinimNumber<*>
    
    /**
     * Gets the value of left shifting the bits of this number by x places.
     *
     * @return ex. 5 << 7 -> 640
     */
    infix fun shl(x: MinimNumber<*>): MinimNumber<*>
    
    /**
     * Gets the value of right shifting the bits of this number by x places.
     *
     * @return ex. 5 >> 7 -> 0
     */
    infix fun shr(x: MinimNumber<*>): MinimNumber<*>
    
    /**
     * Gets the value of unsigned right shifting the bits of this number by x places.
     *
     * @return ex. 5 >>> 7 -> 0
     */
    infix fun usr(x: MinimNumber<*>): MinimNumber<*>
    
    /**
     * Gets a boolean value based on the truthiness of this number.
     */
    fun toBoolean(): Boolean
    
    /**
     * Gets a character value based on the unicode value of this number.
     */
    fun toChar(): Char
    
    /**
     * Converts this number to a float.
     */
    fun toFloat(): kotlin.Float
    
    /**
     * Converts this number to an integer.
     */
    fun toInt(): kotlin.Int
    
    /**
     * An implementation of [MinimNumber] that uses 32-bit floating-point numbers and arithmetic.
     *
     * @param value the internal value
     */
    class Float(override val value: kotlin.Float = 0F) : MinimNumber<kotlin.Float> {
        override operator fun unaryMinus() =
            Float(-value)
        
        override operator fun not() =
            Float((!value.toBoolean()).toFloat())
        
        override fun narrow() =
            Float(value.toBoolean().toFloat())
        
        override fun inv() =
            Float(value.toInt().inv().toFloat())
        
        override operator fun plus(x: MinimNumber<*>) = when (x) {
            is Float -> Float(value + x.value)
            is Int   -> Float(value + x.value)
        }
        
        override operator fun minus(x: MinimNumber<*>) = when (x) {
            is Float -> Float(value - x.value)
            is Int   -> Float(value - x.value)
        }
        
        override operator fun times(x: MinimNumber<*>) = when (x) {
            is Float -> Float(value * x.value)
            is Int   -> Float(value * x.value)
        }
        
        override operator fun div(x: MinimNumber<*>) = when (x) {
            is Float -> Float(value / x.value)
            is Int   -> Float(value / x.value)
        }
        
        override operator fun rem(x: MinimNumber<*>) = when (x) {
            is Float -> Float(value % x.value)
            is Int   -> Float(value % x.value)
        }
        
        override infix fun lss(x: MinimNumber<*>) = when (x) {
            is Float -> Float((value < x.value).toFloat())
            is Int   -> Float((value < x.value).toFloat())
        }
        
        override infix fun leq(x: MinimNumber<*>) = when (x) {
            is Float -> Float((value <= x.value).toFloat())
            is Int   -> Float((value <= x.value).toFloat())
        }
        
        override infix fun grt(x: MinimNumber<*>) = when (x) {
            is Float -> Float((value > x.value).toFloat())
            is Int   -> Float((value > x.value).toFloat())
        }
        
        override infix fun geq(x: MinimNumber<*>) = when (x) {
            is Float -> Float((value >= x.value).toFloat())
            is Int   -> Float((value >= x.value).toFloat())
        }
        
        override infix fun equ(x: MinimNumber<*>) = when (x) {
            is Float -> Float((value == x.value).toFloat())
            is Int   -> Float((value == x.value.toFloat()).toFloat())
        }
        
        override infix fun neq(x: MinimNumber<*>) = when (x) {
            is Float -> Float((value != x.value).toFloat())
            is Int   -> Float((value != x.value.toFloat()).toFloat())
        }
        
        override fun bnd(x: MinimNumber<*>) = when (x) {
            is Float -> Float((value.toInt() and x.value.toInt()).toFloat())
            is Int   -> Float((value.toInt() and x.value).toFloat())
        }
        
        override fun xor(x: MinimNumber<*>) = when (x) {
            is Float -> Float((value.toInt() xor x.value.toInt()).toFloat())
            is Int   -> Float((value.toInt() xor x.value).toFloat())
        }
        
        override fun bor(x: MinimNumber<*>) = when (x) {
            is Float -> Float((value.toInt() or x.value.toInt()).toFloat())
            is Int   -> Float((value.toInt() or x.value).toFloat())
        }
        
        override infix fun shl(x: MinimNumber<*>) = when (x) {
            is Float -> Float((value.toInt() shl x.value.toInt()).toFloat())
            is Int   -> Float((value.toInt() shl x.value).toFloat())
        }
        
        override infix fun shr(x: MinimNumber<*>) = when (x) {
            is Float -> Float((value.toInt() shr x.value.toInt()).toFloat())
            is Int   -> Float((value.toInt() shr x.value).toFloat())
        }
        
        override infix fun usr(x: MinimNumber<*>) = when (x) {
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
        
        override fun toString() =
            value.toString()
    }
    
    /**
     * An implementation of [MinimNumber] that uses 32-bit integer numbers and arithmetic.
     *
     * @param value the internal value
     */
    class Int(override val value: kotlin.Int = 0) : MinimNumber<kotlin.Int> {
        override operator fun unaryMinus() =
            Int(-value)
        
        override operator fun not() =
            Int((!value.toBoolean()).toInt())
        
        override fun narrow() =
            Int(value.toBoolean().toInt())
        
        override fun inv() =
            Int(value.inv())
        
        override operator fun plus(x: MinimNumber<*>) = when (x) {
            is Float -> Int(value + x.value.toInt())
            is Int   -> Int(value + x.value)
        }
        
        override operator fun minus(x: MinimNumber<*>) = when (x) {
            is Float -> Int(value - x.value.toInt())
            is Int   -> Int(value - x.value)
        }
        
        override operator fun times(x: MinimNumber<*>) = when (x) {
            is Float -> Int(value * x.value.toInt())
            is Int   -> Int(value * x.value)
        }
        
        override operator fun div(x: MinimNumber<*>) = when (x) {
            is Float -> Int(value / x.value.toInt())
            is Int   -> Int(value / x.value)
        }
        
        override operator fun rem(x: MinimNumber<*>) = when (x) {
            is Float -> Int(value % x.value.toInt())
            is Int   -> Int(value % x.value)
        }
        
        override infix fun lss(x: MinimNumber<*>) = when (x) {
            is Float -> Int((value < x.value).toInt())
            is Int   -> Int((value < x.value).toInt())
        }
        
        override infix fun leq(x: MinimNumber<*>) = when (x) {
            is Float -> Int((value <= x.value).toInt())
            is Int   -> Int((value <= x.value).toInt())
        }
        
        override infix fun grt(x: MinimNumber<*>) = when (x) {
            is Float -> Int((value > x.value).toInt())
            is Int   -> Int((value > x.value).toInt())
        }
        
        override infix fun geq(x: MinimNumber<*>) = when (x) {
            is Float -> Int((value >= x.value).toInt())
            is Int   -> Int((value >= x.value).toInt())
        }
        
        override infix fun equ(x: MinimNumber<*>) = when (x) {
            is Float -> Int((value == x.value.toInt()).toInt())
            is Int   -> Int((value == x.value).toInt())
        }
        
        override infix fun neq(x: MinimNumber<*>) = when (x) {
            is Float -> Int((value != x.value.toInt()).toInt())
            is Int   -> Int((value != x.value).toInt())
        }
        
        override fun bnd(x: MinimNumber<*>) = when (x) {
            is Float -> Int((value and x.value.toInt()))
            is Int   -> Int(value and x.value)
        }
        
        override fun xor(x: MinimNumber<*>) = when (x) {
            is Float -> Int((value xor x.value.toInt()))
            is Int   -> Int(value xor x.value)
        }
        
        override fun bor(x: MinimNumber<*>) = when (x) {
            is Float -> Int((value or x.value.toInt()))
            is Int   -> Int(value or x.value)
        }
        
        override infix fun shl(x: MinimNumber<*>) = when (x) {
            is Float -> Int(value shl x.value.toInt())
            is Int   -> Int(value shl x.value)
        }
        
        override infix fun shr(x: MinimNumber<*>) = when (x) {
            is Float -> Int(value shr x.value.toInt())
            is Int   -> Int(value shr x.value)
        }
        
        override infix fun usr(x: MinimNumber<*>) = when (x) {
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
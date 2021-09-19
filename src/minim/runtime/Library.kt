package minim.runtime

import minim.util.toFloat
import kotlin.math.*

/**
 * A mapping of every available system function.
 */
object Library : MutableMap<String, Library.Function> by mutableMapOf() {
    init {
        addGeneral()
        addMath()
        addText()
    }
    
    /**
     * Adds all of the general functions to the library map.
     */
    private fun addGeneral() {
        this["time"] = Function { _, _ -> floatArrayOf(System.currentTimeMillis() / 1000F) }
        
        this["wait"] = Function(1) { _, args ->
            val (s) = args
            
            Thread.sleep((s * 1000).toLong())
            
            floatArrayOf()
        }
    }
    
    /**
     * Adds all of the math functions to the library map.
     */
    private fun addMath() {
        this["abs"] = Function(1) { _, args ->
            val (n) = args
            
            floatArrayOf(abs(n))
        }
        
        this["acos"] = Function(1) { _, args ->
            val (n) = args
            
            floatArrayOf(acos(n))
        }
        
        this["acosh"] = Function(1) { _, args ->
            val (n) = args
            
            floatArrayOf(acosh(n))
        }
        
        this["asin"] = Function(1) { _, args ->
            val (n) = args
            
            floatArrayOf(asin(n))
        }
        
        this["asinh"] = Function(1) { _, args ->
            val (n) = args
            
            floatArrayOf(asinh(n))
        }
        
        this["atan"] = Function(1) { _, args ->
            val (n) = args
            
            floatArrayOf(atan(n))
        }
        
        this["atan2"] = Function(2) { _, args ->
            val (y, x) = args
            
            floatArrayOf(atan2(y, x))
        }
        
        this["atanh"] = Function(1) { _, args ->
            val (n) = args
            
            floatArrayOf(atanh(n))
        }
        
        this["cbrt"] = Function(1) { _, args ->
            val (n) = args
            
            floatArrayOf(Math.cbrt(n.toDouble()).toFloat())
        }
        
        this["ceil"] = Function(1) { _, args ->
            val (n) = args
            
            floatArrayOf(ceil(n))
        }
        
        this["cos"] = Function(1) { _, args ->
            val (n) = args
            
            floatArrayOf(cos(n))
        }
        
        this["cosh"] = Function(1) { _, args ->
            val (n) = args
            
            floatArrayOf(cosh(n))
        }
        
        this["deg"] = Function(1) { _, args ->
            val (n) = args
            
            floatArrayOf(Math.toDegrees(n.toDouble()).toFloat())
        }
        
        this["exp"] = Function(1) { _, args ->
            val (n) = args
            
            floatArrayOf(exp(n))
        }
        
        this["expm1"] = Function(1) { _, args ->
            val (n) = args
            
            floatArrayOf(expm1(n))
        }
        
        this["floor"] = Function(1) { _, args ->
            val (n) = args
            
            floatArrayOf(floor(n))
        }
        
        this["hypot"] = Function(2) { _, args ->
            val (x, y) = args
            
            floatArrayOf(hypot(x, y))
        }
        
        this["ln"] = Function(1) { _, args ->
            val (n) = args
            
            floatArrayOf(ln(n))
        }
        
        this["ln1p"] = Function(1) { _, args ->
            val (n) = args
            
            floatArrayOf(ln1p(n))
        }
        
        this["log"] = Function(2) { _, args ->
            val (n, b) = args
            
            floatArrayOf(log(n, b))
        }
        
        this["log10"] = Function(1) { _, args ->
            val (n) = args
            
            floatArrayOf(log10(n))
        }
        
        this["log2"] = Function(1) { _, args ->
            val (n) = args
            
            floatArrayOf(log2(n))
        }
        
        this["map"] = Function(5) { _, args ->
            val (n, fromMin, fromMax, toMin, toMax) = args
            
            floatArrayOf((n - fromMin) / (fromMax - fromMin) * (toMax - toMin) + toMin)
        }
        
        this["max"] = Function(2) { _, args ->
            val (a, b) = args
            
            floatArrayOf(max(a, b))
        }
        
        this["min"] = Function(2) { _, args ->
            val (a, b) = args
            
            floatArrayOf(min(a, b))
        }
        
        this["nextdown"] = Function(1) { _, args ->
            val (n) = args
            
            floatArrayOf(n.nextDown())
        }
        
        this["nextto"] = Function(2) { _, args ->
            val (a, b) = args
            
            floatArrayOf(a.nextTowards(b))
        }
        
        this["nextup"] = Function(1) { _, args ->
            val (n) = args
            
            floatArrayOf(n.nextUp())
        }
        
        this["pow"] = Function(2) { _, args ->
            val (b, e) = args
            
            floatArrayOf(b.pow(e))
        }
        
        this["rad"] = Function(1) { _, args ->
            val (n) = args
            
            floatArrayOf(Math.toRadians(n.toDouble()).toFloat())
        }
        
        this["round"] = Function(1) { _, args ->
            val (n) = args
            
            floatArrayOf(round(n))
        }
        
        this["sign"] = Function(1) { _, args ->
            val (n) = args
            
            floatArrayOf(n.sign)
        }
        
        this["sin"] = Function(1) { _, args ->
            val (n) = args
            
            floatArrayOf(sin(n))
        }
        
        this["sinh"] = Function(1) { _, args ->
            val (n) = args
            
            floatArrayOf(sinh(n))
        }
        
        this["sqrt"] = Function(1) { _, args ->
            val (n) = args
            
            floatArrayOf(sqrt(n))
        }
        
        this["tan"] = Function(1) { _, args ->
            val (n) = args
            
            floatArrayOf(tan(n))
        }
        
        this["tanh"] = Function(1) { _, args ->
            val (n) = args
            
            floatArrayOf(tanh(n))
        }
        
        this["truncate"] = Function(1) { _, args ->
            val (n) = args
            
            floatArrayOf(truncate(n))
        }
        
        this["ulp"] = Function(1) { _, args ->
            val (n) = args
            
            floatArrayOf(n.ulp)
        }
    }
    
    /**
     * Adds all of the text functions to the library map.
     */
    private fun addText() {
        this["isalpha"] = Function(1) { _, args ->
            val (c) = args
            
            floatArrayOf(c.toInt().toChar().isLetter().toFloat())
        }
        
        this["isalnum"] = Function(1) { _, args ->
            val (c) = args
            
            floatArrayOf(c.toInt().toChar().isLetterOrDigit().toFloat())
        }
        
        this["isdigit"] = Function(1) { _, args ->
            val (c) = args
            
            floatArrayOf(c.toInt().toChar().isDigit().toFloat())
        }
        
        this["isspace"] = Function(1) { _, args ->
            val (c) = args
            
            floatArrayOf(c.toInt().toChar().isWhitespace().toFloat())
        }
        
        this["islower"] = Function(1) { _, args ->
            val (c) = args
            
            floatArrayOf(c.toInt().toChar().isLowerCase().toFloat())
        }
        
        this["isupper"] = Function(1) { _, args ->
            val (c) = args
            
            floatArrayOf(c.toInt().toChar().isUpperCase().toFloat())
        }
        
        this["tolower"] = Function(1) { _, args ->
            val (c) = args
            
            floatArrayOf(c.toInt().toChar().lowercase()[0].code.toFloat())
        }
        
        this["toupper"] = Function(1) { _, args ->
            val (c) = args
            
            floatArrayOf(c.toInt().toChar().uppercase()[0].code.toFloat())
        }
    }
    
    /**
     * A representation of a single system function.
     *
     * @param arity the amount of parameters the function has
     * @param method the body of the system function
     */
    data class Function(val arity: Int = 0, val method: (runtime: Runtime, args: FloatArray) -> FloatArray) {
        /**
         * Delegates to the invocation of the [method] lambda.
         *
         * @param runtime the current runtime being run
         * @param args the arguments to call the function with
         */
        operator fun invoke(runtime: Runtime, args: FloatArray) =
            method(runtime, args)
    }
}
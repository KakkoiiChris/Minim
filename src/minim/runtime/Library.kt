package minim.runtime

import kotlin.math.*

object Library : MutableMap<String, Library.Command> by mutableMapOf() {
    init {
        this["wait"] = Command(1) { _, args ->
            val (s) = args
            
            Thread.sleep((s * 1000).toLong())
            
            floatArrayOf()
        }
        
        this["time"] = Command { _, _ -> floatArrayOf(System.currentTimeMillis() / 1000F) }
        
        this["args"] = Command { runtime, _ -> runtime.config.args.map { it.code.toFloat() }.toFloatArray() }
        
        this["size"] = Command { runtime, _ -> floatArrayOf(runtime.config.size.toFloat()) }
        
        this["cat"] = Command(3) { _, args ->
            val (a, b, c) = args
            
            floatArrayOf(a, b, c)
        }
        
        this["abs"] = Command(1) { _, args ->
            val (n) = args
            
            floatArrayOf(abs(n))
        }
        
        this["acos"] = Command(1) { _, args ->
            val (n) = args
            
            floatArrayOf(acos(n))
        }
        
        this["acosh"] = Command(1) { _, args ->
            val (n) = args
            
            floatArrayOf(acosh(n))
        }
        
        this["asin"] = Command(1) { _, args ->
            val (n) = args
            
            floatArrayOf(asin(n))
        }
        
        this["asinh"] = Command(1) { _, args ->
            val (n) = args
            
            floatArrayOf(asinh(n))
        }
        
        this["atan"] = Command(1) { _, args ->
            val (n) = args
            
            floatArrayOf(atan(n))
        }
        
        this["atan2"] = Command(2) { _, args ->
            val (y, x) = args
            
            floatArrayOf(atan2(y, x))
        }
        
        this["atanh"] = Command(1) { _, args ->
            val (n) = args
            
            floatArrayOf(atanh(n))
        }
        
        this["ceil"] = Command(1) { _, args ->
            val (n) = args
            
            floatArrayOf(ceil(n))
        }
        
        this["cos"] = Command(1) { _, args ->
            val (n) = args
            
            floatArrayOf(cos(n))
        }
        
        this["cosh"] = Command(1) { _, args ->
            val (n) = args
            
            floatArrayOf(cosh(n))
        }
        
        this["e"] = Command { _, _ -> floatArrayOf(Math.E.toFloat()) }
        
        this["exp"] = Command(1) { _, args ->
            val (n) = args
            
            floatArrayOf(exp(n))
        }
        
        this["expm1"] = Command(1) { _, args ->
            val (n) = args
            
            floatArrayOf(expm1(n))
        }
        
        this["floor"] = Command(1) { _, args ->
            val (n) = args
            
            floatArrayOf(floor(n))
        }
        
        this["hypot"] = Command(2) { _, args ->
            val (x, y) = args
            
            floatArrayOf(hypot(x, y))
        }
        
        this["ln"] = Command(1) { _, args ->
            val (n) = args
            
            floatArrayOf(ln(n))
        }
        
        this["ln1p"] = Command(1) { _, args ->
            val (n) = args
            
            floatArrayOf(ln1p(n))
        }
        
        this["log"] = Command(2) { _, args ->
            val (n, base) = args
            
            floatArrayOf(log(n, base))
        }
        
        this["log10"] = Command(1) { _, args ->
            val (n) = args
            
            floatArrayOf(log10(n))
        }
        
        this["log2"] = Command(1) { _, args ->
            val (n) = args
            
            floatArrayOf(log2(n))
        }
        
        this["max"] = Command(2) { _, args ->
            val (a, b) = args
            
            floatArrayOf(max(a, b))
        }
        
        this["min"] = Command(2) { _, args ->
            val (a, b) = args
            
            floatArrayOf(min(a, b))
        }
        
        this["nextDown"] = Command(1) { _, args ->
            val (n) = args
            
            floatArrayOf(n.nextDown())
        }
        
        this["nextTowards"] = Command(2) { _, args ->
            val (a, b) = args
            
            floatArrayOf(a.nextTowards(b))
        }
        
        this["nextUp"] = Command(1) { _, args ->
            val (n) = args
            
            floatArrayOf(n.nextUp())
        }
        
        this["pi"] = Command { _, _ -> floatArrayOf(Math.PI.toFloat()) }
        
        this["pow"] = Command(2) { _, args ->
            val (b, e) = args
            
            floatArrayOf(b.pow(e))
        }
        
        this["random"] = Command { _, _ -> floatArrayOf(Math.random().toFloat()) }
        
        this["round"] = Command(1) { _, args ->
            val (n) = args
            
            floatArrayOf(round(n))
        }
        
        this["roundToInt"] = Command(1) { _, args ->
            val (n) = args
            
            floatArrayOf(n.roundToInt().toFloat())
        }
        
        this["sign"] = Command(1) { _, args ->
            val (n) = args
            
            floatArrayOf(n.sign)
        }
        
        this["sin"] = Command(1) { _, args ->
            val (n) = args
            
            floatArrayOf(sin(n))
        }
        
        this["sinh"] = Command(1) { _, args ->
            val (n) = args
            
            floatArrayOf(sinh(n))
        }
        
        this["tan"] = Command(1) { _, args ->
            val (n) = args
            
            floatArrayOf(tan(n))
        }
        
        this["tanh"] = Command(1) { _, args ->
            val (n) = args
            
            floatArrayOf(tanh(n))
        }
        
        this["truncate"] = Command(1) { _, args ->
            val (n) = args
            
            floatArrayOf(truncate(n))
        }
        
        this["ulp"] = Command(1) { _, args ->
            val (n) = args
            
            floatArrayOf(n.ulp)
        }
        
        this["map"] = Command(5) { _, args ->
            val (n, fromMin, fromMax, toMin, toMax) = args
            
            floatArrayOf((n - fromMin) / (fromMax - fromMin) * (toMax - toMin) + toMin)
        }
    }
    
    class Command(val arity: Int = 0, val method: (runtime: Runtime, args: FloatArray) -> FloatArray) {
        operator fun invoke(runtime: Runtime, args: FloatArray) =
            method(runtime, args)
    }
}
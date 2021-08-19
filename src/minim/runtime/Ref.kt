package minim.runtime

class Ref(var value: MNumber = MNumber.Float()) {
    fun preIncrement() = when (value) {
        is MNumber.Float -> {
            value = MNumber.Float(value.value as Float + 1F)
            
            value
        }
        
        is MNumber.Int   -> {
            value = MNumber.Int(value.value as Int + 1)
            
            value
        }
    }
    
    fun preDecrement() = when (value) {
        is MNumber.Float -> {
            value = MNumber.Float(value.value as Float - 1F)
            
            value
        }
        
        is MNumber.Int   -> {
            value = MNumber.Int(value.value as Int - 1)
            
            value
        }
    }
    
    fun preNarrow(): MNumber {
        value = value.narrow()
        
        return value
    }
    
    fun preToggle(): MNumber {
        value = value.not()
        
        return value
    }
    
    fun preInvert(): MNumber {
        value = value.inv()
        
        return value
    }
    
    fun postIncrement() = when (value) {
        is MNumber.Float -> {
            val before = value
            
            value = MNumber.Float(value.value as Float + 1F)
            
            before
        }
        
        is MNumber.Int   -> {
            val before = value
            
            value = MNumber.Int(value.value as Int + 1)
            
            before
        }
    }
    
    fun postDecrement() = when (value) {
        is MNumber.Float -> {
            val before = value
            
            value = MNumber.Float(value.value as Float - 1F)
            
            before
        }
        
        is MNumber.Int   -> {
            val before = value
            
            value = MNumber.Int(value.value as Int - 1)
            
            before
        }
    }
    
    fun postNarrow(): MNumber {
        val before = value
        
        value = value.narrow()
        
        return before
    }
    
    fun postToggle(): MNumber {
        val before = value
        
        value = value.not()
        
        return before
    }
    
    fun postInvert(): MNumber {
        val before = value
        
        value = value.inv()
        
        return before
    }
    
    fun assign(x: MNumber): MNumber {
        value = x
        
        return x
    }
    
    fun set(float: Float) {
        value = MNumber.Float(float)
    }
    
    fun set(int: Int) {
        value = MNumber.Int(int)
    }
    
    override fun toString() =
        "&${value.toChar()}"
}

fun Any.fromRef() =
    (this as? Ref)?.value ?: this
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
}

fun Any.fromRef() =
    (this as? Ref)?.value ?: this
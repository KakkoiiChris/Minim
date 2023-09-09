package minim.runtime

/**
 * A container that holds a reference to a single [MinimNumber].
 *
 * @param value the value to hold
 */
class Reference(var value: MinimNumber<*> = MinimNumber.Float()) {
    /**
     * Increases [value] by 1, and gets the new value.
     *
     * @return [value] + 1
     */
    fun preIncrement() = when (value) {
        is MinimNumber.Float -> {
            value = MinimNumber.Float(value.value as Float + 1F)
            
            value
        }
        
        is MinimNumber.Int   -> {
            value = MinimNumber.Int(value.value as Int + 1)
            
            value
        }
    }
    
    /**
     * Decreases [value] by 1, and gets the new value.
     *
     * @return [value] - 1
     */
    fun preDecrement() = when (value) {
        is MinimNumber.Float -> {
            value = MinimNumber.Float(value.value as Float - 1F)
            
            value
        }
        
        is MinimNumber.Int   -> {
            value = MinimNumber.Int(value.value as Int - 1)
            
            value
        }
    }
    
    /**
     * Converts [value] to its logical identity, and gets the new value.
     *
     * @return ?[value]
     */
    fun preNarrow(): MinimNumber<*> {
        value = value.narrow()
        
        return value
    }
    
    /**
     * Converts [value] to its logical inverse, and gets the new value.
     *
     * @return ![value]
     */
    fun preToggle(): MinimNumber<*> {
        value = value.not()
        
        return value
    }
    
    /**
     * Converts [value] to it's bitwise inverse, and gets the new value.
     *
     * @return ~[value]
     */
    fun preInvert(): MinimNumber<*> {
        value = value.inv()
        
        return value
    }
    
    /**
     * Increases [value] by 1, and gets the old value.
     *
     * @return [value]
     */
    fun postIncrement() = when (value) {
        is MinimNumber.Float -> {
            val before = value
            
            value = MinimNumber.Float(value.value as Float + 1F)
            
            before
        }
        
        is MinimNumber.Int   -> {
            val before = value
            
            value = MinimNumber.Int(value.value as Int + 1)
            
            before
        }
    }
    
    /**
     * Decreases [value] by 1, and gets the old value.
     *
     * @return [value]
     */
    fun postDecrement() = when (value) {
        is MinimNumber.Float -> {
            val before = value
            
            value = MinimNumber.Float(value.value as Float - 1F)
            
            before
        }
        
        is MinimNumber.Int   -> {
            val before = value
            
            value = MinimNumber.Int(value.value as Int - 1)
            
            before
        }
    }
    
    /**
     * Converts [value] to its logical identity, and gets the old value.
     *
     * @return [value]
     */
    fun postNarrow(): MinimNumber<*> {
        val before = value
        
        value = value.narrow()
        
        return before
    }
    
    /**
     * Converts [value] to its logical inverse, and gets the old value.
     *
     * @return [value]
     */
    fun postToggle(): MinimNumber<*> {
        val before = value
        
        value = value.not()
        
        return before
    }
    
    /**
     * Converts [value] to it's bitwise inverse, and gets the old value.
     *
     * @return [value]
     */
    fun postInvert(): MinimNumber<*> {
        val before = value
        
        value = value.inv()
        
        return before
    }
    
    /**
     * Sets this reference to a new value.
     *
     * @param x the number to refer to
     *
     * @return [x]
     */
    fun assign(x: MinimNumber<*>): MinimNumber<*> {
        value = x
        
        return x
    }
    
    /**
     * Sets this reference to a new [MinimNumber.Float] with the given value.
     *
     * @param float the number to refer to
     */
    fun set(float: Float) {
        value = MinimNumber.Float(float)
    }
    
    /**
     * Sets this reference to a new [MinimNumber.Int] with the given value.
     *
     * @param int the number to refer to
     */
    fun set(int: Int) {
        value = MinimNumber.Int(int)
    }
    
    /**
     * Gets the string representation of the referenced value prepended with an '&', to distinguish it from standalone values.
     *
     * @return ex. '&247', '&-7.3'
     */
    override fun toString() =
        "&$value"
}

/**
 * Helper extension method to unwrap any reference, and leave other values intact.
 *
 * @receiver the value to unwrap
 */
fun Any.fromReference() =
    (this as? Reference)?.value ?: this
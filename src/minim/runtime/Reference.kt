package minim.runtime

/**
 * A container that holds a reference to a single [MNumber].
 *
 * @param value the value to hold
 */
class Reference(var value: MNumber<*> = MNumber.Float()) {
    /**
     * Increases [value] by 1, and gets the new value.
     *
     * @return [value] + 1
     */
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
    
    /**
     * Decreases [value] by 1, and gets the new value.
     *
     * @return [value] - 1
     */
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
    
    /**
     * Converts [value] to its logical identity, and gets the new value.
     *
     * @return ?[value]
     */
    fun preNarrow(): MNumber<*> {
        value = value.narrow()
        
        return value
    }
    
    /**
     * Converts [value] to its logical inverse, and gets the new value.
     *
     * @return ![value]
     */
    fun preToggle(): MNumber<*> {
        value = value.not()
        
        return value
    }
    
    /**
     * Converts [value] to it's bitwise inverse, and gets the new value.
     *
     * @return ~[value]
     */
    fun preInvert(): MNumber<*> {
        value = value.inv()
        
        return value
    }
    
    /**
     * Increases [value] by 1, and gets the old value.
     *
     * @return [value]
     */
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
    
    /**
     * Decreases [value] by 1, and gets the old value.
     *
     * @return [value]
     */
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
    
    /**
     * Converts [value] to its logical identity, and gets the old value.
     *
     * @return [value]
     */
    fun postNarrow(): MNumber<*> {
        val before = value
        
        value = value.narrow()
        
        return before
    }
    
    /**
     * Converts [value] to its logical inverse, and gets the old value.
     *
     * @return [value]
     */
    fun postToggle(): MNumber<*> {
        val before = value
        
        value = value.not()
        
        return before
    }
    
    /**
     * Converts [value] to it's bitwise inverse, and gets the old value.
     *
     * @return [value]
     */
    fun postInvert(): MNumber<*> {
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
    fun assign(x: MNumber<*>): MNumber<*> {
        value = x
        
        return x
    }
    
    /**
     * Sets this reference to a new [MNumber.Float] with the given value.
     *
     * @param float the number to refer to
     */
    fun set(float: Float) {
        value = MNumber.Float(float)
    }
    
    /**
     * Sets this reference to a new [MNumber.Int] with the given value.
     *
     * @param int the number to refer to
     */
    fun set(int: Int) {
        value = MNumber.Int(int)
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
package minim

import minim.runtime.Config
import minim.runtime.MNumber
import minim.runtime.Runtime
import minim.util.MinimError
import minim.util.Source
import minim.util.escaped
import java.io.File
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

/**
 * Parses program flags from the command line arguments, and starts the appropriate interpreter mode.
 */
@ExperimentalTime
fun main(mainArgs: Array<String>) {
    var args = ""
    var debug = false
    var file = ""
    var size = 0x10000
    
    var i = 0
    
    while (i < mainArgs.size) {
        val arg = mainArgs[i]
        
        if (arg[0] == '-') when (arg.substring(1)) {
            "a" -> args = mainArgs[++i]
            
            "d" -> debug = true
            
            "f" -> file = mainArgs[++i]
            
            "s" -> {
                val code = mainArgs[++i]
                
                val number = code.substring(0, code.length - 1)
                val suffix = code[code.length - 1]
                
                size = when (suffix) {
                    'K', 'k' -> ((number.toFloatOrNull() ?: error("Memory size must be a number!")) * 1000).toInt()
                    
                    'M', 'm' -> ((number.toFloatOrNull() ?: error("Memory size must be a number!")) * 1000000).toInt()
                    
                    'B', 'b' -> ((number.toFloatOrNull()
                        ?: error("Memory size must be a number!")) * 1000000000).toInt()
                    
                    else     -> code.toIntOrNull() ?: error("Memory size must be a number!")
                }
            }
        }
        
        i++
    }
    
    val repl = file.isEmpty()
    
    val config = Config(args, debug, repl, size)
    
    if (repl) {
        repl(config)
    }
    else {
        file(config, file)
    }
}

/**
 * Runs the interpreter in REPL (Read-Eval-Print Loop) mode.
 *
 * @param config the configuration data for the [Runtime]
 */
@ExperimentalTime
private fun repl(config: Config) {
    println("""
        ##.      .##'    .##
        ####.  .##'    .##'
        ## '####'    .##'
        ##   ''    .##'    .
        ##       .##'    .##
        ##     .##'    .####
        '    .##'    .##' ##
           .##'    .##'   ##
         .##'    .##'     ##
        ##'    .##'       ##
        --------------------
         Minim  Programming
          Language V 5.1.9
          
          """.trimIndent())
    
    val runtime = Runtime(config, mutableListOf())
    
    do {
        print("$> ")
        
        val text = readLine()?.takeIf { it.isNotBlank() } ?: break
        
        val (value, duration) = try {
            val code = "[0] = $text."
            
            val source = Source("REPL", code)
            
            val stmts = source.compile()
            
            runtime.reset(stmts)
            
            measureTimedValue { runtime.run() }
        }
        catch (e: MinimError) {
            try {
                val source = Source("REPL", text)
                
                val stmts = source.compile()
                
                runtime.reset(stmts)
                
                measureTimedValue { runtime.run() }
            }
            catch (error: MinimError) {
                printError(config.debug, error)
                
                continue
            }
        }
        
        printEndMessage(value, duration)
    }
    while (true)
}

/**
 * Runs the interpreter in file mode.
 *
 * @param config the configuration data for the [Runtime]
 * @param path the path fo the file to read and run
 */
@ExperimentalTime
private fun file(config: Config, path: String) {
    val file = File(path)
    
    val name = file.nameWithoutExtension
    val text = file.readText()
    
    val source = Source(name, text)
    
    try {
        val stmts = source.compile()
        
        val runtime = Runtime(config, stmts)
        
        val (value, duration) = measureTimedValue { runtime.run() }
        
        printEndMessage(value, duration)
    }
    catch (error: MinimError) {
        printError(config.debug, error)
    }
}

/**
 * Prints the end-of-execution message, with the final value and the execution time.
 *
 * @param value the final value of memory index 0
 * @param duration the timing of the program execution
 */
@ExperimentalTime
private fun printEndMessage(value: MNumber, duration: Duration) {
    println("\n$< $value, '${value.toChar().escaped()}' (${duration.inWholeNanoseconds / 1E9} s)\n")
}

/**
 * Prints the error message, or the full error stack trace if debug mode is active.
 *
 * @param debug if debug mode is active
 * @param error the error to print
 */
private fun printError(debug: Boolean, error: MinimError) {
    if (debug) {
        println()
        
        error.printStackTrace()
        
        println("\n")
    }
    else {
        println("\n${error.message}\n")
    }
}
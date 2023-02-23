package minim

import minim.parser.Program
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
fun main(args: Array<String>) {
    val cd = args[0]
    
    var mArgs = ""
    var debug = false
    var file = ""
    var size = 0x10000
    
    var i = 1
    
    while (i < args.size) {
        val arg = args[i]
        
        if (arg[0] == '-') when (arg.substring(1)) {
            "a" -> mArgs = args[++i]
            
            "d" -> debug = true
            
            "f" -> file = args[++i]
            
            "s" -> {
                val code = args[++i]
                
                val number = code.substring(0, code.length - 1)
                val suffix = code[code.length - 1]
                
                size = when (suffix) {
                    'K', 'k' -> ((number.toFloatOrNull() ?: error("Memory size must be a number!")) * 1E3).toInt()
                    
                    'M', 'm' -> ((number.toFloatOrNull() ?: error("Memory size must be a number!")) * 1E6).toInt()
                    
                    'B', 'b' -> ((number.toFloatOrNull() ?: error("Memory size must be a number!")) * 1E9).toInt()
                    
                    else     -> code.toIntOrNull() ?: error("Memory size must be a number!")
                }
            }
        }
        
        i++
    }
    
    val repl = file.isEmpty()
    
    val config = Config(mArgs, debug, repl, size)
    
    if (repl) {
        repl(config)
    }
    else {
        file(config, cd, file)
    }
}

/**
 * Runs the interpreter in REPL (Read-Eval-Print Loop) mode.
 *
 * @param config the configuration data for the [Runtime]
 */
@ExperimentalTime
private fun repl(config: Config) {
    println(
        """
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
          
          """.trimIndent()
    )
    
    val runtime = Runtime(config, Program.empty)
    
    do {
        print("$> ")
        
        val text = readln().takeIf { it.isNotBlank() } ?: break
        
        val (value, duration) = try {
            val code = "[0] = $text."
            
            val source = Source("REPL", code)
            
            val program = source.create()
            
            runtime.reset(program)
            
            measureTimedValue { runtime.run() }
        }
        catch (e: MinimError) {
            try {
                val source = Source("REPL", text)
                
                val program = source.create()
                
                runtime.reset(program)
                
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
private fun file(config: Config, cd: String, path: String) {
    var file = File(path)
    
    if (!file.exists()) {
        file = File("$cd\\$path")
        
        if (!file.exists()) {
            error("The specified path does not exist!")
        }
    }
    
    val name = file.nameWithoutExtension
    val text = file.readText()
    
    val source = Source(name, text)
    
    try {
        val program = source.create()
        
        val runtime = Runtime(config, program)
        
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
private fun printEndMessage(value: MNumber<*>, duration: Duration) {
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
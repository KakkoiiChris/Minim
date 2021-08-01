package minim

import minim.runtime.Config
import minim.util.Source
import java.io.File
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

@ExperimentalTime
fun main(mainArgs: Array<String>) {
    var args = ""
    var size = 0xFFFF
    var file = ""
    
    var i = 0
    
    while (i < mainArgs.size) {
        val arg = mainArgs[i]
        
        if (arg[0] == '-') when (arg.substring(1)) {
            "a" -> args = mainArgs[++i]
            
            "f" -> file = mainArgs[++i]
            
            "s" -> size = mainArgs[++i].toIntOrNull() ?: error("Memory size must be an integer!")
        }
        
        i++
    }
    
    val config = Config(args, size)
    
    if (file.isNotEmpty()) {
        file(config, file)
    }
    else {
        repl()
    }
}

@ExperimentalTime
private fun repl() {
    println("""
        ##.      .##'    .##
        ####.  .##'    .##'
        ## '####'    .##'
        ##   ''    .##'    .
        ##       .##'    .##
        ##     .##'    .####
             .##'    .##' ##
           .##'    .##'   ##
         .##'    .##'     ##
        ##'    .##'       ##
        --------------------
         Minim  Programming
          Language V 5.1.5
          
          """.trimIndent())
    
    do {
        print("minim> ")
        
        val text = readLine()?.takeIf { it.isNotBlank() } ?: break
        
        println()
        
        val source = Source("<REPL>", text)
        
        exec(Config(), source)
    }
    while (true)
}

@ExperimentalTime
private fun file(config: Config, path: String) {
    val file = File(path)
    
    val name = file.nameWithoutExtension
    val text = file.readText()
    
    val source = Source(name, text)
    
    exec(config, source)
}

@ExperimentalTime
private fun exec(config: Config, source: Source) {
    val runtime = source.compile(config)
    
    val (value, duration) = measureTimedValue { runtime.run() }
    
    if (value == Unit) {
        println("Done (${duration.inWholeMilliseconds / 1E3} s)")
    }
    else {
        println("Done: $value (${duration.inWholeMilliseconds / 1E3} s)")
    }
}
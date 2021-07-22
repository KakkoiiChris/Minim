package minim

import minim.util.Source
import java.io.File
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

@ExperimentalTime
fun main(args: Array<String>) {
    var subArgs = ""
    var file = ""
    
    var i = 0
    
    while (i < args.size) {
        val arg = args[i]
        
        if (arg[0] == '-') when (arg.substring(1)) {
            "a" -> subArgs = args[++i]
            
            "f" -> file = args[++i]
        }
        
        i++
    }
    
    if (file.isNotEmpty()) {
        file(subArgs, file)
    }
}

@ExperimentalTime
private fun repl() {
    do {
        print("minim> ")
        
        val text = readLine()?.takeIf { it.isNotBlank() } ?: break
        
        println()
        
        val source = Source("<REPL>", text)
        
        exec("", source)
    }
    while (true)
}

@ExperimentalTime
private fun file(args: String, path: String) {
    val file = File(path)
    
    val name = file.nameWithoutExtension
    val text = file.readText()
    
    val source = Source(name, text)
    
    exec(args, source)
}

@ExperimentalTime
private fun exec(args: String, source: Source) {
    val script = source.compile(args)
    
    val (value, duration) = measureTimedValue { script.run() }
    
    if (value == Unit) {
        println("Done (${duration.inWholeMilliseconds / 1E3} s)")
    }
    else {
        println("Done: $value (${duration.inWholeMilliseconds / 1E3} s)")
    }
}
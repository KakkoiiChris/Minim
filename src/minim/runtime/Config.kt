package minim.runtime

/**
 * A collection of configuration data passed from the command line to the interpreter [Runtime].
 *
 * @param args program arguments passed by the **a** flag
 * @param debug debug mode set by the **d** flag
 * @param repl REPL mode set if no file was specified with the **f** flag
 * @param size the size in values of each memory scope set by the **s** flag
 */
data class Config(val args: String, val debug: Boolean, val repl: Boolean, val size: Int)
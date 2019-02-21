package ac.uk.ebi.transpiler

import ac.uk.ebi.transpiler.cli.TranspilerCommandLine

fun main(args: Array<String>) {
    println(TranspilerCommandLine().transpile(args))
}

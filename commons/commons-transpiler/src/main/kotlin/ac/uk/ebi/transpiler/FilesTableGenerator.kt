package ac.uk.ebi.transpiler

import java.io.File

fun main(args: Array<String>) {
    // TODO use command line tool library to create the command line options
    val file = File(args[0])
    file.forEachLine { println(it) }
}

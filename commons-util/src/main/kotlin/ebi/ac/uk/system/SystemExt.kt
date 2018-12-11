package ebi.ac.uk.system

import java.nio.file.Path
import java.nio.file.Paths

fun tempFolder(): Path {
    return Paths.get(System.getProperty("java.io.tmpdir"))
}
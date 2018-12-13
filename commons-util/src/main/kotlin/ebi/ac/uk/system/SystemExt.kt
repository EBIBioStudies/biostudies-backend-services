package ebi.ac.uk.system

import java.nio.file.Path
import java.nio.file.Paths

/**
 * Obtain the current user configured temp folder.
 */
fun tempFolder(): Path {
    return Paths.get(System.getProperty("java.io.tmpdir"))
}

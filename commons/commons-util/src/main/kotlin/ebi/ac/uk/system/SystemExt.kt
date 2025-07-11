package ebi.ac.uk.system

import java.nio.file.Path
import java.nio.file.Paths

/**
 * Get the current user configured temp folder.
 */
fun tempFolder(): Path = Paths.get(System.getProperty("java.io.tmpdir"))

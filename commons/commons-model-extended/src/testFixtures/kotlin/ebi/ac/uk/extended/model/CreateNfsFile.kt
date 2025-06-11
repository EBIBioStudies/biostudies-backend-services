package ebi.ac.uk.extended.model

import ebi.ac.uk.io.ext.md5
import ebi.ac.uk.io.ext.size
import java.io.File

fun createNfsFile(
    filePath: String,
    relpath: String,
    file: File,
    attributes: List<ExtAttribute> = listOf(),
): NfsFile = NfsFile(filePath, relpath, file, file.absolutePath, true, file.md5(), file.size(), attributes)

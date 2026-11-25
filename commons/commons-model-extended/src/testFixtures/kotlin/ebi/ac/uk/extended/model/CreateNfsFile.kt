package ebi.ac.uk.extended.model

import uk.ac.ebi.biostd.io.ext.md5
import uk.ac.ebi.biostd.io.ext.size
import java.io.File

fun createNfsFile(
    filePath: String,
    relpath: String,
    file: File,
    attributes: List<ExtAttribute> = listOf(),
): NfsFile = NfsFile(filePath, relpath, file, file.absolutePath, file.md5(), file.size(), attributes = attributes)

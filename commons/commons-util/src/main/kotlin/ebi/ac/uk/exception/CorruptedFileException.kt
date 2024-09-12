package ebi.ac.uk.exception

import java.io.File

class CorruptedFileException(
    file: File,
) : RuntimeException("The file ${file.absolutePath} doesn't match the expected MD5")
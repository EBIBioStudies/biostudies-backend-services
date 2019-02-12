package ac.uk.ebi.transpiler.processor

import ac.uk.ebi.transpiler.common.LIB_FILE_SEPARATOR
import ac.uk.ebi.transpiler.common.LINE_BREAK
import ac.uk.ebi.transpiler.common.LibraryFile

abstract class LibraryFileProcessor {
    fun chunkerize(libraryFile:String): MutableList<List<String>> =
        libraryFile.split(LINE_BREAK).mapTo(mutableListOf()) { it.split(LIB_FILE_SEPARATOR) }

    abstract fun process(libraryFile: String, baseColumns: List<String>): LibraryFile
}

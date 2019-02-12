package ac.uk.ebi.transpiler.service

import ac.uk.ebi.biostd.SubFormat

interface LibraryFileTranspiler {
    fun transpile(libraryFile: String, baseColumns: List<String>, format: SubFormat): String
}

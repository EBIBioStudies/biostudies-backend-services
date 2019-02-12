package ac.uk.ebi.transpiler.service

import ac.uk.ebi.biostd.SubFormat

class BioImagesTranspiler : LibraryFileTranspiler {
    override fun transpile(libraryFile: String, baseColumns: List<String>, format: SubFormat): String {
        throw NotImplementedError()
    }
}

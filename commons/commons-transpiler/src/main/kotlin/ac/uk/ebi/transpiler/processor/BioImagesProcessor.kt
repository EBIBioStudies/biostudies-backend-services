package ac.uk.ebi.transpiler.processor

import ac.uk.ebi.transpiler.common.LibraryFile
import ac.uk.ebi.transpiler.common.PATH_SEPARATOR
import ebi.ac.uk.util.collections.ifNotEmpty
import ebi.ac.uk.util.collections.removeFirst

class BioImagesProcessor : LibraryFileProcessor() {
    override fun process(libraryFile: String, baseColumns: List<String>): LibraryFile {
        val libFile = LibraryFile()
        val chunks = chunkerize(libraryFile)
        chunks.ifNotEmpty {
            val header = chunks.removeFirst()

            libFile.header = header
            chunks.forEach { libFile.addRecord(getPath(it, baseColumns.size), it) }
        }

        return libFile
    }

    private fun getPath(attributes: List<String>, pathLength: Int) =
        attributes.subList(0, pathLength).reduce { path, attr -> path + PATH_SEPARATOR + attr }
}

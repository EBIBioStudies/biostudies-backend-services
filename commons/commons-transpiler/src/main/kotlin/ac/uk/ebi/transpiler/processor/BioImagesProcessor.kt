package ac.uk.ebi.transpiler.processor

import ac.uk.ebi.transpiler.common.FilesTableTemplate
import ac.uk.ebi.transpiler.common.PATH_SEPARATOR
import ebi.ac.uk.util.collections.ifNotEmpty
import ebi.ac.uk.util.collections.removeFirst

class BioImagesProcessor : FilesTableTemplateProcessor() {
    override fun process(template: String, baseColumns: List<String>): FilesTableTemplate {
        val libFile = FilesTableTemplate()
        val chunks = chunkerize(template)
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

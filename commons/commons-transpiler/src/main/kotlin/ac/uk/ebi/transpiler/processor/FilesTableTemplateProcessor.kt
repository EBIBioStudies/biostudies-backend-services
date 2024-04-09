package ac.uk.ebi.transpiler.processor

import ac.uk.ebi.transpiler.common.FilesTableTemplate
import ac.uk.ebi.transpiler.common.LINE_BREAK
import ac.uk.ebi.transpiler.common.PATH_SEPARATOR
import ac.uk.ebi.transpiler.common.TEMPLATE_SEPARATOR
import ac.uk.ebi.transpiler.exception.InvalidColumnException
import ebi.ac.uk.util.collections.ifNotEmpty
import ebi.ac.uk.util.collections.removeFirst

class FilesTableTemplateProcessor {
    fun process(
        template: String,
        baseColumns: List<String>,
    ): FilesTableTemplate {
        val libFile = FilesTableTemplate()
        val chunks = chunkerize(template)
        chunks.ifNotEmpty {
            val header = chunks.removeFirst()
            validateHeader(header, baseColumns)

            libFile.header = header
            chunks.forEach { libFile.addRecord(getPath(it, baseColumns.size), it) }
        }

        return libFile
    }

    private fun chunkerize(template: String): MutableList<List<String>> =
        if (template.isBlank()) {
            mutableListOf()
        } else {
            template
                .split(LINE_BREAK)
                .filter { it.isNotEmpty() }
                .mapTo(mutableListOf()) { it.split(TEMPLATE_SEPARATOR) }
        }

    private fun getPath(
        attributes: List<String>,
        pathLength: Int,
    ) = attributes.subList(0, pathLength).reduce { path, attr -> path + PATH_SEPARATOR + attr }

    private fun validateHeader(
        header: List<String>,
        baseColumns: List<String>,
    ) = baseColumns.forEachIndexed { idx, col ->
        if (col != header[idx]) throw InvalidColumnException(col, header[idx])
    }
}

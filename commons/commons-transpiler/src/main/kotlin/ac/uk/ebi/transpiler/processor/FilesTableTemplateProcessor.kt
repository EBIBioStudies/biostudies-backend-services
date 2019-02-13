package ac.uk.ebi.transpiler.processor

import ac.uk.ebi.transpiler.common.TEMPLATE_SEPARATOR
import ac.uk.ebi.transpiler.common.LINE_BREAK
import ac.uk.ebi.transpiler.common.FilesTableTemplate

abstract class FilesTableTemplateProcessor {
    fun chunkerize(template: String): MutableList<List<String>> =
        template.split(LINE_BREAK).mapTo(mutableListOf()) { it.split(TEMPLATE_SEPARATOR) }

    abstract fun process(template: String, baseColumns: List<String>): FilesTableTemplate
}

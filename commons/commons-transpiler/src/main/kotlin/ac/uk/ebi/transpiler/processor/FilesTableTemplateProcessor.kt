package ac.uk.ebi.transpiler.processor

import ac.uk.ebi.transpiler.common.FilesTableTemplate
import ac.uk.ebi.transpiler.common.LINE_BREAK
import ac.uk.ebi.transpiler.common.TEMPLATE_SEPARATOR

abstract class FilesTableTemplateProcessor {
    internal fun chunkerize(template: String): MutableList<List<String>> =
        if (template.isBlank()) mutableListOf()
        else template.split(LINE_BREAK).mapTo(mutableListOf()) { it.split(TEMPLATE_SEPARATOR) }

    abstract fun process(template: String, baseColumns: List<String>): FilesTableTemplate
}

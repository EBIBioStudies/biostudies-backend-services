package ac.uk.ebi.transpiler.service

import ac.uk.ebi.biostd.SubFormat

interface FilesTableTemplateTranspiler {
    fun transpile(template: String, baseColumns: List<String>, format: SubFormat): String
}

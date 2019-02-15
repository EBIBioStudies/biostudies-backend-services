package ac.uk.ebi.transpiler.mapper

import ac.uk.ebi.transpiler.common.FilesTableTemplate
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.File
import ebi.ac.uk.model.FilesTable

class FilesTableTemplateMapper {
    fun map(template: FilesTableTemplate): FilesTable {
        val attrKeys = template.header
        val files = template.rows.map {
            val attributes = it.attributes.mapIndexed { idx, attrVal -> Attribute(attrKeys[idx], attrVal) }.toList()
            File(it.path, attributes = attributes)
        }.toList()

        return FilesTable(files)
    }
}

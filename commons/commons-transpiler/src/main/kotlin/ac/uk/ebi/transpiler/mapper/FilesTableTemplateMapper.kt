package ac.uk.ebi.transpiler.mapper

import ac.uk.ebi.transpiler.common.FilesTableTemplate
import ebi.ac.uk.model.FilesTable

interface FilesTableTemplateMapper {
    fun map(template: FilesTableTemplate): FilesTable
}

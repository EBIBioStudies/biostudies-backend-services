package ac.uk.ebi.transpiler.validator

import ac.uk.ebi.transpiler.common.FilesTableTemplate
import ac.uk.ebi.transpiler.exception.InvalidDirectoryException
import ebi.ac.uk.util.collections.ifNotEmpty
import java.io.File

class FilesTableTemplateValidator {
    fun validate(template: FilesTableTemplate, filesPath: String) {
        template.rows
            .map { File("$filesPath/${it.path}") }
            .filter { it.exists().not().or(it.listFiles().isNullOrEmpty()) }
            .ifNotEmpty { throw InvalidDirectoryException(it) }
    }
}

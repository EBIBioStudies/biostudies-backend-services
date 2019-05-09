package ac.uk.ebi.transpiler.mapper

import ac.uk.ebi.transpiler.common.FilesTableTemplate
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.File
import ebi.ac.uk.model.FilesTable
import java.nio.file.Paths

class FilesTableTemplateMapper {
    fun map(template: FilesTableTemplate, filesPath: String, basePath: String): FilesTable {
        val attrKeys = template.header
        val files: MutableList<File> = mutableListOf()

        template.rows.forEach { row ->
            val path = "$filesPath/${row.path}"
            val rowFiles = Paths.get(path).toFile().listFiles()
            val attributes = row.attributes.mapIndexed { idx, attrVal -> Attribute(attrKeys[idx], attrVal) }.toList()

            rowFiles.forEach { files.add(File("$basePath/${row.path}/${it.name}", attributes = attributes)) }
        }

        return FilesTable(files)
    }
}

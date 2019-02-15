package ac.uk.ebi.transpiler.service

import ac.uk.ebi.biostd.SerializationService
import ac.uk.ebi.biostd.SubFormat
import ac.uk.ebi.transpiler.mapper.FilesTableTemplateMapper
import ac.uk.ebi.transpiler.processor.FilesTableTemplateProcessor

/**
 * Transpiler to convert files table template into page tab.
 */
class FilesTableTemplateTranspiler(
    private val templateProcessor: FilesTableTemplateProcessor = FilesTableTemplateProcessor(),
    private val templateMapper: FilesTableTemplateMapper = FilesTableTemplateMapper(),
    private val serializationService: SerializationService = SerializationService()
) {
    /**
     * Transforms a files table template to its corresponding files table page tab representation in the desired format.
     * A files table template is a document containing a list of attributes that will be applied for all the files inside
     * the folder specified by the given base columns.
     *
     * @param template The files table template content.
     * @param baseColumns A list of the columns that will be used to build the path where the files are located. These
     *        base columns should correspond to the first columns in the template.
     * @param format The desired format for the generated page tab
     */
    fun transpile(template: String, baseColumns: List<String>, format: SubFormat): String {
        val tableTemplate = templateProcessor.process(template, baseColumns)
        val filesTable = templateMapper.map(tableTemplate)

        return serializationService.serializeElement(filesTable, format)
    }
}

package ac.uk.ebi.transpiler.service

import ac.uk.ebi.biostd.SerializationService
import ac.uk.ebi.biostd.SubFormat
import ac.uk.ebi.transpiler.mapper.FilesTableTemplateMapper
import ac.uk.ebi.transpiler.processor.FilesTableTemplateProcessor

class BioImagesTranspiler(
    private val templateProcessor: FilesTableTemplateProcessor,
    private val templateMapper: FilesTableTemplateMapper,
    private val serializationService: SerializationService
) : FilesTableTemplateTranspiler {
    override fun transpile(template: String, baseColumns: List<String>, format: SubFormat): String {
        val tableTemplate = templateProcessor.process(template, baseColumns)
        val filesTable = templateMapper.map(tableTemplate)

        return serializationService.serializeElement(filesTable, format)
    }
}

package ac.uk.ebi.transpiler.service

import ac.uk.ebi.biostd.integration.SerializationConfig
import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat
import ac.uk.ebi.transpiler.mapper.FilesTableTemplateMapper
import ac.uk.ebi.transpiler.processor.FilesTableTemplateProcessor
import ac.uk.ebi.transpiler.validator.FilesTableTemplateValidator
import java.nio.file.Files
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.readText

/**
 * Transpiler to convert files table template into page tab.
 */
class FilesTableTemplateTranspiler(
    private val templateProcessor: FilesTableTemplateProcessor = FilesTableTemplateProcessor(),
    private val templateValidator: FilesTableTemplateValidator = FilesTableTemplateValidator(),
    private val templateMapper: FilesTableTemplateMapper = FilesTableTemplateMapper(),
    private val serializationService: SerializationService = SerializationConfig.serializationService()
) {
    /**
     * Transforms a files table template to its corresponding files table page tab representation in the desired format.
     * A files table template is a document containing a list of attributes that will be applied for all the files
     * inside the folder specified by the given base columns.
     *
     * @param template The files table template content.
     * @param baseColumns A list of the columns that will be used to readFromBuilder the path where the files are
     *      located. These base columns should correspond to the first columns in the template.
     * @param filesPath The path to the folder containing the files to be mapped.
     * @param basePath The prefix for the entries in the generated files table.
     * @param format The desired format for the generated page tab.
     */
    @OptIn(ExperimentalPathApi::class)
    fun transpile(
        template: String,
        baseColumns: List<String>,
        filesPath: String,
        basePath: String,
        format: SubFormat
    ): String {
        val tableTemplate = templateProcessor.process(template, baseColumns)

        templateValidator.validate(tableTemplate, filesPath)

        val filesTable = templateMapper.map(tableTemplate, filesPath, basePath)
        val file = Files.createTempFile("tempFile.txt", "")
        serializationService.serializeFileList(filesTable, format, file.toFile())
        return file.readText()
    }
}

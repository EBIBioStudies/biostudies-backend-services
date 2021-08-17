package ac.uk.ebi.biostd.integration

import ac.uk.ebi.biostd.service.FileListSerializer
import ac.uk.ebi.biostd.service.PageTabSerializationService
import ac.uk.ebi.biostd.service.PagetabSerializer
import ebi.ac.uk.util.file.ExcelReader

object SerializationConfig {
    fun serializationService(): SerializationService =
        PageTabSerializationService(excelReader(), pageTabSerializer(), fileListSerializer())

    private fun excelReader(): ExcelReader = ExcelReader()

    private fun pageTabSerializer(): PagetabSerializer = PagetabSerializer()

    private fun fileListSerializer(): FileListSerializer = FileListSerializer(excelReader(), pageTabSerializer())
}

package ac.uk.ebi.biostd.integration

import ac.uk.ebi.biostd.service.FileListSerializer
import ac.uk.ebi.biostd.service.PageTabSerializationService
import ac.uk.ebi.biostd.service.PagetabSerializer

object SerializationConfig {
    fun serializationService(): SerializationService =
        PageTabSerializationService(pageTabSerializer(), fileListSerializer())

    private fun pageTabSerializer(): PagetabSerializer = PagetabSerializer()

    private fun fileListSerializer(): FileListSerializer = FileListSerializer(pageTabSerializer())
}

package ac.uk.ebi.biostd.common

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.service.FileListSerializer
import ac.uk.ebi.biostd.service.PageTabSerializationService
import ac.uk.ebi.biostd.service.PagetabSerializer
import ac.uk.ebi.biostd.tsv.TsvSerializer as TsvSerialization
import ac.uk.ebi.biostd.tsv.serialization.TsvSerializer

object SerializationConfig {
    fun serializationService(enableTsvPagetabExtension: Boolean = false): SerializationService {
        val pagetabExtension = TsvPagetabExtension(enableTsvPagetabExtension)
        val tsvSerializer = TsvSerialization(tsvSerializer = TsvSerializer(pagetabExtension))
        val pageTabSerializer = PagetabSerializer(tsvSerializer = tsvSerializer)

        return PageTabSerializationService(
            pageTabSerializer,
            FileListSerializer(pageTabSerializer)
        )
    }
}

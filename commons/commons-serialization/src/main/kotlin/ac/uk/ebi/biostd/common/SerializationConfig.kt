package ac.uk.ebi.biostd.common

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.service.FileListSerializer
import ac.uk.ebi.biostd.service.PageTabSerializationService
import ac.uk.ebi.biostd.service.PagetabSerializer
import ac.uk.ebi.biostd.tsv.serialization.TsvSerializer
import ac.uk.ebi.biostd.tsv.TsvSerializer as TsvSerialization

object SerializationConfig {
    fun serializationService(): SerializationService {
        val tsvSerializer = TsvSerialization(tsvSerializer = TsvSerializer())
        val pageTabSerializer = PagetabSerializer(tsvSerializer = tsvSerializer)

        return PageTabSerializationService(
            pageTabSerializer,
            FileListSerializer(pageTabSerializer)
        )
    }
}

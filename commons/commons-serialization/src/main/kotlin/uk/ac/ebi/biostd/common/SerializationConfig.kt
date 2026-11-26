package uk.ac.ebi.biostd.common

import uk.ac.ebi.biostd.integration.SerializationService
import uk.ac.ebi.biostd.service.FileListSerializer
import uk.ac.ebi.biostd.service.PageTabSerializationService
import uk.ac.ebi.biostd.service.PagetabSerializer
import uk.ac.ebi.biostd.tsv.serialization.TsvSerializer
import uk.ac.ebi.biostd.tsv.TsvSerializer as TsvSerialization

object SerializationConfig {
    fun serializationService(): SerializationService {
        val tsvSerializer = TsvSerialization(tsvSerializer = TsvSerializer())
        val pageTabSerializer = PagetabSerializer(tsvSerializer = tsvSerializer)

        return PageTabSerializationService(
            pageTabSerializer,
            FileListSerializer(pageTabSerializer),
        )
    }
}

package ac.uk.ebi.biostd.integration

import ac.uk.ebi.biostd.service.PagetabSerializationService

object SerializationConfig {
    fun serializationService(): SerializationService = PagetabSerializationService()
}

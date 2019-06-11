package ac.uk.ebi.biostd.integration

import ac.uk.ebi.biostd.service.PagetabSerializationService

class SerializationConfig {

    companion object {

        fun serializationService(): SerializationService = PagetabSerializationService()
    }
}

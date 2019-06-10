package ac.uk.ebi.biostd.integration

import ac.uk.ebi.biostd.service.SerializationServiceDecorator

class SerializationConfigX {

    companion object {

        fun serializationService(): ISerializationService = SerializationServiceDecorator()
    }
}

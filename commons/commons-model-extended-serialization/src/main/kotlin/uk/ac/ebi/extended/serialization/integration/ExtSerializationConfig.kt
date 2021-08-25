package uk.ac.ebi.extended.serialization.integration

import uk.ac.ebi.extended.serialization.service.ExtSerializationService

object ExtSerializationConfig {
    fun extSerializationService(): ExtSerializationService = ExtSerializationService()
}

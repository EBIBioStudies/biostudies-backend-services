package uk.ac.ebi.biostd.extended.serialization.integration

import uk.ac.ebi.biostd.extended.serialization.service.ExtSerializationService

object ExtSerializationConfig {
    fun extSerializationService(): ExtSerializationService = ExtSerializationService()
}

package ac.uk.ebi.biostd.persistence.doc.integration

import ac.uk.ebi.biostd.common.SerializationConfig
import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import ac.uk.ebi.biostd.integration.SerializationService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import uk.ac.ebi.extended.serialization.integration.ExtSerializationConfig
import uk.ac.ebi.extended.serialization.service.ExtSerializationService

@Configuration
class SerializationConfiguration {
    @Bean
    fun serializationService(applicationProperties: ApplicationProperties): SerializationService =
        SerializationConfig.serializationService(applicationProperties.featureFlags.tsvPagetabExtension)

    @Bean
    fun extSerializationService(): ExtSerializationService = ExtSerializationConfig.extSerializationService()
}

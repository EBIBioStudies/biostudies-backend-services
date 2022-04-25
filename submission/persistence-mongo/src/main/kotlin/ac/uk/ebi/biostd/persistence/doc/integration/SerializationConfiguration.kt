package ac.uk.ebi.biostd.persistence.doc.integration

import ac.uk.ebi.biostd.integration.SerializationConfig
import ac.uk.ebi.biostd.integration.SerializationService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import uk.ac.ebi.extended.serialization.integration.ExtSerializationConfig
import uk.ac.ebi.extended.serialization.service.ExtSerializationService

@Configuration
class SerializationConfiguration {
    @Bean
    fun serializationService(): SerializationService = SerializationConfig.serializationService()

    @Bean
    fun extSerializationService(): ExtSerializationService = ExtSerializationConfig.extSerializationService()
}

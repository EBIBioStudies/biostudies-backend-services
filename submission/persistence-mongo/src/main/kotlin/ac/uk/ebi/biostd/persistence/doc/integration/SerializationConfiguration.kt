package ac.uk.ebi.biostd.persistence.doc.integration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import uk.ac.ebi.biostd.common.SerializationConfig
import uk.ac.ebi.biostd.extended.serialization.integration.ExtSerializationConfig
import uk.ac.ebi.biostd.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.biostd.integration.SerializationService

@Configuration
class SerializationConfiguration {
    @Bean
    fun serializationService(): SerializationService = SerializationConfig.serializationService()

    @Bean
    fun extSerializationService(): ExtSerializationService = ExtSerializationConfig.extSerializationService()
}

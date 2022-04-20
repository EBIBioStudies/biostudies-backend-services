package ac.uk.ebi.biostd.persistence.doc.integration

import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import ac.uk.ebi.biostd.integration.SerializationConfig
import ac.uk.ebi.biostd.integration.SerializationService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import uk.ac.ebi.extended.serialization.integration.ExtSerializationConfig
import uk.ac.ebi.extended.serialization.service.ExtFilesResolver
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import java.io.File

@Configuration
class SerializationConfiguration {
    @Bean
    fun serializationService(): SerializationService = SerializationConfig.serializationService()

    @Bean
    fun extSerializationService(): ExtSerializationService = ExtSerializationConfig.extSerializationService()

    @Bean
    fun filesResolver(props: ApplicationProperties): ExtFilesResolver = ExtFilesResolver(File(props.requestFilesPath))
}

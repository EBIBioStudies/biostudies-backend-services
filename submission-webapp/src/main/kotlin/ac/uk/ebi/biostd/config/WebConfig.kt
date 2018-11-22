package ac.uk.ebi.biostd.config

import ac.uk.ebi.biostd.SerializationService
import ac.uk.ebi.biostd.config.SerializationConfig.SerializerConfig
import ac.uk.ebi.biostd.rest.converters.PagetabConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(SerializerConfig::class)
class WebConfig {

    @Bean
    fun jsonPagetabConverter(serializationService: SerializationService) = PagetabConverter(serializationService)
}

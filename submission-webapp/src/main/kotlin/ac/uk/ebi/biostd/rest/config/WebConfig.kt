package ac.uk.ebi.biostd.rest.config

import ac.uk.ebi.biostd.config.SerializationConfig.SerializerConfig
import ac.uk.ebi.biostd.json.JsonSerializer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(SerializerConfig::class)
class WebConfig {

    @Bean
    fun jsonPagetabConverter(jsonSerializer: JsonSerializer) = JsonPagetabConverter(jsonSerializer)
}
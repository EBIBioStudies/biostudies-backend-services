package ac.uk.ebi.biostd.common.config

import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import ac.uk.ebi.biostd.submission.domain.service.JsonSchemaValidationService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import java.nio.file.Paths

@Configuration
//@Import(JsonSchemaValidationService::class)
class JsonSchemaValidationConfig {
    @Bean
    fun jsonSchemaValidationService(
        applicationProperties: ApplicationProperties
    ) : JsonSchemaValidationService {
        val schemaPath : String = applicationProperties.jsonSchemaValidationPath
        return JsonSchemaValidationService(schemaPath)

    }
}

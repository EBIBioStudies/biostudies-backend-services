package ac.uk.ebi.biostd.common.config

import ac.uk.ebi.biostd.common.config.SubmitterConfig.FilesHandlerConfig
import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import uk.ac.ebi.serialization.common.FilesResolver
import java.io.File

@Configuration
@Import(value = [WebConfig::class, FilesHandlerConfig::class])
class FileSystemConfig(
    private val applicationProperties: ApplicationProperties,
) {
    @Bean
    fun extFilesResolver(): FilesResolver = FilesResolver(File(applicationProperties.requestFilesPath))
}

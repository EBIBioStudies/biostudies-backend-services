package ac.uk.ebi.biostd.persistence.doc.test.beans

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import uk.ac.ebi.extended.serialization.service.ExtFilesResolver
import java.nio.file.Files

@Configuration
class TestConfig {
    @Bean
    fun filesResolver(): ExtFilesResolver = ExtFilesResolver(Files.createTempDirectory("ext-files").toFile())
}

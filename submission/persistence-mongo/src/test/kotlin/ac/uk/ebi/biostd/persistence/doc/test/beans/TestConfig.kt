package ac.uk.ebi.biostd.persistence.doc.test.beans

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import uk.ac.ebi.serialization.common.FilesResolver
import java.nio.file.Files

@Configuration
class TestConfig {
    @Bean
    fun filesResolver(): FilesResolver = FilesResolver(Files.createTempDirectory("ext-files").toFile())
}

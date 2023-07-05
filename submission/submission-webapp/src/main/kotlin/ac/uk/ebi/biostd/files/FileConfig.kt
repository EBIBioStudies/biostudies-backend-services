package ac.uk.ebi.biostd.files

import ac.uk.ebi.biostd.files.web.common.FilesMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class FileConfig {

    @Bean
    fun fileMapper() = FilesMapper()
}

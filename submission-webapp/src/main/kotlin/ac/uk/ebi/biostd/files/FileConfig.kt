package ac.uk.ebi.biostd.files

import ac.uk.ebi.biostd.files.service.FileManager
import ebi.ac.uk.paths.FolderResolver
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class FileConfig {

    @Bean
    fun fileManager(folder: FolderResolver) = FileManager(folder)
}

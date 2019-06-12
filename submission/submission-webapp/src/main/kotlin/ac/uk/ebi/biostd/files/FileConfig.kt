package ac.uk.ebi.biostd.files

import ac.uk.ebi.biostd.files.service.GroupFilesService
import ac.uk.ebi.biostd.files.service.UserFilesService
import ac.uk.ebi.biostd.files.web.common.FilesMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class FileConfig {

    @Bean
    fun fileManager() = UserFilesService()

    @Bean
    fun groupFilesService() = GroupFilesService()

    @Bean
    fun fileMapper() = FilesMapper()
}

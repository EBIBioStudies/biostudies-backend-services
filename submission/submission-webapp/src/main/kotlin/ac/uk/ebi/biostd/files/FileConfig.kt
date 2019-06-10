package ac.uk.ebi.biostd.files

import ac.uk.ebi.biostd.files.service.GroupFilesService
import ac.uk.ebi.biostd.files.service.UserFilesService
import ac.uk.ebi.biostd.files.web.common.FilesMapper
import ac.uk.ebi.biostd.persistence.repositories.UserGroupDataRepository
import ebi.ac.uk.paths.FolderResolver
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class FileConfig {

    @Bean
    fun fileManager() = UserFilesService()

    @Bean
    fun groupFilesService(folder: FolderResolver, repository: UserGroupDataRepository) =
        GroupFilesService(folder, repository)

    @Bean
    fun fileMapper() = FilesMapper()
}

package ac.uk.ebi.biostd.common.config

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.persistence.integration.SqlPersistenceConfig
import ac.uk.ebi.biostd.persistence.repositories.AccessPermissionRepository
import ac.uk.ebi.biostd.persistence.repositories.SubmissionDataRepository
import ac.uk.ebi.biostd.persistence.repositories.data.ProjectRepository
import ac.uk.ebi.biostd.persistence.service.UserSqlPermissionsService
import ac.uk.ebi.biostd.persistence.service.filesystem.FileSystemService
import ac.uk.ebi.biostd.persistence.service.filesystem.FilesService
import ac.uk.ebi.biostd.persistence.service.filesystem.FtpFilesService
import ebi.ac.uk.paths.SubmissionFolderResolver
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Suppress("TooManyFunctions")
@Configuration
@Import(SqlPersistenceConfig::class)
class PersistenceConfig(
    private val submissionDataRepository: SubmissionDataRepository,
    private val folderResolver: SubmissionFolderResolver,
    private val serializationService: SerializationService,
    private val permissionRepository: AccessPermissionRepository
) {

    @Bean
    fun projectRepository() = ProjectRepository(submissionDataRepository)

    @Bean
    fun ftpFilesService() = FtpFilesService(folderResolver)

    @Bean
    fun filePersistenceService() = FilesService(folderResolver, serializationService)

    @Bean
    fun userPermissionsService() = UserSqlPermissionsService(permissionRepository)

    @Bean
    fun fileSystemService(
        filesService: FilesService,
        ftpService: FtpFilesService
    ) = FileSystemService(filesService, ftpService)
}

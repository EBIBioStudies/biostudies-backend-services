package ac.uk.ebi.pmc.config

import ac.uk.ebi.biostd.integration.SerializationConfig
import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.pmc.persistence.ErrorsDocService
import ac.uk.ebi.pmc.persistence.InputFilesDocService
import ac.uk.ebi.pmc.persistence.SubmissionDocService
import ac.uk.ebi.pmc.persistence.repository.ErrorsRepository
import ac.uk.ebi.pmc.persistence.repository.InputFileRepository
import ac.uk.ebi.pmc.persistence.repository.SubFileRepository
import ac.uk.ebi.pmc.persistence.repository.SubmissionRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(value = [PersistenceConfig::class])
class AppConfig {
    @Bean
    fun serializationService() = SerializationConfig.serializationService()

    @Bean
    fun inputFilesDocService(inputFileRepository: InputFileRepository) = InputFilesDocService(inputFileRepository)

    @Bean
    fun errorsDocService(errorsRepository: ErrorsRepository, submissionRepository: SubmissionRepository) =
        ErrorsDocService(errorsRepository, submissionRepository)

    @Bean
    fun submissionDocService(
        submissionRepository: SubmissionRepository,
        submissionFileRepository: SubFileRepository,
        serializationService: SerializationService
    ) = SubmissionDocService(submissionRepository, submissionFileRepository, serializationService)
}

package ac.uk.ebi.pmc.config

import ac.uk.ebi.biostd.SerializationService
import ac.uk.ebi.pmc.persistence.MongoDocService
import ac.uk.ebi.pmc.persistence.SubmissionDocService
import ac.uk.ebi.pmc.persistence.repository.ErrorsRepository
import ac.uk.ebi.pmc.persistence.repository.InputFileRepository
import ac.uk.ebi.pmc.persistence.repository.SubFileRepository
import ac.uk.ebi.pmc.persistence.repository.SubmissionRepository
import ac.uk.ebi.scheduler.properties.PmcImporterProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(value = [PersistenceConfig::class])
class AppConfig {

    @Bean
    @ConfigurationProperties("app.data")
    fun properties() = PmcImporterProperties()

    @Bean
    fun serializationService() = SerializationService()

    @Bean
    fun mongoDocService(
        errorsRepository: ErrorsRepository,
        submissionRepository: SubmissionRepository,
        submissionFileRepository: SubFileRepository,
        inputFileRepository: InputFileRepository
    ) = MongoDocService(submissionRepository, errorsRepository, inputFileRepository)

    @Bean
    fun submissionDocService(
        submissionRepository: SubmissionRepository,
        submissionFileRepository: SubFileRepository,
        serializationService: SerializationService
    ) = SubmissionDocService(submissionRepository, submissionFileRepository, serializationService)

}

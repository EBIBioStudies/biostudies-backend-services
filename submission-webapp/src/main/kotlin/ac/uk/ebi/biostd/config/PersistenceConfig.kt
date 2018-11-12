package ac.uk.ebi.biostd.config

import ac.uk.ebi.biostd.persistence.mapping.SubmissionDbMapper
import ac.uk.ebi.biostd.persistence.repositories.SubmissionDataRepository
import ac.uk.ebi.biostd.persistence.service.SubmissionRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@Configuration
@EnableJpaRepositories(basePackageClasses = [SubmissionDataRepository::class])
class PersistenceConfig(
    private val submissionDataRepository: SubmissionDataRepository
) {

    @Bean
    fun submissionRepository(): SubmissionRepository {
        return SubmissionRepository(submissionDataRepository, submissionMapper())
    }

    @Bean
    fun submissionMapper(): SubmissionDbMapper {
        return SubmissionDbMapper()
    }
}

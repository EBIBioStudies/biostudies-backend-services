package ac.uk.ebi.biostd.config

import ac.uk.ebi.biostd.persistence.repositories.SubmissionRepository
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@Configuration
@EnableJpaRepositories(basePackageClasses = [SubmissionRepository::class])
class PersistenceConfig

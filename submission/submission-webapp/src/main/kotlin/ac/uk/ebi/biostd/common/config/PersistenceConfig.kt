package ac.uk.ebi.biostd.common.config

import ac.uk.ebi.biostd.persistence.repositories.JdbcLockExecutor
import ac.uk.ebi.biostd.persistence.repositories.LockExecutor
import ac.uk.ebi.biostd.persistence.repositories.SubmissionDataRepository
import ac.uk.ebi.biostd.persistence.service.SubFileResolver
import ac.uk.ebi.biostd.persistence.service.SubmissionRepository
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

@Configuration
@EnableJpaRepositories(basePackageClasses = [SubmissionDataRepository::class])
class PersistenceConfig(
    private val submissionDataRepository: SubmissionDataRepository,
    private val template: NamedParameterJdbcTemplate
) {

    @Bean
    fun subFileResolver(): SubFileResolver = TODO()

    @Bean
    fun submissionRepository() = SubmissionRepository(submissionDataRepository, subFileResolver())

    @Bean
    @ConditionalOnMissingBean(LockExecutor::class)
    fun lockExecutor(): LockExecutor = JdbcLockExecutor(template)
}

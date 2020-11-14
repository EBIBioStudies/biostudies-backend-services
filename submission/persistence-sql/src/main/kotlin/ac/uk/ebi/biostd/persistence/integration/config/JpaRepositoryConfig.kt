package ac.uk.ebi.biostd.persistence.integration.config

import ac.uk.ebi.biostd.persistence.model.DbSubmission
import ac.uk.ebi.biostd.persistence.repositories.SubmissionDataRepository
import com.cosium.spring.data.jpa.entity.graph.repository.support.EntityGraphJpaRepositoryFactoryBean
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@EnableJpaRepositories(
    repositoryFactoryBeanClass = EntityGraphJpaRepositoryFactoryBean::class,
    basePackageClasses = [SubmissionDataRepository::class])
@EntityScan(basePackageClasses = [DbSubmission::class])
internal class JpaRepositoryConfig

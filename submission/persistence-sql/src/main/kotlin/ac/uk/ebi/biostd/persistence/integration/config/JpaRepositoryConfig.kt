package ac.uk.ebi.biostd.persistence.integration.config

import ac.uk.ebi.biostd.persistence.model.DbUser
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import com.cosium.spring.data.jpa.entity.graph.repository.support.EntityGraphJpaRepositoryFactoryBean
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@EnableJpaRepositories(
    repositoryFactoryBeanClass = EntityGraphJpaRepositoryFactoryBean::class,
    basePackageClasses = [UserDataRepository::class]
)
@EntityScan(basePackageClasses = [DbUser::class])
internal class JpaRepositoryConfig

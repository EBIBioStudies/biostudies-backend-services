package ac.uk.ebi.biostd.itest.common

import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ebi.ac.uk.security.service.SecurityService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class TestConfig {
    @Bean
    fun securityTestService(
        userDataRepository: UserDataRepository,
        securityService: SecurityService,
    ) = SecurityTestService(securityService, userDataRepository)

    @Bean(name = ["TestCollectionValidator"])
    fun testCollectionValidator(): TestCollectionValidator = TestCollectionValidator()

    @Bean(name = ["FailCollectionValidator"])
    fun failCollectionValidator(): FailCollectionValidator = FailCollectionValidator()
}

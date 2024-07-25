package ac.uk.ebi.biostd.itest.common

import ac.uk.ebi.biostd.persistence.repositories.SequenceDataRepository
import ac.uk.ebi.biostd.persistence.repositories.UserDataDataRepository
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ebi.ac.uk.security.service.SecurityService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class TestConfig {
    @Bean
    fun securityTestService(
        userDataRepository: UserDataRepository,
        sequenceDataRepository: SequenceDataRepository,
        securityService: SecurityService,
    ) = SecurityTestService(securityService, userDataRepository, sequenceDataRepository)

    @Bean(name = ["TestCollectionValidator"])
    fun testCollectionValidator(): TestCollectionValidator = TestCollectionValidator()

    @Bean(name = ["FailCollectionValidator"])
    fun failCollectionValidator(): FailCollectionValidator = FailCollectionValidator()

    @Bean
    fun testUserDataService(userDataDataRepository: UserDataDataRepository) = TestUserDataService(userDataDataRepository)
}

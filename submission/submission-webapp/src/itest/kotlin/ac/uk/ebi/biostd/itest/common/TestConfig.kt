package ac.uk.ebi.biostd.itest.common

import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ebi.ac.uk.security.integration.components.IGroupService
import ebi.ac.uk.security.service.SecurityService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class TestConfig {

    @Bean
    fun securityTestService(
        userDataRepository: UserDataRepository,
        securityService: SecurityService,
        groupService: IGroupService
    ) = SecurityTestService(securityService, userDataRepository, groupService)
}

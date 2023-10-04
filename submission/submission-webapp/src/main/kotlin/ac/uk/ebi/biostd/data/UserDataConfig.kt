package ac.uk.ebi.biostd.data

import ac.uk.ebi.biostd.data.service.UserDataService
import ac.uk.ebi.biostd.persistence.repositories.UserDataDataRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class UserDataConfig(
    private val userDataDataRepository: UserDataDataRepository,
) {
    @Bean
    fun userDataService() = UserDataService(userDataDataRepository)
}

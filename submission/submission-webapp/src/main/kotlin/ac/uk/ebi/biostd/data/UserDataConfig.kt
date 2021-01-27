package ac.uk.ebi.biostd.data

import ac.uk.ebi.biostd.data.service.SubmissionDraftSqlService
import ac.uk.ebi.biostd.data.service.UserDataService
import ac.uk.ebi.biostd.persistence.repositories.UserDataDataRepository
import ac.uk.ebi.biostd.submission.domain.service.SubmissionService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class UserDataConfig(
    private val userDataDataRepository: UserDataDataRepository,
    private val submissionService: SubmissionService
) {
    @Bean
    fun tmpSubService() = SubmissionDraftSqlService(userDataService(), submissionService)

    @Bean
    fun userDataService() = UserDataService(userDataDataRepository)
}

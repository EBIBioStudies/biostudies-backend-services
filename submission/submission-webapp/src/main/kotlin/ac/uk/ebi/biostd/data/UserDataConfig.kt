package ac.uk.ebi.biostd.data

import ac.uk.ebi.biostd.data.service.SubmissionDraftSqlService
import ac.uk.ebi.biostd.data.service.UserDataService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionDraftService
import ac.uk.ebi.biostd.persistence.repositories.UserDataDataRepository
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ac.uk.ebi.biostd.submission.domain.service.SubmissionService
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class UserDataConfig(
    private val userDataDataRepository: UserDataDataRepository,
    private val userDataRepository: UserDataRepository,
    private val submissionService: SubmissionService
) {
    @Bean
    @ConditionalOnProperty(prefix = "app.persistence", name = ["enableMongo"], havingValue = "false")
    fun tmpSubService(): SubmissionDraftService = SubmissionDraftSqlService(userDataService(), submissionService)

    @Bean
    fun userDataService() = UserDataService(userDataDataRepository, userDataRepository)
}

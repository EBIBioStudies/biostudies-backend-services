package ac.uk.ebi.biostd.persistence.doc.integration

import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.service.SubmissionMongoMetaQueryService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(MongoDbReposConfig::class)
class MongoDbQueryConfig {
    @Bean
    internal fun submissionMongoMetaQueryService(
        submissionDocDataRepository: SubmissionDocDataRepository
    ): SubmissionMongoMetaQueryService = SubmissionMongoMetaQueryService(submissionDocDataRepository)
}

package ac.uk.ebi.biostd.persistence.doc.integration

import ac.uk.ebi.biostd.persistence.doc.MongoDbReactiveConfig
import ac.uk.ebi.biostd.persistence.doc.db.lock.DistributedLockExecutor
import ac.uk.ebi.biostd.persistence.doc.service.DistributedLockService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.data.mongodb.core.ReactiveMongoTemplate

@Configuration
@Import(MongoDbReactiveConfig::class)
class LockConfig {

    @Bean
    internal fun distributedLockExecutor(mongoTemplate: ReactiveMongoTemplate): DistributedLockExecutor {
        return DistributedLockExecutor(mongoTemplate)
    }

    @Bean
    internal fun distributedLockService(lockExecutor: DistributedLockExecutor): DistributedLockService {
        return DistributedLockService(lockExecutor)
    }
}

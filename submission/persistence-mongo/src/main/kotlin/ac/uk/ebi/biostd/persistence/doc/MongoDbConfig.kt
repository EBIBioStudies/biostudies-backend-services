package ac.uk.ebi.biostd.persistence.doc

import ac.uk.ebi.biostd.persistence.doc.db.repositories.SubmissionMongoRepository
import ac.uk.ebi.biostd.persistence.doc.migrations.CHANGE_LOG_CLASSES
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import ac.uk.ebi.biostd.persistence.doc.model.FileListDocFile
import com.github.cloudyrock.mongock.driver.mongodb.springdata.v3.SpringDataMongoV3Driver
import com.github.cloudyrock.spring.v5.MongockSpring5
import com.github.cloudyrock.spring.v5.MongockSpring5.MongockApplicationRunner
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.MongoDatabaseFactory
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory
import org.springframework.data.mongodb.core.convert.MappingMongoConverter
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories

const val CHANGE_LOG_COLLECTION = "submitter_mongockChangeLog"
const val CHANGE_LOG_LOCK = "submitter_mongockLock"

@Configuration
@EnableMongoRepositories(
    basePackageClasses = [
        SubmissionMongoRepository::class,
        DocSubmission::class,
        FileListDocFile::class
    ]
)
@EnableConfigurationProperties
class MongoDbConfig(
    @Value("\${spring.data.mongodb.database}") val mongoDatabase: String,
    @Value("\${spring.data.mongodb.uri}") val mongoUri: String,
) {

    @Bean
    @ConditionalOnProperty(prefix = "app.mongo", name = ["execute-migrations"], havingValue = "true")
    fun mongockApplicationRunner(
        springContext: ApplicationContext,
        mongoTemplate: MongoTemplate,
    ): ApplicationRunner {
        return createMongockConfig(mongoTemplate, springContext, CHANGE_LOG_CLASSES)
    }

    @Bean
    fun mongoClient(): MongoClient {
        return MongoClients.create(mongoUri)
    }

    @Bean
    fun mongoTemplate(databaseFactory: MongoDatabaseFactory, converter: MappingMongoConverter): MongoTemplate {
        return MongoTemplate(databaseFactory, converter)
    }

    @Bean
    fun mongoDbFactory(): MongoDatabaseFactory {
        return SimpleMongoClientDatabaseFactory(mongoClient(), mongoDatabase)
    }

    companion object {
        fun createMongockConfig(
            mongoTemplate: MongoTemplate,
            springContext: ApplicationContext,
            classes: List<Class<*>>,
        ): MongockApplicationRunner {
            return MongockSpring5.builder()
                .setDriver(createDriver(mongoTemplate))
                .addChangeLogClasses(classes)
                .setSpringContext(springContext)
                .buildApplicationRunner()
        }

        private fun createDriver(mongoTemplate: MongoTemplate): SpringDataMongoV3Driver? {
            val driver = SpringDataMongoV3Driver.withDefaultLock(mongoTemplate)
            driver.lockRepositoryName = CHANGE_LOG_LOCK
            driver.changeLogRepositoryName = CHANGE_LOG_COLLECTION
            return driver
        }
    }
}

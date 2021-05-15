package ac.uk.ebi.pmc.config

import ac.uk.ebi.pmc.persistence.ext.getCollection
import ac.uk.ebi.pmc.persistence.repository.ErrorsRepository
import ac.uk.ebi.pmc.persistence.repository.InputFileRepository
import ac.uk.ebi.pmc.persistence.repository.SubFileRepository
import ac.uk.ebi.pmc.persistence.repository.SubmissionRepository
import ac.uk.ebi.scheduler.properties.PmcImporterProperties
import com.github.cloudyrock.mongock.driver.mongodb.springdata.v3.SpringDataMongoV3Driver
import com.github.cloudyrock.spring.v5.MongockSpring5
import com.github.cloudyrock.spring.v5.MongockSpring5.MongockApplicationRunner
import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoClients
import com.mongodb.reactivestreams.client.MongoClient
import org.litote.kmongo.reactivestreams.KMongo
import org.springframework.boot.ApplicationRunner
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration
import org.springframework.data.mongodb.core.MongoTemplate

const val CHANGE_LOG_COLLECTION = "pmc_mongockChangeLog"
const val CHANGE_LOG_LOCK = "pmc_mongockLock"

private const val ERRORS_COL = "pmc_errors"
private const val SUBMISSION_COL = "pmc_submissions"
private const val SUB_FILES_COL = "pmc_submissions_files"
private const val INPUT_FILES_COL = "pmc_input_files"

@Configuration
class PersistenceConfig(val properties: PmcImporterProperties) : AbstractMongoClientConfiguration() {

    override fun getDatabaseName(): String = properties.mongodbDatabase

    @Bean
    override fun mongoClient(): com.mongodb.client.MongoClient {
        return MongoClients.create(properties.mongodbUri)
    }

    @Bean
    fun mongockApplicationRunner(
        springContext: ApplicationContext,
        mongoTemplate: MongoTemplate
    ): ApplicationRunner {
        return createMongockConfig(mongoTemplate, springContext, "ac.uk.ebi.pmc.migrations")
    }

    @Bean
    fun kMongoClient(): MongoClient {
        return KMongo.createClient(
            MongoClientSettings
                .builder()
                .applyConnectionString(ConnectionString(properties.mongodbUri))
                .build()
        )
    }

    @Bean
    fun errorsRepository(kMongoClient: MongoClient) =
        ErrorsRepository(kMongoClient.getCollection(properties.mongodbDatabase, ERRORS_COL))

    @Bean
    fun submissionRepository(kMongoClient: MongoClient) =
        SubmissionRepository(kMongoClient.getCollection(properties.mongodbDatabase, SUBMISSION_COL))

    @Bean
    fun submissionFileRepository(kMongoClient: MongoClient) =
        SubFileRepository(kMongoClient.getCollection(properties.mongodbDatabase, SUB_FILES_COL))

    @Bean
    fun inputFileRepository(kMongoClient: MongoClient) =
        InputFileRepository(kMongoClient.getCollection(properties.mongodbDatabase, INPUT_FILES_COL))

    companion object {
        fun createMongockConfig(
            mongoTemplate: MongoTemplate,
            springContext: ApplicationContext,
            migrationPackage: String
        ): MongockApplicationRunner {
            return MongockSpring5.builder()
                .setDriver(createDriver(mongoTemplate))
                .addChangeLogsScanPackage(migrationPackage)
                .setSpringContext(springContext)
                .buildApplicationRunner()
        }

        private fun createDriver(mongoTemplate: MongoTemplate): SpringDataMongoV3Driver {
            val driver = SpringDataMongoV3Driver.withDefaultLock(mongoTemplate)
            driver.lockRepositoryName = CHANGE_LOG_LOCK
            driver.changeLogRepositoryName = CHANGE_LOG_COLLECTION
            return driver
        }
    }
}

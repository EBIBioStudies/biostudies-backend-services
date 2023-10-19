package ac.uk.ebi.pmc.config

import ac.uk.ebi.pmc.persistence.ext.getCollection
import ac.uk.ebi.pmc.persistence.repository.ErrorsRepository
import ac.uk.ebi.pmc.persistence.repository.InputFileRepository
import ac.uk.ebi.pmc.persistence.repository.SubFileRepository
import ac.uk.ebi.pmc.persistence.repository.SubmissionRepository
import ac.uk.ebi.scheduler.properties.PmcImporterProperties
import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.MongoClients
import org.litote.kmongo.reactivestreams.KMongo
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration

const val CHANGE_LOG_COLLECTION = "pmc_mongockChangeLog"
const val CHANGE_LOG_LOCK = "pmc_mongockLock"

const val ERRORS_COL = "pmc_errors"
const val SUBMISSION_COL = "pmc_submissions"
const val SUB_FILES_COL = "pmc_submissions_files"
const val INPUT_FILES_COL = "pmc_input_files"

@Configuration
class PersistenceConfig(val properties: PmcImporterProperties) : AbstractReactiveMongoConfiguration() {

    override fun getDatabaseName(): String = properties.mongodbDatabase

    @Bean
    override fun reactiveMongoClient(): MongoClient {
        return MongoClients.create(properties.mongodbUri)
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
}

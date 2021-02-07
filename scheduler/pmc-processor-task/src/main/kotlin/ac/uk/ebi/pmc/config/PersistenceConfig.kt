package ac.uk.ebi.pmc.config

import ac.uk.ebi.pmc.persistence.ext.getCollection
import ac.uk.ebi.pmc.persistence.repository.ErrorsRepository
import ac.uk.ebi.pmc.persistence.repository.InputFileRepository
import ac.uk.ebi.pmc.persistence.repository.SubFileRepository
import ac.uk.ebi.pmc.persistence.repository.SubmissionRepository
import ac.uk.ebi.scheduler.properties.PmcImporterProperties
import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.async.client.MongoClient
import org.litote.kmongo.async.KMongo
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

private const val ERRORS_COL = "pmc_errors"
private const val SUBMISSION_COL = "pmc_submissions"
private const val SUB_FILES_COl = "pmc_submissions_files"
private const val INPUT_FILES_COl = "pmc_input_files"

@Configuration
class PersistenceConfig(val properties: PmcImporterProperties) {

    @Bean
    fun mongoClient(): MongoClient {
        return KMongo.createClient(
            MongoClientSettings
                .builder()
                .applyConnectionString(ConnectionString(properties.mongodbUri))
                .build())
    }

    @Bean
    fun errorsRepository(client: MongoClient) =
        ErrorsRepository(client.getCollection(properties.mongodbDatabase, ERRORS_COL))

    @Bean
    fun submissionRepository(client: MongoClient) =
        SubmissionRepository(client.getCollection(properties.mongodbDatabase, SUBMISSION_COL))

    @Bean
    fun submissionFileRepository(client: MongoClient) =
        SubFileRepository(client.getCollection(properties.mongodbDatabase, SUB_FILES_COl))

    @Bean
    fun inputFileRepository(client: MongoClient) =
        InputFileRepository(client.getCollection(properties.mongodbDatabase, INPUT_FILES_COl))
}

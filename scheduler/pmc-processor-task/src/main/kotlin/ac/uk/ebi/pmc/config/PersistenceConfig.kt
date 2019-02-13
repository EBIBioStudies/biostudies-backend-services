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
import java.util.concurrent.TimeUnit

@Configuration
class PersistenceConfig {

    @Bean
    fun mongoClient(properties: PmcImporterProperties): MongoClient {
        return KMongo.createClient(
            MongoClientSettings
                .builder()
                .applyConnectionString(ConnectionString(properties.mongodbUri))
                .applyToConnectionPoolSettings {
                    it.maxSize(10)
                    it.maxWaitTime(10, TimeUnit.MINUTES)
                }
                .build())
    }

    @Bean
    fun errorsRepository(client: MongoClient) = ErrorsRepository(client.getCollection("eubioimag", "errors"))

    @Bean
    fun submissionRepository(client: MongoClient) = SubmissionRepository(client.getCollection("eubioimag", "submissions"))

    @Bean
    fun submissionFileRepository(client: MongoClient) = SubFileRepository(client.getCollection("eubioimag", "submissions-files"))

    @Bean
    fun inputFileRepository(client: MongoClient) = InputFileRepository(client.getCollection("eubioimag", "input-files"))
}

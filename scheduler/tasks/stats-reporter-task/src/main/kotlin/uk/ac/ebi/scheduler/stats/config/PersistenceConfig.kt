package uk.ac.ebi.scheduler.stats.config

import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.MongoDatabaseFactory
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory
import uk.ac.ebi.scheduler.stats.persistence.StatsReporterDataRepository

@Configuration
@EnableConfigurationProperties
class PersistenceConfig(
    @Value("\${spring.data.mongodb.database}") val mongoDatabase: String,
    @Value("\${spring.data.mongodb.uri}") val mongoUri: String,
) {
    @Bean
    fun mongoClient(): MongoClient {
        return MongoClients.create(mongoUri)
    }

    @Bean
    fun mongoDbFactory(mongoClient: MongoClient): MongoDatabaseFactory {
        return SimpleMongoClientDatabaseFactory(mongoClient, mongoDatabase)
    }

    @Bean
    fun mongoTemplate(databaseFactory: MongoDatabaseFactory): MongoTemplate {
        return MongoTemplate(databaseFactory)
    }

    @Bean
    fun statsReporterRepository(
        mongoTemplate: MongoTemplate
    ): StatsReporterDataRepository = StatsReporterDataRepository(mongoTemplate)
}

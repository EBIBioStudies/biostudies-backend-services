package uk.ac.ebi.scheduler.stats.config

import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.MongoClients
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.SimpleReactiveMongoDatabaseFactory
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
    fun mongoDbFactory(mongoClient: MongoClient): ReactiveMongoDatabaseFactory {
        return SimpleReactiveMongoDatabaseFactory(mongoClient, mongoDatabase)
    }

    @Bean
    fun reactiveMongoTemplate(databaseFactory: ReactiveMongoDatabaseFactory): ReactiveMongoTemplate {
        return ReactiveMongoTemplate(databaseFactory)
    }

    @Bean
    fun statsReporterRepository(
        mongoTemplate: ReactiveMongoTemplate
    ): StatsReporterDataRepository = StatsReporterDataRepository(mongoTemplate)
}

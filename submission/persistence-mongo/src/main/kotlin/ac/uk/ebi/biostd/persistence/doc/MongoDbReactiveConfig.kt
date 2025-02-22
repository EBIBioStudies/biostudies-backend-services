package ac.uk.ebi.biostd.persistence.doc

import ac.uk.ebi.biostd.persistence.doc.migrations.executeMigrations
import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.MongoClients
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.convert.MongoCustomConversions
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories

@Configuration
@EnableConfigurationProperties
@EnableReactiveMongoRepositories
class MongoDbReactiveConfig(
    @Value("\${spring.data.mongodb.database}") val mongoDatabase: String,
    @Value("\${spring.data.mongodb.uri}") val mongoUri: String,
) : AbstractReactiveMongoConfiguration() {
    override fun getDatabaseName(): String = mongoDatabase

    @Bean
    override fun reactiveMongoClient(): MongoClient = MongoClients.create(mongoUri)

    @Bean
    override fun customConversions(): MongoCustomConversions = MongoCustomConversions(converters())

    @Bean
    @ConditionalOnProperty(prefix = "app.mongo", name = ["execute-migrations"], havingValue = "true")
    fun createApplicationRunner(reactiveMongoClient: ReactiveMongoTemplate): ApplicationRunner =
        ApplicationRunner {
            runBlocking {
                reactiveMongoClient.executeMigrations()
            }
        }
}

package ac.uk.ebi.biostd.persistence.doc

import ac.uk.ebi.biostd.persistence.doc.db.reactive.repositories.SubmissionDraftRepository
import ac.uk.ebi.biostd.persistence.doc.migrations.executeMigrations
import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.MongoClients
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.convert.MongoCustomConversions
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories
import java.util.concurrent.TimeUnit

@Configuration
@EnableConfigurationProperties
@EnableReactiveMongoRepositories(
    basePackageClasses = [
        SubmissionDraftRepository::class,
    ]
)
class MongoDbReactiveConfig(
    @Value("\${spring.data.mongodb.database}") val mongoDatabase: String,
    @Value("\${spring.data.mongodb.uri}") val mongoUri: String,
) : AbstractReactiveMongoConfiguration() {

    override fun getDatabaseName(): String = mongoDatabase

    @Bean
    @Suppress("MagicNumber")
    override fun reactiveMongoClient(): MongoClient {
        val settings = MongoClientSettings.builder()
            .applyConnectionString(ConnectionString(mongoUri))
            .applyToConnectionPoolSettings {
                it.maxSize(30)
                it.maxConnectionIdleTime(60, TimeUnit.SECONDS)
                it.minSize(10)
            }
            .applyToSocketSettings {
                it.connectTimeout(60, TimeUnit.SECONDS)
            }
            .build()
        return MongoClients.create(settings)
    }

    @Bean
    override fun customConversions(): MongoCustomConversions {
        return MongoCustomConversions(converters())
    }

    @Bean
    @ConditionalOnProperty(prefix = "app.mongo", name = ["execute-migrations"], havingValue = "true")
    fun createApplicationRunner(reactiveMongoClient: ReactiveMongoTemplate): ApplicationRunner {
        return object : ApplicationRunner {
            override fun run(args: ApplicationArguments) {
                runBlocking {
                    reactiveMongoClient.executeMigrations()
                }
            }
        }
    }
}

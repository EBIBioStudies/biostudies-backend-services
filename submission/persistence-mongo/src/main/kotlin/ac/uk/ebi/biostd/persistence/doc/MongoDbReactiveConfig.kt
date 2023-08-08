package ac.uk.ebi.biostd.persistence.doc

import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.MongoClients
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration
import org.springframework.data.mongodb.core.convert.MongoCustomConversions

@Configuration
@EnableConfigurationProperties
class MongoDbReactiveConfig(
    @Value("\${spring.data.mongodb.database}") val mongoDatabase: String,
    @Value("\${spring.data.mongodb.uri}") val mongoUri: String,
) : AbstractReactiveMongoConfiguration() {

    override fun getDatabaseName(): String = mongoDatabase

    @Bean
    override fun reactiveMongoClient(): MongoClient {
        return MongoClients.create(mongoUri)
    }

    @Bean
    override fun customConversions(): MongoCustomConversions {
        return MongoCustomConversions(converters())
    }
}

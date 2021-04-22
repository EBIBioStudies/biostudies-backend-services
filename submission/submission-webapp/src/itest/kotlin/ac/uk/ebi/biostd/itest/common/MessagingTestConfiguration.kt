package ac.uk.ebi.biostd.itest.common

import com.github.fridujo.rabbitmq.mock.MockConnectionFactory
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.context.annotation.Configuration

@Configuration
internal class MessagingTestConfiguration {
//    @Bean
    fun connectionFactory(): ConnectionFactory {
        return CachingConnectionFactory(MockConnectionFactory())
    }
}

package ac.uk.ebi.biostd.itest.common

import org.springframework.amqp.rabbit.core.RabbitAdmin
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
internal class MessagingTestConfiguration {
    @Bean
    fun rabbitAdmin(rabbitTemplate: RabbitTemplate): RabbitAdmin = RabbitAdmin(rabbitTemplate)
}

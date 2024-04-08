package ac.uk.ebi.biostd.handlers.config

import ac.uk.ebi.biostd.common.events.BIOSTUDIES_EXCHANGE
import ac.uk.ebi.biostd.common.events.SECURITY_NOTIFICATIONS_ROUTING_KEY
import ac.uk.ebi.biostd.common.events.SUBMISSIONS_PUBLISHED_ROUTING_KEY
import ac.uk.ebi.biostd.common.events.SUBMISSIONS_ROUTING_KEY
import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.TopicExchange
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

const val NOTIFICATIONS_FAILED_REQUEST_ROUTING_KEY = "bio.notification.failed"
const val SUBMISSIONS_FAILED_REQUEST_ROUTING_KEY = "bio.submission.failed"

internal const val LOG_QUEUE = "submission-submitted-log-queue"
internal const val SECURITY_NOTIFICATIONS_QUEUE = "security-notifications-queue"
internal const val SUBMIT_NOTIFICATIONS_QUEUE = "submission-submitted-notifications-queue"
internal const val RELEASE_NOTIFICATIONS_QUEUE = "submission-released-notifications-queue"
internal const val FAILED_SUBMISSIONS_NOTIFICATIONS_QUEUE = "submission-failed-notifications-queue"

private const val ST_LOG_QUEUE = "submission-submitted-st-queue"

private const val DURABLES_QUEUES = true

@Configuration
@Suppress("TooManyFunctions")
class QueuesConfig {
    @Bean
    fun exchange(): TopicExchange = TopicExchange(BIOSTUDIES_EXCHANGE)

    @Bean
    fun logQueue(): Queue = Queue(LOG_QUEUE, DURABLES_QUEUES)

    @Bean
    fun submissionToolQueue(): Queue = Queue(ST_LOG_QUEUE, DURABLES_QUEUES)

    @Bean
    fun notificationsQueue(): Queue = Queue(SUBMIT_NOTIFICATIONS_QUEUE, DURABLES_QUEUES)

    @Bean
    fun releaseNotificationsQueue(): Queue = Queue(RELEASE_NOTIFICATIONS_QUEUE, DURABLES_QUEUES)

    @Bean
    fun failedSubmissionNotificationsQueue(): Queue = Queue(FAILED_SUBMISSIONS_NOTIFICATIONS_QUEUE, DURABLES_QUEUES)

    @Bean
    fun securityNotificationsQueue(): Queue = Queue(SECURITY_NOTIFICATIONS_QUEUE, DURABLES_QUEUES)

    @Bean
    fun logQueueBinding(exchange: TopicExchange): Binding = BindingBuilder.bind(logQueue()).to(exchange).with(SUBMISSIONS_ROUTING_KEY)

    @Bean
    fun stQueueBinding(exchange: TopicExchange): Binding =
        BindingBuilder.bind(submissionToolQueue()).to(exchange).with(SUBMISSIONS_ROUTING_KEY)

    @Bean
    fun notificationsQueueBinding(exchange: TopicExchange): Binding =
        BindingBuilder.bind(notificationsQueue()).to(exchange).with(SUBMISSIONS_ROUTING_KEY)

    @Bean
    fun releaseNotificationsQueueBinding(exchange: TopicExchange): Binding =
        BindingBuilder.bind(releaseNotificationsQueue()).to(exchange).with(SUBMISSIONS_PUBLISHED_ROUTING_KEY)

    @Bean
    fun failedSubmissionNotificationQueueBinding(exchange: TopicExchange): Binding =
        BindingBuilder
            .bind(failedSubmissionNotificationsQueue())
            .to(exchange)
            .with(SUBMISSIONS_FAILED_REQUEST_ROUTING_KEY)

    @Bean
    fun securityNotificationsQueueBinding(exchange: TopicExchange): Binding =
        BindingBuilder.bind(securityNotificationsQueue()).to(exchange).with(SECURITY_NOTIFICATIONS_ROUTING_KEY)
}

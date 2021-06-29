package ac.uk.ebi.biostd.itest.test.submission.submit.async

import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.TSV
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.common.config.JmsConfig
import ac.uk.ebi.biostd.itest.common.BaseIntegrationTest
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.persistence.common.service.SubmissionQueryService
import ac.uk.ebi.biostd.submission.ext.getSimpleByAccNo
import ebi.ac.uk.await.untilNotNull
import ebi.ac.uk.dsl.line
import ebi.ac.uk.dsl.submission
import ebi.ac.uk.dsl.tsv
import ebi.ac.uk.model.extensions.releaseDate
import ebi.ac.uk.model.extensions.title
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.Binding.DestinationType.QUEUE
import org.springframework.amqp.core.Queue
import org.springframework.amqp.rabbit.core.RabbitAdmin
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.core.ParameterizedTypeReference
import org.springframework.web.client.RestTemplate
import uk.ac.ebi.events.config.BIOSTUDIES_EXCHANGE
import uk.ac.ebi.events.config.SUBMISSIONS_ROUTING_KEY
import java.util.concurrent.TimeUnit.SECONDS

private const val QUEUE_NAME = "submissions_queue"

@ExtendWith(TemporaryFolderExtension::class)
internal class SubmitAsyncApiTest(tempFolder: TemporaryFolder) : BaseIntegrationTest(tempFolder) {

    @Nested
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    inner class SubmitAsyncTSV(
        @Autowired private val securityTestService: SecurityTestService,
        @Autowired private val rabbitTemplate: RabbitTemplate,
        @Autowired private val rabbitAdmin: RabbitAdmin,
        @Autowired private val submissionRepository: SubmissionQueryService,
        @Autowired private val jmsConfig: JmsConfig
    ) {
        @LocalServerPort
        private var serverPort: Int = 0

        private lateinit var webClient: BioWebClient
        val restTemplate = RestTemplate()

        @BeforeAll
        fun init() {
            securityTestService.registerUser(SuperUser)
            webClient = getWebClient(serverPort, SuperUser)
            createQueue(QUEUE_NAME, BIOSTUDIES_EXCHANGE, SUBMISSIONS_ROUTING_KEY)
        }

        @Test
        fun submissionsNotification() {
            val submissionTSV = tsv {
                line("Submission", "S-TEST22")
                line("Title", "Test title")
                line("ReleaseDate", "2000-01-31")
                line()
            }.toString()

            webClient.submitAsync(submissionTSV, TSV)

            val message = await().atMost(10, SECONDS).untilNotNull {
                rabbitTemplate.receiveAndConvert(QUEUE_NAME, MapType)
            }

            val pageTabUrlJSON: String = message.getValue("pagetabUrl").replace("8080", "$serverPort")
            val pageTabUrlTSV = pageTabUrlJSON.replace("json", "tsv")
            val result = restTemplate.getForObject(pageTabUrlTSV, String::class.java)

            assertThat(result).isNotNull()
            assertThat(result).isEqualToIgnoringWhitespace(submissionTSV)
            assertThat(submissionRepository.getSimpleByAccNo("S-TEST22")).isEqualTo(
                submission("S-TEST22") {
                    title = "Test title"
                    releaseDate = "2000-01-31"
                }
            )
        }

        private fun createQueue(queueName: String, exchangeName: String, routingKey: String) {
            val queue = Queue(queueName)
            rabbitAdmin.declareQueue(queue)
            rabbitAdmin.purgeQueue(queue.name)
            rabbitAdmin.declareExchange(jmsConfig.exchange())
            rabbitAdmin.declareBinding(Binding(queue.name, QUEUE, exchangeName, routingKey, null))
        }
    }
}

object MapType : ParameterizedTypeReference<Map<String, String>>()

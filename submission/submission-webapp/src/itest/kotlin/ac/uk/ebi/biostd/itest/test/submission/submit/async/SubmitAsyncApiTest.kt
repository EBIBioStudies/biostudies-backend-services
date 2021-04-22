package ac.uk.ebi.biostd.itest.test.submission.submit.async

import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.TSV
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.common.config.JmsConfig
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.common.BaseAsyncIntegrationTest
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ebi.ac.uk.dsl.line
import ebi.ac.uk.dsl.tsv
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

@ExtendWith(TemporaryFolderExtension::class)
internal class SubmitAsyncApiTest(private val tempFolder: TemporaryFolder) : BaseAsyncIntegrationTest(tempFolder) {

    @Nested
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    inner class SubmitAsyncTSV(
        @Autowired private val securityTestService: SecurityTestService,
        @Autowired private val myRabbitTemplate: RabbitTemplate,
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
        }

        @Test
        fun test() {
            val submissionTSV = tsv {
                line("Submission", "S-TEST22")
                line("Title", "Test title")
                line("ReleaseDate", "2000-01-31")
                line()
//                line("Study", "SECT-001")
//                line("File List", "FileList.tsv")
            }.toString()

            val queue = Queue("myTestQueue")
            val exchange = jmsConfig.exchange()
            val binding = Binding(queue.name, QUEUE, BIOSTUDIES_EXCHANGE, SUBMISSIONS_ROUTING_KEY, null)
            val admin = RabbitAdmin(myRabbitTemplate)
            admin.declareQueue(queue)
            admin.purgeQueue("myTestQueue")
            admin.declareExchange(exchange)
            admin.declareBinding(binding)

            webClient.submitAsync(submissionTSV, TSV)

            var message: Map<String, String>? = null

            await().atMost(10, SECONDS).until {
                message = myRabbitTemplate.receiveAndConvert(queue.name,
                    object : ParameterizedTypeReference<Map<String, String>>() {})
                message != null
            }

            val pageTabUrlJSON: String = message?.get("pagetabUrl")!!.replace("8080", "$serverPort")
            val pageTabUrlTSV = pageTabUrlJSON.removeSuffix("json") + "tsv"
            val result = restTemplate.getForObject(pageTabUrlTSV, String::class.java)

            assertThat(result.trim()).isEqualTo(submissionTSV.trim())
        }
    }
}

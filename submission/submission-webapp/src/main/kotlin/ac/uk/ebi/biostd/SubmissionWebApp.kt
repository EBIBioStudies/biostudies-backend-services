package ac.uk.ebi.biostd

import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(value = [ApplicationProperties::class])
class SubmissionWebApp

@Suppress("SpreadOperator")
fun main(args: Array<String>) {
    runApplication<SubmissionWebApp>(*args)
}

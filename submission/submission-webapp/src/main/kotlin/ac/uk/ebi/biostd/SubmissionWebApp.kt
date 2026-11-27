package ac.uk.ebi.biostd

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import uk.ac.ebi.biostd.common.properties.ApplicationProperties

@SpringBootApplication
@EnableConfigurationProperties(value = [ApplicationProperties::class])
class SubmissionWebApp

@Suppress("SpreadOperator")
fun main(args: Array<String>) {
    runApplication<SubmissionWebApp>(*args)
}

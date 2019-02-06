package ac.uk.ebi.biostd

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SubmissionWebApp

@Suppress("SpreadOperator")
fun main(args: Array<String>) {
    runApplication<SubmissionWebApp>(*args)
}

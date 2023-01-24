package ac.uk.ebi.biostd

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
@Suppress("UtilityClassWithPublicConstructor")
class SubmissionWebApp {
    companion object {
        @JvmStatic
        @Suppress("SpreadOperator")
        fun main(args: Array<String>) {
            runApplication<SubmissionWebApp>(*args)
        }
    }
}

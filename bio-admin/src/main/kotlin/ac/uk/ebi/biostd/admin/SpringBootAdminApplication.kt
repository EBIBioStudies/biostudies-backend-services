package ac.uk.ebi.biostd.admin

import de.codecentric.boot.admin.server.config.EnableAdminServer
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Configuration

@Configuration
@EnableAutoConfiguration
@EnableAdminServer
class SpringBootAdminApplication

@Suppress("SpreadOperator")
fun main(args: Array<String>) {
    runApplication<SpringBootAdminApplication>(*args)
}

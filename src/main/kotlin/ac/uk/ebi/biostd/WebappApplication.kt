package ac.uk.ebi.biostd

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class WebappApplication

fun main(args: Array<String>) {
    runApplication<WebappApplication>(*args)
}
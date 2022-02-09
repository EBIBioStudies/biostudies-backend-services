package ac.uk.ebi.pmc

import ac.uk.ebi.pmc.config.AppConfig
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.mongo.MongoReactiveAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(value = [AppConfig::class])
class MainConfig {

    @Bean
    fun commandLineRunner(pmcTaskExecutor: PmcTaskExecutor) = PmcCommandLineRunner(pmcTaskExecutor)
}

@SpringBootApplication(exclude = [MongoReactiveAutoConfiguration::class])
class PmcProcessorApp

@Suppress("SpreadOperator")
fun main(args: Array<String>) {
    runApplication<PmcProcessorApp>(*args)
}

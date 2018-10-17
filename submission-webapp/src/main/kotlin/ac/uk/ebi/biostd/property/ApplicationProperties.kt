package ac.uk.ebi.biostd.property

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource
import java.nio.file.Path

@Configuration
@PropertySource("classpath:application.yml")
@ConfigurationProperties(prefix = "app")
class ApplicationProperties {

    lateinit var basePath: Path
}
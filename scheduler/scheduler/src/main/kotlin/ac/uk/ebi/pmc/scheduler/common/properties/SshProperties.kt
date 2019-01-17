package ac.uk.ebi.pmc.scheduler.common.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@ConfigurationProperties("app.ssh")
@Component
class SshProperties {

    lateinit var user: String
    lateinit var password: String
    lateinit var server: String
}
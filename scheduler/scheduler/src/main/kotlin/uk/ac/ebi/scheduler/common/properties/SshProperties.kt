package uk.ac.ebi.scheduler.common.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@ConfigurationProperties("app.ssh")
@Component
class SshProperties {
    lateinit var sshKey: String
    lateinit var server: String
}

package ac.uk.ebi.biostd.common.properties

import java.util.Properties

data class NotificationProperties(
    val smtp: String,
    val uiUrl: String,
    val stUrl: String,
    val slackUrl: String,
    val bccEmail: String?,

    @NestedConfigurationProperty
    val rt: RtConfig,
) {
    fun asProperties(): Properties = Properties().apply { setProperty("mail.host", smtp) }
}

data class RtConfig(
    val host: String,
    val queue: String,
    val user: String,
    val password: String,
)

package ebi.ac.uk.notifications.integration

import java.util.Properties

class NotificationProperties {
    lateinit var smtp: String

    fun asProperties(): Properties = Properties().apply { setProperty("mail.host", smtp) }
}

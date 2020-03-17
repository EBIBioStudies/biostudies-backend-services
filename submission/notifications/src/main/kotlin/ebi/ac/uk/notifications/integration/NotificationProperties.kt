package ebi.ac.uk.notifications.integration

import java.util.Properties

class NotificationProperties {
    lateinit var smtp: String
    var submissionNotification: Boolean = false

    fun asProperties(): Properties = Properties().apply { setProperty("mail.host", smtp) }
}

package ebi.ac.uk.notifications.integration

import java.util.Properties

class NotificationProperties(private val stmp: String) {
    fun asProperties(): Properties = Properties().apply { setProperty("mail.host", stmp) }
}

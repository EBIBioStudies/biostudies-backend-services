package ebi.ac.uk.commons.http.slack

import java.awt.Color
import java.net.InetAddress

private const val NOTIFICATION_DESCRIPTION = "BioStudies Backend Notification System"

sealed class SystemNotification(
    private val system: String,
    private val subSystem: String,
    private val message: String?
) {
    internal fun asNotification(): Notification {
        return when (this) {
            is Report -> Notification(
                text = "Report [${InetAddress.getLocalHost().hostName}]",
                attachments = listOf(
                    Attachment(
                        fallback = NOTIFICATION_DESCRIPTION,
                        color = Color.BLUE.toHex(),
                        pretext = "$system[$subSystem] Notification",
                        text = message
                    )
                )
            )
            is Alert -> Notification(
                text = "Error Report [${InetAddress.getLocalHost().hostName}]",
                attachments = listOf(
                    Attachment(
                        fallback = NOTIFICATION_DESCRIPTION,
                        color = Color.RED.toHex(),
                        pretext = "$system[$subSystem]",
                        text = message,
                        fields = listOf(Field("Exception Error", errorMessage.orEmpty()))
                    )
                )
            )
        }
    }
}

class Report(system: String, subSystem: String, message: String) :
    SystemNotification(system, subSystem, message)

class Alert(system: String, subSystem: String, message: String, val errorMessage: String? = null) :
    SystemNotification(system, subSystem, message)

private fun Color.toHex(): String = Integer.toHexString(rgb).substring(2)

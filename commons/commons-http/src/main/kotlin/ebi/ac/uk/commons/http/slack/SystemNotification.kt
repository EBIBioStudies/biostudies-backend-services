package ebi.ac.uk.commons.http.slack

import java.awt.Color
import java.net.InetAddress

private const val NOTIFICATION_DESCRIPTION = "biostudies backend system notification"

sealed class SystemNotification(
    private val system: String,
    private val subSystem: String,
    private val message: String?
) {

    internal fun asNotification(): Notification {
        return when (this) {
            is ReportNotification -> Notification(
                text = "Report [${InetAddress.getLocalHost().hostName}]",
                attachments = listOf(Attachment(
                    fallback = NOTIFICATION_DESCRIPTION,
                    color = Color.BLUE.toHex(),
                    pretext = "$system[$subSystem] Notification",
                    text = message)))
            is ErrorNotification -> Notification(
                text = "Error Report [${InetAddress.getLocalHost().hostName}]",
                attachments = listOf(Attachment(
                    fallback = NOTIFICATION_DESCRIPTION,
                    color = Color.RED.toHex(),
                    pretext = "$system[$subSystem] Notification",
                    text = message,
                    fields = listOf(Field("Exception Error", errorMessage.orEmpty()))
                )))
        }
    }
}

class ReportNotification(system: String, subSystem: String, message: String) :
    SystemNotification(system, subSystem, message)

class ErrorNotification(system: String, subSystem: String, message: String, val errorMessage: String?) :
    SystemNotification(system, subSystem, message)

private fun Color.toHex(): String = Integer.toHexString(rgb).substring(2)

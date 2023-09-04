package ebi.ac.uk.commons.http.slack

import ebi.ac.uk.commons.http.ext.RequestParams
import ebi.ac.uk.commons.http.ext.post
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.web.reactive.function.client.WebClient

class NotificationsSender(
    private val client: WebClient,
    private val notificationUrl: String,
) {
    fun send(notification: SystemNotification) {
        val headers = HttpHeaders().apply {
            accept = listOf(APPLICATION_JSON)
            contentType = APPLICATION_JSON
        }

        client.post(notificationUrl, RequestParams(headers, notification.asNotification()))
    }
}

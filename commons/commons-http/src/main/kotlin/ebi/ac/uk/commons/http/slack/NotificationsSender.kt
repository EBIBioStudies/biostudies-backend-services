package ebi.ac.uk.commons.http.slack

import ebi.ac.uk.commons.http.builder.httpHeadersOf
import ebi.ac.uk.commons.http.ext.RequestParams
import ebi.ac.uk.commons.http.ext.postForObjectAsync
import org.springframework.http.HttpHeaders.ACCEPT
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.web.reactive.function.client.WebClient

class NotificationsSender(
    private val client: WebClient,
    private val notificationUrl: String,
) {
    suspend fun send(notification: SystemNotification) {
        val headers = httpHeadersOf(ACCEPT to APPLICATION_JSON, CONTENT_TYPE to APPLICATION_JSON)
        client.postForObjectAsync<String>(notificationUrl, RequestParams(headers, notification.asNotification()))
    }
}

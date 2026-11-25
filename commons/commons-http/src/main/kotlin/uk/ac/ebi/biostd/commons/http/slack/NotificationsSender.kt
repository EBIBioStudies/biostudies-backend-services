package uk.ac.ebi.biostd.commons.http.slack

import org.springframework.http.HttpHeaders.ACCEPT
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.web.reactive.function.client.WebClient
import uk.ac.ebi.biostd.commons.http.builder.httpHeadersOf
import uk.ac.ebi.biostd.commons.http.ext.RequestParams
import uk.ac.ebi.biostd.commons.http.ext.postForObjectAsync

class NotificationsSender(
    private val client: WebClient,
    private val notificationUrl: String,
) {
    suspend fun send(notification: SystemNotification) {
        val headers = httpHeadersOf(ACCEPT to APPLICATION_JSON, CONTENT_TYPE to APPLICATION_JSON)
        client.postForObjectAsync<String>(notificationUrl, RequestParams(headers, notification.asNotification()))
    }
}

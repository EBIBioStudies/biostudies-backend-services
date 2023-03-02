package ebi.ac.uk.commons.http.slack

import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForEntity

open class NotificationsSender(
    private val restTemplate: RestTemplate,
    private val notificationUrl: String
) {
    fun send(notification: SystemNotification) {
        val headers = HttpHeaders().apply {
            accept = listOf(APPLICATION_JSON)
            contentType = APPLICATION_JSON
        }

        restTemplate.postForEntity<String>(notificationUrl, HttpEntity(notification.asNotification(), headers))
    }
}

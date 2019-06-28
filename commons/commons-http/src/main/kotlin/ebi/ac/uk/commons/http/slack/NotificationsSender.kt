package ebi.ac.uk.commons.http.slack

import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForEntity

class NotificationsSender(
    private val restTemplate: RestTemplate,
    private val notificationUrl: String
) {

    fun sent(system: String, notification: Notification) {
        restTemplate.postForEntity<String>(notificationUrl, "*[$system]* ${notification.text}")
    }
}

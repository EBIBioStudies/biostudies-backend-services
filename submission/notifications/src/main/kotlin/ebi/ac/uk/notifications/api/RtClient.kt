package ebi.ac.uk.notifications.api

import arrow.core.getOrElse
import ebi.ac.uk.notifications.exception.InvalidResponseException
import ebi.ac.uk.notifications.exception.InvalidTicketIdException
import ebi.ac.uk.notifications.integration.RtConfig
import ebi.ac.uk.util.collections.second
import ebi.ac.uk.util.regex.match
import ebi.ac.uk.util.regex.secondGroup
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForEntity
import org.springframework.web.util.UriComponentsBuilder

class RtClient(
    private val rtConfig: RtConfig,
    private val restTemplate: RestTemplate
) {
    private val ticketIdPattern = "(# Ticket )(\\d+)( created.)".toPattern()

    fun createTicket(subject: String, owner: String, content: String): String {
        val url = UriComponentsBuilder
            .fromUriString("${rtConfig.host}/REST/1.0/ticket/new")
            .queryParam("user", rtConfig.user)
            .queryParam("pass", rtConfig.password)
            .build()
            .toUriString()
        val body = getRequestBody(subject, owner, content)
        val response = restTemplate.postForEntity<String>(url, body).body ?: throw InvalidResponseException()

        return getTicketId(response)
    }

    private fun getRequestBody(subject: String, owner: String, content: String) = LinkedMultiValueMap<String, String>(
        mapOf("content" to listOf(
            "Queue: ${rtConfig.queue}\nSubject: $subject\nRequestor: $owner\nText: ${content.replace("\n", "\n ")}")))

    private fun getTicketId(response: String): String {
        val body = response.split("\n\n")

        require(body.size > 2) { throw InvalidTicketIdException() }
        return ticketIdPattern
            .match(body.second())
            .map { it.secondGroup() }
            .getOrElse { throw InvalidTicketIdException() }
    }
}

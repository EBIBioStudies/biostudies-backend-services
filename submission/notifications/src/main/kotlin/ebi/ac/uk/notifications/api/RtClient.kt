package ebi.ac.uk.notifications.api

import ac.uk.ebi.biostd.common.properties.RtConfig
import ebi.ac.uk.notifications.exception.InvalidResponseException
import ebi.ac.uk.notifications.exception.InvalidTicketIdException
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

    fun createTicket(accNo: String, subject: String, owner: String, content: String): String {
        val requestContent = ticketContent(accNo, subject, owner, content)
        val response = performRtRequest("/ticket/new", requestContent)

        return getTicketId(response)
    }

    fun commentTicket(ticketId: String, comment: String) {
        val content = ticketComment(ticketId, comment)
        performRtRequest("/ticket/$ticketId/comment", content)
    }

    private fun performRtRequest(path: String, content: String): String {
        val rtUrl = UriComponentsBuilder
            .fromUriString("${rtConfig.host}/REST/1.0$path")
            .queryParam("user", rtConfig.user)
            .queryParam("pass", rtConfig.password)
            .build()
            .toUriString()
        val body = LinkedMultiValueMap<String, String>(mapOf("content" to listOf(content)))

        return restTemplate.postForEntity<String>(rtUrl, body).body ?: throw InvalidResponseException()
    }

    private fun getTicketId(response: String): String {
        val body = response.split("\n\n")

        require(body.size > 2) { throw InvalidTicketIdException() }
        return ticketIdPattern.match(body.second())?.secondGroup() ?: throw InvalidTicketIdException()
    }

    private fun ticketComment(ticketId: String, comment: String) =
        StringBuilder("id: $ticketId\n")
            .append("Action: correspond\n")
            .append("Text: ${trimContent(comment)}")
            .toString()

    private fun ticketContent(accNo: String, subject: String, owner: String, content: String) =
        StringBuilder("Queue: ${rtConfig.queue}\n")
            .append("Subject: $subject\n")
            .append("Status: resolved\n")
            .append("Requestor: $owner\n")
            .append("CF-Accession: $accNo\n")
            .append("Text: ${trimContent(content)}")
            .toString()

    private fun trimContent(content: String) = content.replace("\n", "\n ")
}

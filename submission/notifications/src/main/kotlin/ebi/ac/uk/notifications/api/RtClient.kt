package ebi.ac.uk.notifications.api

import ac.uk.ebi.biostd.common.properties.RtConfig
import ebi.ac.uk.commons.http.ext.RequestParams
import ebi.ac.uk.commons.http.ext.retrieveBlocking
import ebi.ac.uk.notifications.exception.InvalidResponseException
import ebi.ac.uk.notifications.exception.InvalidTicketIdException
import ebi.ac.uk.util.collections.second
import ebi.ac.uk.util.regex.match
import ebi.ac.uk.util.regex.secondGroup
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriComponentsBuilder

class RtClient(
    private val rtConfig: RtConfig,
    private val client: WebClient,
) {
    private val ticketIdPattern = "(# Ticket )(\\d+)( created.)".toPattern()

    fun createTicket(
        accNo: String,
        subject: String,
        owner: String,
        adminCc: String?,
        content: String,
    ): String {
        val requestContent = ticketContent(accNo, subject, owner, adminCc, content)
        val response = performRtRequest("/ticket/new", requestContent)
        return getTicketId(response)
    }

    fun commentTicket(
        ticketId: String,
        ccUser: String?,
        comment: String,
    ) {
        val content = ticketCommentContent(ticketId, ccUser, comment)
        performRtRequest("/ticket/$ticketId/comment", content)
    }

    private fun performRtRequest(
        path: String,
        content: String,
    ): String {
        val rtUrl =
            UriComponentsBuilder
                .fromUriString("${rtConfig.host}/REST/1.0$path")
                .queryParam("user", rtConfig.user)
                .queryParam("pass", rtConfig.password)
                .build()
                .toUriString()
        val body = LinkedMultiValueMap(mapOf("content" to listOf(content)))

        return client.post()
            .retrieveBlocking<String>(rtUrl, RequestParams(body = body))
            ?: throw InvalidResponseException()
    }

    private fun getTicketId(response: String): String {
        val body = response.split("\n\n")

        require(body.size > 2) { throw InvalidTicketIdException() }
        return ticketIdPattern.match(body.second())?.secondGroup() ?: throw InvalidTicketIdException()
    }

    private fun ticketCommentContent(
        ticketId: String,
        ccUser: String?,
        comment: String,
    ): String {
        return buildString {
            appendLine("id: $ticketId")
            appendLine("Action: correspond")
            appendLine("Status: resolved")
            ccUser?.let { appendLine("Bcc: $it") }
            append("Text: ${trimContent(comment)}")
        }
    }

    private fun ticketContent(
        accNo: String,
        subject: String,
        owner: String,
        ccUser: String?,
        content: String,
    ): String {
        return buildString {
            appendLine("Queue: ${rtConfig.queue}")
            appendLine("Subject: $subject")
            appendLine("Status: resolved")
            appendLine("Requestor: $owner")
            ccUser?.let { appendLine("AdminCc: $it") }
            appendLine("CF-Accession: $accNo")
            append("Text: ${trimContent(content)}")
        }
    }

    private fun trimContent(content: String) = content.replace("\n", "\n ")
}

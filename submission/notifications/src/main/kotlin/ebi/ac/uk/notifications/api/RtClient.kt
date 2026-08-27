package ebi.ac.uk.notifications.api

import ac.uk.ebi.biostd.common.properties.RtConfig
import ebi.ac.uk.commons.http.builder.httpHeadersOf
import ebi.ac.uk.commons.http.ext.RequestParams
import ebi.ac.uk.commons.http.ext.retrieveBlocking
import ebi.ac.uk.notifications.exception.InvalidResponseException
import ebi.ac.uk.notifications.exception.InvalidTicketIdException
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriComponentsBuilder

class RtClient(
    private val rtConfig: RtConfig,
    private val client: WebClient,
) {
    fun createTicket(
        accNo: String,
        subject: String,
        owner: String,
        adminCc: String?,
        content: String,
    ): String {
        val requestContent = ticketContent(accNo, subject, owner, adminCc, content)
        val response = performRtRequest("/ticket", requestContent)
        return getTicketId(response)
    }

    @Suppress("UNUSED_PARAMETER")
    fun commentTicket(
        ticketId: String,
        ccUser: String?,
        comment: String,
    ) {
        val content = ticketCommentContent(comment)
        performRtCorrespondenceRequest("/ticket/$ticketId/correspond", content)
    }

    private fun performRtRequest(
        path: String,
        content: Map<String, Any>,
    ): Map<*, *> {
        return client.post()
            .retrieveBlocking<Map<*, *>>(rtUrl(path), RequestParams(headers = requestHeaders(), body = content))
            ?: throw InvalidResponseException()
    }

    private fun performRtCorrespondenceRequest(
        path: String,
        content: Map<String, Any>,
    ) {
        val request = client.post().uri(rtUrl(path))
        request.headers { headers -> headers.addAll(requestHeaders()) }
        request.bodyValue(content)
            .retrieve()
            .toBodilessEntity()
            .block()
            ?: throw InvalidResponseException()
    }

    private fun rtUrl(path: String): String =
        UriComponentsBuilder
            .fromUriString("${rtConfig.host}/REST/2.0$path")
            .build()
            .toUriString()

    private fun requestHeaders() =
        httpHeadersOf(
            AUTHORIZATION to "token ${rtConfig.token}",
            CONTENT_TYPE to APPLICATION_JSON,
        )

    private fun getTicketId(response: Map<*, *>): String = response["id"] as? String ?: throw InvalidTicketIdException()

    private fun ticketCommentContent(comment: String): Map<String, Any> =
        mapOf(
            "Content" to comment,
            "ContentType" to TEXT_PLAIN,
            "Status" to RESOLVED,
        )

    private fun ticketContent(
        accNo: String,
        subject: String,
        owner: String,
        ccUser: String?,
        content: String,
    ): Map<String, Any> =
        buildMap {
            put("Queue", rtConfig.queue)
            put("Subject", subject)
            put("Status", RESOLVED)
            put("Requestor", owner)
            ccUser?.let { put("AdminCc", it) }
            put("CustomFields", mapOf("Accession" to accNo))
            put("Content", content)
            put("ContentType", TEXT_PLAIN)
        }

    private companion object {
        const val RESOLVED = "resolved"
        const val TEXT_PLAIN = "text/plain"
    }
}

package uk.ac.ebi.biostd.client.cli.services

import com.github.ajalt.clikt.core.PrintMessage
import org.apache.commons.lang3.exception.ExceptionUtils
import uk.ac.ebi.biostd.client.cli.dto.SecurityConfig
import uk.ac.ebi.biostd.client.exception.WebClientException
import uk.ac.ebi.biostd.client.integration.web.SecurityWebClient

internal inline fun <T> performRequest(request: () -> T) =
    runCatching { request() }.getOrElse { error ->
        if (error is WebClientException) throw error
        throw PrintMessage(ExceptionUtils.getMessage(error))
    }

internal fun bioWebClient(securityConfig: SecurityConfig) =
    SecurityWebClient
        .create(securityConfig.server)
        .getAuthenticatedClient(securityConfig.user, securityConfig.password, securityConfig.onBehalf)

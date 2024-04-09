package uk.ac.ebi.biostd.client.cli.services

import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient
import com.github.ajalt.clikt.core.PrintMessage
import org.apache.commons.lang3.exception.ExceptionUtils
import uk.ac.ebi.biostd.client.cli.dto.SecurityConfig

internal inline fun <T> performRequest(request: () -> T) =
    runCatching { request() }.getOrElse { throw PrintMessage(ExceptionUtils.getMessage(it)) }

internal fun bioWebClient(
    server: String,
    user: String,
    password: String,
    onBehalf: String? = null,
) = SecurityWebClient
    .create(server)
    .getAuthenticatedClient(user, password, onBehalf)

internal fun bioWebClient(securityConfig: SecurityConfig) =
    SecurityWebClient
        .create(securityConfig.server)
        .getAuthenticatedClient(securityConfig.user, securityConfig.password, securityConfig.onBehalf)

package ebi.ac.uk.security.service

import ac.uk.ebi.biostd.common.properties.SecurityProperties
import ebi.ac.uk.commons.http.ext.postForObject
import ebi.ac.uk.security.integration.exception.InvalidCaptchaException
import org.springframework.web.reactive.function.client.WebClient
import java.net.URI

internal val VERIFY_URL = URI.create("https://www.google.com/recaptcha/api/siteverify")

class CaptchaVerifier(
    private val client: WebClient,
    private val properties: SecurityProperties,
) {
    fun verifyCaptcha(userKey: String?) {
        if (userKey.isNullOrBlank()) throw InvalidCaptchaException()
        if (checkCaptcha(userKey).success.not()) throw InvalidCaptchaException()
    }

    private fun checkCaptcha(userKey: String): CaptchaCheckResponse {
        return client.postForObject("$VERIFY_URL?secret=${properties.captchaKey}&response=$userKey")
    }
}

internal data class CaptchaCheckResponse(
    val success: Boolean,
)

package ebi.ac.uk.security.service

import ebi.ac.uk.security.integration.SecurityProperties
import ebi.ac.uk.security.integration.exception.InvalidCaptchaException
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForObject
import java.net.URI

internal val VERIFY_URL = URI.create("https://www.google.com/recaptcha/api/siteverify")

class CaptchaVerifier(
    private val restTemplate: RestTemplate,
    private val properties: SecurityProperties
) {
    fun verifyCaptcha(userKey: String?) {
        if (userKey.isNullOrBlank()) throw InvalidCaptchaException()
        if (checkCaptcha(userKey).success.not()) throw InvalidCaptchaException()
    }

    private fun checkCaptcha(userKey: String): CaptchaCheckResponse =
        restTemplate.postForObject("$VERIFY_URL?secret=${properties.captchaKey}&response=$userKey")
}

internal data class CaptchaCheckResponse(
    val success: Boolean
)

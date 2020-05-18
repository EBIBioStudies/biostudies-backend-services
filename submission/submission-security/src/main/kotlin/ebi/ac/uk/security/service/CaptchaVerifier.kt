package ebi.ac.uk.security.service

import ebi.ac.uk.security.integration.SecurityProperties
import ebi.ac.uk.security.integration.exception.InvalidCaptchaException
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForObject
import java.net.URI

private val VERIFY_URL = URI.create("https://www.google.com/recaptcha/api/siteverify")

internal class CaptchaVerifier(
    private val restTemplate: RestTemplate,
    private val properties: SecurityProperties
) {
    fun verifyCaptcha(userKey: String?) {
        if (properties.checkCaptcha) {
            if (userKey.isNullOrBlank()) throw InvalidCaptchaException()
            if (checkCaptcha(userKey).success.not()) throw InvalidCaptchaException()
        }
    }

    private fun checkCaptcha(userKey: String): CaptchaVerificationResponse =
        restTemplate.postForObject(
            "$VERIFY_URL?secret=${properties.captchaKey}&response=$userKey",
            CaptchaVerificationResponse::class.java
        )!!
}

internal data class CaptchaVerificationRequest(
    val secret: String,
    val response: String
)

internal data class CaptchaVerificationResponse(
    val success: Boolean
)

package ebi.ac.uk.security.service

import ebi.ac.uk.security.integration.SecurityProperties
import ebi.ac.uk.security.integration.exception.InvalidCaptchaException
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForObject

private const val USER_CAPTCHA = "captcha"
private const val APP_KEY = "app-key"
private val EXPECTED_URL = "$VERIFY_URL?secret=$APP_KEY&response=$USER_CAPTCHA"

@ExtendWith(MockKExtension::class)
internal class CaptchaVerifierTest(
    @MockK val restTemplate: RestTemplate,
    @MockK val properties: SecurityProperties
) {

    private val testInstance = CaptchaVerifier(restTemplate, properties)

    @BeforeEach
    fun beforeEach() {
        every { properties.captchaKey } returns APP_KEY
    }

    @Test
    fun verifyCaptchaWhenNoCaptcha() {
        assertThrows<InvalidCaptchaException> { testInstance.verifyCaptcha(null) }
    }

    @Test
    fun verifyCaptchaWhenInvalid() {
        every { restTemplate.postForObject<CaptchaCheckResponse>(EXPECTED_URL) } returns CaptchaCheckResponse(false)

        assertThrows<InvalidCaptchaException> { testInstance.verifyCaptcha(USER_CAPTCHA) }
    }

    @Test
    fun verifyCaptchaWhenValid() {
        every { restTemplate.postForObject<CaptchaCheckResponse>(EXPECTED_URL) } returns CaptchaCheckResponse(true)

        testInstance.verifyCaptcha(USER_CAPTCHA)
    }
}

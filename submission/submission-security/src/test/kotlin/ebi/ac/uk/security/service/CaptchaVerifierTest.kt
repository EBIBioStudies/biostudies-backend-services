package ebi.ac.uk.security.service

import ac.uk.ebi.biostd.common.properties.SecurityProperties
import ebi.ac.uk.security.integration.exception.InvalidCaptchaException
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec

private const val USER_CAPTCHA = "captcha"
private const val APP_KEY = "app-key"
private val EXPECTED_URL = "$VERIFY_URL?secret=$APP_KEY&response=$USER_CAPTCHA"

@ExtendWith(MockKExtension::class)
internal class CaptchaVerifierTest(
    @MockK private val client: WebClient,
    @MockK private val requestSpec: RequestBodySpec,
    @MockK private val properties: SecurityProperties,
) {
    private val testInstance = CaptchaVerifier(client, properties)

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
        every { client.post().uri(EXPECTED_URL) } returns requestSpec
        every {
            requestSpec.retrieve().bodyToMono(CaptchaCheckResponse::class.java).block()
        } returns CaptchaCheckResponse(false)

        assertThrows<InvalidCaptchaException> { testInstance.verifyCaptcha(USER_CAPTCHA) }
    }

    @Test
    fun verifyCaptchaWhenValid() {
        every { client.post().uri(EXPECTED_URL) } returns requestSpec
        every {
            requestSpec.retrieve().bodyToMono(CaptchaCheckResponse::class.java).block()
        } returns CaptchaCheckResponse(true)

        testInstance.verifyCaptcha(USER_CAPTCHA)
    }
}

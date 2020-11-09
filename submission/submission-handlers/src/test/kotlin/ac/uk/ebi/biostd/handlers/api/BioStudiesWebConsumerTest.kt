package ac.uk.ebi.biostd.handlers.api

import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.ExtUser
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForObject
import uk.ac.ebi.extended.serialization.service.ExtSerializationService

@ExtendWith(MockKExtension::class)
class BioStudiesWebConsumerTest(
    @MockK private val restTemplate: RestTemplate,
    @MockK private val extSerializationService: ExtSerializationService
) {
    private val testInstance = BioStudiesWebConsumer(restTemplate, extSerializationService)

    @Test
    fun `get extended submission`(@MockK extSubmission: ExtSubmission) {
        val url = "http://biostudy:8788/submission/extended/S-TEST123"

        every { restTemplate.getForObject<String>(url) } returns "the-submission"
        every { extSerializationService.deserialize("the-submission", ExtSubmission::class.java) } returns extSubmission

        val submission = testInstance.getExtSubmission(url)

        assertThat(submission).isEqualTo(extSubmission)
        verify { restTemplate.getForObject<String>(url) }
        verify { extSerializationService.deserialize("the-submission", ExtSubmission::class.java) }
    }

    @Test
    fun `get extended user`(@MockK extUser: ExtUser) {
        val url = "http://biostudy:8788/security/user/extended/5"

        every { restTemplate.getForObject<ExtUser>(url) } returns extUser

        assertThat(testInstance.getExtUser(url)).isEqualTo(extUser)
    }
}

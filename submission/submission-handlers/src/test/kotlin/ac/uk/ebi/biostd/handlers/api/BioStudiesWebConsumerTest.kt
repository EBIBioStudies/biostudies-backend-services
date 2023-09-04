package ac.uk.ebi.biostd.handlers.api

import ebi.ac.uk.commons.http.ext.getForObject
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.ExtUser
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.web.reactive.function.client.WebClient
import uk.ac.ebi.extended.serialization.service.ExtSerializationService

@ExtendWith(MockKExtension::class)
class BioStudiesWebConsumerTest(
    @MockK private val client: WebClient,
    @MockK private val extSerializationService: ExtSerializationService,
) {
    private val testInstance = BioStudiesWebConsumer(client, extSerializationService)

    @Test
    fun `get extended submission`(
        @MockK extSubmission: ExtSubmission,
    ) {
        val url = "http://biostudy:8788/submission/extended/S-TEST123"

        every { client.getForObject<String>(url) } returns "the-submission"
        every { extSerializationService.deserialize("the-submission") } returns extSubmission

        val submission = testInstance.getExtSubmission(url)

        assertThat(submission).isEqualTo(extSubmission)
        verify {
            client.getForObject<String>(url)
            extSerializationService.deserialize("the-submission")
        }
    }

    @Test
    fun `get extended user`(
        @MockK extUser: ExtUser,
    ) {
        val url = "http://biostudy:8788/security/user/extended/5"

        every { client.getForObject<ExtUser>(url) } returns extUser

        assertThat(testInstance.getExtUser(url)).isEqualTo(extUser)
    }
}

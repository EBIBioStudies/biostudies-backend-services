package ac.uk.ebi.biostd.client.extensions

import ac.uk.ebi.biostd.client.api.EXT_SUBMISSIONS_URL
import ac.uk.ebi.biostd.client.dto.ExtPageQuery
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.client.integration.web.SubmissionClient
import ebi.ac.uk.extended.model.ExtPage
import ebi.ac.uk.extended.model.ExtSubmission
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.spyk
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class BioWebClientExtTest(@MockK private val submissionClient: SubmissionClient) {
    private val testInstance = spyk(BioWebClient(submissionClient))

    @Test
    fun `ext submissions as sequence`(
        @MockK firstExtSubmission: ExtSubmission,
        @MockK secondExtSubmission: ExtSubmission
    ) {
        val firstPageUrl = "$EXT_SUBMISSIONS_URL?offset=0&limit=1"
        val secondPageUrl = "$EXT_SUBMISSIONS_URL?offset=1&limit=1"

        val firstExtPage = ExtPage(listOf(firstExtSubmission), 1, secondPageUrl, null)
        val secondExtPage = ExtPage(listOf(secondExtSubmission), 1, null, firstPageUrl)
        val query = ExtPageQuery(limit = 1, offset = 1, fromRTime = null, toRTime = null)

        every { testInstance.getExtSubmissions(query) } returns firstExtPage
        every { testInstance.getExtSubmissionsPage(secondPageUrl) } returns secondExtPage

        var idx = 0
        testInstance.getExtSubmissionsAsSequence(query).forEach {
            when (idx) {
                0 -> assertThat(it).isEqualTo(firstExtSubmission)
                1 -> assertThat(it).isEqualTo(secondExtSubmission)
                else -> fail("The sequence should only have two elements")
            }
            idx++
        }
    }

    @Test
    fun `ext submissions as sequence with only one page`(@MockK extSubmission: ExtSubmission) {
        val extPage = ExtPage(listOf(extSubmission), 1, null, null)
        val query = ExtPageQuery(limit = 1, offset = 1, fromRTime = null, toRTime = null)

        every { testInstance.getExtSubmissions(query) } returns extPage

        var idx = 0
        testInstance.getExtSubmissionsAsSequence(query).forEach {
            when (idx) {
                0 -> assertThat(it).isEqualTo(extSubmission)
                else -> fail("The sequence should only have one element")
            }
            idx++
        }
    }

    @Test
    fun `access non existing element`() {
        val extPage = ExtPage(listOf(), 1, null, null)
        val query = ExtPageQuery(limit = 1, offset = 1, fromRTime = null, toRTime = null)

        every { testInstance.getExtSubmissions(query) } returns extPage

        assertThrows<NoSuchElementException> { testInstance.getExtSubmissionsAsSequence(query).first() }
    }
}

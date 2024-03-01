package ac.uk.ebi.biostd.submission.domain.extended

import ac.uk.ebi.biostd.persistence.common.request.SubmissionFilter
import ac.uk.ebi.biostd.persistence.common.service.SubmissionFilesPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ebi.ac.uk.extended.model.ExtCollection
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.test.basicExtSubmission
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable

@ExtendWith(MockKExtension::class)
internal class ExtSubmissionQueryServiceTest(
    @MockK private val filesRepository: SubmissionFilesPersistenceService,
    @MockK private val submissionQueryService: SubmissionPersistenceQueryService,
) {
    private val testInstance = ExtSubmissionQueryService(filesRepository, submissionQueryService)

    @Test
    fun `get ext submission`() = runTest {
        val extSubmission = basicExtSubmission.copy(collections = listOf(ExtCollection("ArrayExpress")))
        coEvery { submissionQueryService.getExtByAccNo("S-TEST123") } returns extSubmission

        val submission = testInstance.getExtendedSubmission("S-TEST123")
        assertThat(submission).isEqualTo(extSubmission)
    }

    @Test
    fun `get ext submission by version`() = runTest {
        val extSubmission = basicExtSubmission.copy(version = 2)
        coEvery { submissionQueryService.getExtByAccNoAndVersion("S-TEST123", 2) } returns extSubmission

        val submission = testInstance.getExtSubmissionByAccNoAndVersion("S-TEST123", 2)
        assertThat(submission).isEqualTo(extSubmission)
    }

    @Test
    fun `filtering extended submissions`(@MockK extSubmission: ExtSubmission) = runTest {
        val filter = slot<SubmissionFilter>()
        val request = ExtPageRequest(
            fromRTime = "2019-09-21T15:00:00Z",
            toRTime = "2020-09-21T15:00:00Z",
            released = true,
            offset = 1,
            limit = 2,
        )

        val pageable = Pageable.unpaged()
        val page = PageImpl(mutableListOf(extSubmission), pageable, 2L)

        coEvery { submissionQueryService.getExtendedSubmissions(capture(filter)) } returns page

        val result = testInstance.getExtendedSubmissions(request)
        assertThat(result.content).hasSize(1)
        assertThat(result.content.first()).isEqualTo(extSubmission)
        assertThat(result.pageable).isEqualTo(pageable)
        assertThat(result.totalElements).isEqualTo(2L)

        val submissionFilter = filter.captured
        assertThat(submissionFilter.released).isTrue
        assertThat(submissionFilter.rTimeTo).isEqualTo("2020-09-21T15:00:00Z")
        assertThat(submissionFilter.rTimeFrom).isEqualTo("2019-09-21T15:00:00Z")
        coVerify(exactly = 1) { submissionQueryService.getExtendedSubmissions(submissionFilter) }
    }
}

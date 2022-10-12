package ac.uk.ebi.biostd.submission.domain.service.ext

import ac.uk.ebi.biostd.persistence.common.request.SubmissionFilter
import ac.uk.ebi.biostd.persistence.common.service.SubmissionFilesPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ac.uk.ebi.biostd.submission.domain.service.ExtSubmissionQueryService
import ac.uk.ebi.biostd.submission.web.model.ExtPageRequest
import ebi.ac.uk.extended.model.ExtCollection
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.test.basicExtSubmission
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable

@ExtendWith(MockKExtension::class)
internal class ExtSubmissionPersistenceQueryServiceTest(
    @MockK private val requestService: SubmissionRequestPersistenceService,
    @MockK private val filesService: SubmissionFilesPersistenceService,
    @MockK private val submissionQueryService: SubmissionPersistenceQueryService,
) {
    private val testInstance = ExtSubmissionQueryService(requestService, filesService, submissionQueryService)

    @Test
    fun `get ext submission`() {
        val extSubmission = basicExtSubmission.copy(collections = listOf(ExtCollection("ArrayExpress")))
        every { submissionQueryService.getExtByAccNo("S-TEST123") } returns extSubmission

        val submission = testInstance.getExtendedSubmission("S-TEST123")
        assertThat(submission).isEqualTo(extSubmission)
    }

    @Test
    fun `filtering extended submissions`(@MockK extSubmission: ExtSubmission) {
        val filter = slot<SubmissionFilter>()
        val request = ExtPageRequest(
            fromRTime = "2019-09-21T15:00:00Z",
            toRTime = "2020-09-21T15:00:00Z",
            released = true,
            offset = 1,
            limit = 2
        )

        val pageable = Pageable.unpaged()
        val page = PageImpl(mutableListOf(extSubmission), pageable, 2L)

        every { submissionQueryService.getExtendedSubmissions(capture(filter)) } returns page

        val result = testInstance.getExtendedSubmissions(request)
        assertThat(result.content).hasSize(1)
        assertThat(result.content.first()).isEqualTo(extSubmission)
        assertThat(result.pageable).isEqualTo(pageable)
        assertThat(result.totalElements).isEqualTo(2L)

        val submissionFilter = filter.captured
        assertThat(submissionFilter.released).isTrue
        assertThat(submissionFilter.rTimeTo).isEqualTo("2020-09-21T15:00:00Z")
        assertThat(submissionFilter.rTimeFrom).isEqualTo("2019-09-21T15:00:00Z")
        verify(exactly = 1) { submissionQueryService.getExtendedSubmissions(submissionFilter) }
    }

    @Test
    fun `get referenced files`(
        @MockK extFile: ExtFile
    ) {
        every { filesService.getReferencedFiles("S-BSST1", "file-list") } returns listOf(extFile)

        assertThat(testInstance.getReferencedFiles("S-BSST1", "file-list").files).containsExactly(extFile)
    }
}

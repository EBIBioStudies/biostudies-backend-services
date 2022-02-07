package ac.uk.ebi.biostd.data.service

import ac.uk.ebi.biostd.persistence.common.request.PaginationFilter
import ac.uk.ebi.biostd.persistence.model.DbUserData
import ac.uk.ebi.biostd.submission.domain.service.SubmissionService
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Instant

private const val USER_ID = 1234L
private const val USER_EMAIL = "jhon.doe@ebi.ac.uk"
private const val DRAFT_KEY = "key"
private const val DRAFT_CONTENT = "data"

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockKExtension::class)
class SubmissionDraftSqlServiceTest(
    @MockK private val userDataService: UserDataService,
    @MockK private val submissionService: SubmissionService
) {
    val testInstance = SubmissionDraftSqlService(userDataService, submissionService)

    private val dbUserData = DbUserData(USER_ID, DRAFT_KEY, DRAFT_CONTENT)
    private val someFilter: PaginationFilter = PaginationFilter()

    @Test
    fun `get draft when submission exists`() {
        every { userDataService.getUserData(USER_EMAIL, DRAFT_KEY) } returns dbUserData

        val result = testInstance.getSubmissionDraft(USER_EMAIL, DRAFT_KEY)

        assertThat(result.key).isEqualTo(DRAFT_KEY)
        assertThat(result.content).isEqualTo(DRAFT_CONTENT)
    }

    @Test
    fun `get draft when submission does not exists`() {
        val draftJson = "{sub as JSON}"
        every { userDataService.getUserData(USER_EMAIL, DRAFT_KEY) } returns null
        every { submissionService.getSubmissionAsJson(DRAFT_KEY) } returns draftJson
        every { userDataService.saveUserData(USER_EMAIL, DRAFT_KEY, draftJson) } returns dbUserData

        val result = testInstance.getSubmissionDraft(USER_EMAIL, DRAFT_KEY)

        assertThat(result.key).isEqualTo(DRAFT_KEY)
        assertThat(result.content).isEqualTo(DRAFT_CONTENT)
    }

    @Test
    fun `update submission draft`() {
        every { userDataService.saveUserData(USER_EMAIL, DRAFT_KEY, DRAFT_CONTENT) } returns dbUserData

        testInstance.updateSubmissionDraft(USER_EMAIL, DRAFT_KEY, DRAFT_CONTENT)

        verify(exactly = 1) { userDataService.saveUserData(USER_EMAIL, DRAFT_KEY, DRAFT_CONTENT) }
    }

    @Test
    fun `delete submission draft`() {
        every { userDataService.delete(USER_EMAIL, DRAFT_KEY) } returns Unit

        testInstance.deleteSubmissionDraft(USER_EMAIL, DRAFT_KEY)

        verify(exactly = 1) { userDataService.delete(USER_EMAIL, DRAFT_KEY) }
    }

    @Test
    fun `get submissions in list`() {
        every { userDataService.findAll(USER_EMAIL, someFilter) } returns listOf(dbUserData)

        val result = testInstance.getActiveSubmissionsDraft(USER_EMAIL, someFilter)

        assertThat(result).hasSize(1)
        assertThat(result[0].key).isEqualTo(DRAFT_KEY)
        assertThat(result[0].content).isEqualTo(DRAFT_CONTENT)
    }

    @Test
    fun `create submission draft`() {
        mockkStatic(Instant::class)
        val draftCreationTime = 2L
        every { Instant.now().toEpochMilli() } returns draftCreationTime
        every { userDataService.saveUserData(USER_EMAIL, "TMP_$draftCreationTime", DRAFT_CONTENT) } returns dbUserData

        val result = testInstance.createSubmissionDraft(USER_EMAIL, DRAFT_CONTENT)

        assertThat(result.key).isEqualTo(DRAFT_KEY)
        assertThat(result.content).isEqualTo(DRAFT_CONTENT)
        unmockkStatic(Instant::class)
    }
}

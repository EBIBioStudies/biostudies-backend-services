package ac.uk.ebi.biostd.submission.domain.service

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat
import ac.uk.ebi.biostd.integration.SubFormat.JsonFormat.JsonPretty
import ac.uk.ebi.biostd.persistence.common.model.SubmissionDraft
import ac.uk.ebi.biostd.persistence.common.request.PageRequest
import ac.uk.ebi.biostd.persistence.common.service.SubmissionDraftPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.submission.exceptions.UserCanNotUpdateSubmit
import ac.uk.ebi.biostd.submission.model.AcceptedSubmission
import ac.uk.ebi.biostd.submission.web.handlers.SubmitBuilderRequest
import ac.uk.ebi.biostd.submission.web.handlers.SubmitRequestBuilder
import ac.uk.ebi.biostd.submission.web.handlers.SubmitWebHandler
import ac.uk.ebi.biostd.submission.web.model.ContentSubmitWebRequest
import ac.uk.ebi.biostd.submission.web.model.OnBehalfRequest
import ac.uk.ebi.biostd.submission.web.model.SubmissionRequestParameters
import ebi.ac.uk.extended.mapping.to.ToSubmissionMapper
import ebi.ac.uk.security.integration.components.IUserPrivilegesService
import ebi.ac.uk.security.integration.model.api.SecurityUser
import ebi.ac.uk.test.basicExtSubmission
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Instant

@ExtendWith(MockKExtension::class)
@OptIn(ExperimentalCoroutinesApi::class)
class SubmissionDraftServiceTest(
    @MockK private val submitWebHandler: SubmitWebHandler,
    @MockK private val toSubmissionMapper: ToSubmissionMapper,
    @MockK private val serializationService: SerializationService,
    @MockK private val submitRequestBuilder: SubmitRequestBuilder,
    @MockK private val userPrivilegesService: IUserPrivilegesService,
    @MockK private val submissionQueryService: SubmissionPersistenceQueryService,
    @MockK private val draftPersistenceService: SubmissionDraftPersistenceService,
) {
    private val testInstance =
        SubmissionDraftService(
            submitWebHandler,
            toSubmissionMapper,
            serializationService,
            submitRequestBuilder,
            userPrivilegesService,
            submissionQueryService,
            draftPersistenceService,
        )

    @AfterEach
    fun afterEach() = clearAllMocks()

    @Test
    fun `get active submission drafts`(
        @MockK pageRequest: PageRequest,
    ) = runTest {
        every { draftPersistenceService.getActiveSubmissionDrafts(USER_ID, pageRequest) } returns flowOf(testDraft)

        val result = testInstance.getActiveSubmissionDrafts(USER_ID, pageRequest).toList()

        assertThat(result).hasSize(1)
        assertThat(result.first()).isEqualTo(testDraft)
    }

    @Test
    fun `get draft when exists`() = runTest {
        coEvery { draftPersistenceService.findSubmissionDraft(USER_ID, DRAFT_KEY) } returns testDraft

        val result = testInstance.getOrCreateSubmissionDraft(USER_ID, DRAFT_KEY)

        assertThat(result.key).isEqualTo(DRAFT_KEY)
        assertThat(result.content).isEqualTo(DRAFT_CONTENT)
        coVerify(exactly = 0) {
            userPrivilegesService.canResubmit(USER_ID, DRAFT_KEY)
            submissionQueryService.getExtByAccNo(DRAFT_KEY)
            serializationService.serializeSubmission(any(), JsonPretty)
            draftPersistenceService.createSubmissionDraft(USER_ID, DRAFT_KEY, any())
        }
    }

    @Test
    fun `get draft from submission`() = runTest {
        val submission = basicExtSubmission
        coEvery { userPrivilegesService.canResubmit(USER_ID, DRAFT_KEY) } returns true
        coEvery { submissionQueryService.getExtByAccNo(DRAFT_KEY) } returns submission
        coEvery { draftPersistenceService.findSubmissionDraft(USER_ID, DRAFT_KEY) } returns null
        coEvery { draftPersistenceService.createSubmissionDraft(USER_ID, DRAFT_KEY, DRAFT_CONTENT) } returns testDraft
        coEvery {
            serializationService.serializeSubmission(toSubmissionMapper.toSimpleSubmission(submission), JsonPretty)
        } returns DRAFT_CONTENT

        val result = testInstance.getOrCreateSubmissionDraft(USER_ID, DRAFT_KEY)

        assertThat(result.key).isEqualTo(DRAFT_KEY)
        assertThat(result.content).isEqualTo(DRAFT_CONTENT)
        coVerify(exactly = 1) {
            userPrivilegesService.canResubmit(USER_ID, DRAFT_KEY)
            submissionQueryService.getExtByAccNo(DRAFT_KEY)
            draftPersistenceService.createSubmissionDraft(USER_ID, DRAFT_KEY, DRAFT_CONTENT)
        }
    }

    @Test
    fun `get draft from submission without permissions`() = runTest {
        coEvery { userPrivilegesService.canResubmit(USER_ID, DRAFT_KEY) } returns false
        coEvery { draftPersistenceService.findSubmissionDraft(USER_ID, DRAFT_KEY) } returns null

        val error = assertThrows<UserCanNotUpdateSubmit> { testInstance.getOrCreateSubmissionDraft(USER_ID, DRAFT_KEY) }
        assertThat(error.message).isEqualTo("The user {$USER_ID} is not allowed to update the submission $DRAFT_KEY")

        coVerify(exactly = 1) {
            userPrivilegesService.canResubmit(USER_ID, DRAFT_KEY)
        }
        coVerify(exactly = 0) {
            submissionQueryService.getExtByAccNo(DRAFT_KEY)
            serializationService.serializeSubmission(any(), JsonPretty)
            draftPersistenceService.createSubmissionDraft(USER_ID, DRAFT_KEY, any())
        }
    }

    @Test
    fun `delete submission draft`() = runTest {
        coEvery { draftPersistenceService.deleteSubmissionDraft(USER_ID, DRAFT_KEY) } answers { nothing }

        testInstance.deleteSubmissionDraft(USER_ID, DRAFT_KEY)

        coVerify(exactly = 1) { draftPersistenceService.deleteSubmissionDraft(USER_ID, DRAFT_KEY) }
    }

    @Test
    fun `update submission draft`() = runTest {
        coEvery { draftPersistenceService.updateSubmissionDraft(USER_ID, DRAFT_KEY, DRAFT_CONTENT) } returns testDraft

        testInstance.updateSubmissionDraft(USER_ID, DRAFT_KEY, DRAFT_CONTENT)

        coVerify(exactly = 1) { draftPersistenceService.updateSubmissionDraft(USER_ID, DRAFT_KEY, DRAFT_CONTENT) }
    }

    @Test
    fun `create submission draft`() = runTest {
        mockkStatic(Instant::class)
        val draftCreationTime = 2L

        every { Instant.now().toEpochMilli() } returns draftCreationTime
        coEvery { draftPersistenceService.createSubmissionDraft(USER_ID, "TMP_2", DRAFT_CONTENT) } returns testDraft

        testInstance.createSubmissionDraft(USER_ID, DRAFT_CONTENT)

        coVerify(exactly = 1) { draftPersistenceService.createSubmissionDraft(USER_ID, "TMP_2", DRAFT_CONTENT) }
        unmockkStatic(Instant::class)
    }

    @Test
    fun `submit draft`(
        @MockK user: SecurityUser,
        @MockK onBehalfRequest: OnBehalfRequest,
        @MockK parameters: SubmissionRequestParameters,
        @MockK contentRequest: ContentSubmitWebRequest,
    ) = runTest {
        val requestSlot = slot<SubmitBuilderRequest>()

        every { user.email } returns USER_ID
        coEvery { submitWebHandler.submitAsync(contentRequest) } returns AcceptedSubmission(ACC_NO, VERSION)
        coEvery { draftPersistenceService.findSubmissionDraft(USER_ID, DRAFT_KEY) } returns testDraft
        every {
            submitRequestBuilder.buildContentRequest(DRAFT_CONTENT, SubFormat.JSON_PRETTY, capture(requestSlot))
        } returns contentRequest

        testInstance.submitDraftAsync(DRAFT_KEY, user, onBehalfRequest, parameters)

        val submitRequest = requestSlot.captured
        assertThat(submitRequest.user).isEqualTo(user)
        assertThat(submitRequest.draftKey).isEqualTo(DRAFT_KEY)
        assertThat(submitRequest.onBehalfRequest).isEqualTo(onBehalfRequest)
        assertThat(submitRequest.submissionRequestParameters).isEqualTo(parameters)

        coVerify(exactly = 1) {
            submitWebHandler.submitAsync(contentRequest)
            draftPersistenceService.findSubmissionDraft(USER_ID, DRAFT_KEY)
            submitRequestBuilder.buildContentRequest(DRAFT_CONTENT, SubFormat.JSON_PRETTY, submitRequest)
        }
    }

    companion object {
        private const val ACC_NO = "S-BSST1"
        private const val USER_ID = "jhon.doe@ebi.ac.uk"
        private const val DRAFT_KEY = "key"
        private const val DRAFT_CONTENT = "content"
        private const val VERSION = 1
        private val testDraft = SubmissionDraft(DRAFT_KEY, DRAFT_CONTENT)
    }
}

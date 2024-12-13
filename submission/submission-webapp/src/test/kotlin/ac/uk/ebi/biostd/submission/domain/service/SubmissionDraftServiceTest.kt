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
import ac.uk.ebi.biostd.submission.model.ContentSubmitWebRequest
import ac.uk.ebi.biostd.submission.web.handlers.SubmitBuilderRequest
import ac.uk.ebi.biostd.submission.web.handlers.SubmitRequestBuilder
import ac.uk.ebi.biostd.submission.web.handlers.SubmitWebHandler
import ebi.ac.uk.api.OnBehalfParameters
import ebi.ac.uk.api.SubmitParameters
import ebi.ac.uk.asserts.assertThrows
import ebi.ac.uk.extended.mapping.to.ToSubmissionMapper
import ebi.ac.uk.security.integration.components.IUserPrivilegesService
import ebi.ac.uk.security.integration.model.api.SecurityUser
import ebi.ac.uk.test.basicExtSubmission
import io.mockk.Called
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.unmockkStatic
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Clock
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset.UTC

@Disabled
@ExtendWith(MockKExtension::class)
class SubmissionDraftServiceTest(
    @MockK private val submitWebHandler: SubmitWebHandler,
    @MockK private val toSubmissionMapper: ToSubmissionMapper,
    @MockK private val serializationService: SerializationService,
    @MockK private val submitRequestBuilder: SubmitRequestBuilder,
    @MockK private val userPrivilegesService: IUserPrivilegesService,
    @MockK private val submissionQueryService: SubmissionPersistenceQueryService,
    @MockK private val draftPersistenceService: SubmissionDraftPersistenceService,
) {
    private val now = Instant.ofEpochMilli(2)
    private val clock = Clock.fixed(now, ZoneId.systemDefault())

    private val testInstance =
        SubmissionDraftService(
            clock,
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
    fun `get draft when exists`() =
        runTest {
            coEvery { draftPersistenceService.findSubmissionDraft(USER_ID, KEY) } returns testDraft

            val result = testInstance.getOrCreateSubmissionDraft(USER_ID, KEY)

            assertThat(result.key).isEqualTo(KEY)
            assertThat(result.content).isEqualTo(CONTENT)
            coVerify {
                serializationService wasNot Called
                userPrivilegesService wasNot Called
                submissionQueryService wasNot Called
            }
            coVerify(exactly = 0) {
                draftPersistenceService.createSubmissionDraft(any(), any(), any(), any())
            }
        }

    @Test
    fun `get draft from submission`() =
        runTest {
            val submission = basicExtSubmission
            coEvery { userPrivilegesService.canResubmit(USER_ID, KEY) } returns true
            coEvery { submissionQueryService.getExtByAccNo(KEY) } returns submission
            coEvery { draftPersistenceService.findSubmissionDraft(USER_ID, KEY) } returns null
            coEvery {
                draftPersistenceService.createSubmissionDraft(
                    USER_ID,
                    KEY,
                    CONTENT,
                    now,
                )
            } returns testDraft
            coEvery {
                serializationService.serializeSubmission(toSubmissionMapper.toSimpleSubmission(submission), JsonPretty)
            } returns CONTENT

            val result = testInstance.getOrCreateSubmissionDraft(USER_ID, KEY)

            assertThat(result.key).isEqualTo(KEY)
            assertThat(result.content).isEqualTo(CONTENT)
            coVerify(exactly = 1) {
                userPrivilegesService.canResubmit(USER_ID, KEY)
                submissionQueryService.getExtByAccNo(KEY)
                draftPersistenceService.createSubmissionDraft(USER_ID, KEY, CONTENT, now)
            }
        }

    @Test
    fun `get draft from submission without permissions`() =
        runTest {
            coEvery { userPrivilegesService.canResubmit(USER_ID, KEY) } returns false
            coEvery { draftPersistenceService.findSubmissionDraft(USER_ID, KEY) } returns null

            val error =
                assertThrows<UserCanNotUpdateSubmit> { testInstance.getOrCreateSubmissionDraft(USER_ID, KEY) }
            assertThat(error.message).isEqualTo("The user $USER_ID is not allowed to update the submission $KEY")

            coVerify(exactly = 1) {
                userPrivilegesService.canResubmit(USER_ID, KEY)
            }
            coVerify {
                serializationService wasNot Called
                submissionQueryService wasNot Called
            }
            coVerify(exactly = 0) {
                draftPersistenceService.createSubmissionDraft(any(), any(), any(), any())
            }
        }

    @Test
    fun `delete submission draft`() =
        runTest {
            coEvery { draftPersistenceService.deleteSubmissionDraft(USER_ID, KEY) } answers { nothing }

            testInstance.deleteSubmissionDraft(USER_ID, KEY)

            coVerify(exactly = 1) { draftPersistenceService.deleteSubmissionDraft(USER_ID, KEY) }
        }

    @Test
    fun `update submission draft`() =
        runTest {
            coEvery {
                draftPersistenceService.updateSubmissionDraft(
                    USER_ID,
                    KEY,
                    CONTENT,
                    now,
                )
            } returns testDraft

            testInstance.updateSubmissionDraft(USER_ID, KEY, CONTENT)

            coVerify(exactly = 1) { draftPersistenceService.updateSubmissionDraft(USER_ID, KEY, CONTENT, now) }
        }

    @Test
    fun `create submission draft`() =
        runTest {
            coEvery { draftPersistenceService.createSubmissionDraft(USER_ID, KEY, CONTENT, now) } returns testDraft

            testInstance.createSubmissionDraft(USER_ID, CONTENT)

            coVerify(exactly = 1) { draftPersistenceService.createSubmissionDraft(USER_ID, KEY, CONTENT, now) }
            unmockkStatic(Instant::class)
        }

    @Test
    fun `submit draft`(
        @MockK user: SecurityUser,
        @MockK onBehalfRequest: OnBehalfParameters,
        @MockK parameters: SubmitParameters,
        @MockK contentRequest: ContentSubmitWebRequest,
    ) = runTest {
        val requestSlot = slot<SubmitBuilderRequest>()

        every { user.email } returns USER_ID
        coEvery { submitWebHandler.submitAsync(contentRequest) } returns AcceptedSubmission(ACC_NO, VERSION)
        coEvery { draftPersistenceService.findSubmissionDraft(USER_ID, KEY) } returns testDraft
        every {
            submitRequestBuilder.buildContentRequest(CONTENT, SubFormat.JSON_PRETTY, capture(requestSlot))
        } returns contentRequest

        testInstance.submitDraftAsync(KEY, user, onBehalfRequest, parameters)

        val submitRequest = requestSlot.captured
        assertThat(submitRequest.user).isEqualTo(user)
        assertThat(submitRequest.draftKey).isEqualTo(KEY)
        assertThat(submitRequest.onBehalfRequest).isEqualTo(onBehalfRequest)
        assertThat(submitRequest.submissionRequestParameters).isEqualTo(parameters)

        coVerify(exactly = 1) {
            submitWebHandler.submitAsync(contentRequest)
            draftPersistenceService.findSubmissionDraft(USER_ID, KEY)
            submitRequestBuilder.buildContentRequest(CONTENT, SubFormat.JSON_PRETTY, submitRequest)
        }
    }

    companion object {
        private const val ACC_NO = "S-BSST1"
        private const val USER_ID = "jhon.doe@ebi.ac.uk"
        private const val KEY = "TMP_1970-01-01T00:00:00.002Z"
        private const val CONTENT = "content"
        private const val VERSION = 1
        private val MODIFICATION_TIME = OffsetDateTime.of(1970, 1, 1, 0, 0, 0, 2, UTC)
        private val testDraft = SubmissionDraft(KEY, CONTENT, MODIFICATION_TIME)
    }
}

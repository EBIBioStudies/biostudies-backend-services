package ac.uk.ebi.biostd.submission.submitter

import ac.uk.ebi.biostd.persistence.integration.PersistenceContext
import ac.uk.ebi.biostd.persistence.integration.SubmissionQueryService
import ac.uk.ebi.biostd.submission.events.SubmissionEvents.successfulSubmission
import ac.uk.ebi.biostd.submission.events.SuccessfulSubmission
import ac.uk.ebi.biostd.submission.model.SubmissionRequest
import ac.uk.ebi.biostd.submission.service.AccNoService
import ac.uk.ebi.biostd.submission.service.ParentInfoService
import ac.uk.ebi.biostd.submission.service.ProjectInfoService
import ac.uk.ebi.biostd.submission.service.TimesService
import ebi.ac.uk.extended.mapping.serialization.to.toSimpleSubmission
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.SubmissionMethod.PAGE_TAB
import ebi.ac.uk.security.integration.model.api.SecurityUser
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SubmissionSubmitterTest {
    private val timesService = mockk<TimesService>()
    private val accNoService = mockk<AccNoService>()
    private val parentInfoService = mockk<ParentInfoService>()
    private val queryService = mockk<SubmissionQueryService>()
    private val persistenceContext = mockk<PersistenceContext>()
    private val projectInfoService = mockk<ProjectInfoService>()

    private val sources = mockk<FilesSource>()
    private val submission = mockk<Submission>()
    private val extSubmission = mockk<ExtSubmission>()

    private val testInstance = spyk(SubmissionSubmitter(
        timesService, accNoService, parentInfoService, projectInfoService, persistenceContext, queryService))

    @BeforeEach
    fun beforeEach() {
        mockkObject(successfulSubmission)
        mockkStatic("ebi.ac.uk.extended.mapping.serialization.to.ToSubmissionKt")

        every { successfulSubmission.onNext(any()) } answers { nothing }
        every { any<ExtSubmission>().toSimpleSubmission() } returns submission
        every { persistenceContext.saveSubmission(extSubmission, "admin_user@ebi.ac.uk", 3) } returns extSubmission
    }

    @AfterEach
    fun afterEach() = clearAllMocks()

    @Test
    fun submit() {
        val testSecurityUser = testUser(notificationsEnabled = true)
        val testUser = testSecurityUser.asUser()

        every { testInstance.process(submission, testUser, sources, PAGE_TAB) } returns extSubmission
        testInstance.submit(SubmissionRequest(submission, testSecurityUser, sources, PAGE_TAB))

        verify(exactly = 1) {
            testInstance.process(submission, testUser, sources, PAGE_TAB)
            persistenceContext.saveSubmission(extSubmission, "admin_user@ebi.ac.uk", 3)
            successfulSubmission.onNext(SuccessfulSubmission(testUser, extSubmission))
        }
    }

    @Test
    fun `submit with notifications disabled`() {
        val testSecurityUser = testUser(notificationsEnabled = false)
        val testUser = testSecurityUser.asUser()

        every { testInstance.process(submission, testUser, sources, PAGE_TAB) } returns extSubmission
        testInstance.submit(SubmissionRequest(submission, testSecurityUser, sources, PAGE_TAB))

        verify(exactly = 0) { successfulSubmission.onNext(SuccessfulSubmission(testUser, extSubmission)) }
        verify(exactly = 1) {
            testInstance.process(submission, testUser, sources, PAGE_TAB)
            persistenceContext.saveSubmission(extSubmission, "admin_user@ebi.ac.uk", 3)
        }
    }

    private fun testUser(notificationsEnabled: Boolean) = SecurityUser(
        id = 3,
        email = "admin_user@ebi.ac.uk",
        fullName = "admin_user",
        login = null,
        secret = "69214a2f-f80b-4f33-86b7-26d3bd0453aa",
        superuser = true,
        magicFolder = mockk(),
        groupsFolders = listOf(mockk()),
        permissions = emptySet(),
        notificationsEnabled = notificationsEnabled)
}

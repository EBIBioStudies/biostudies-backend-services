package ac.uk.ebi.biostd.submission.submitter

import ac.uk.ebi.biostd.persistence.integration.FileMode.COPY
import ac.uk.ebi.biostd.persistence.integration.PersistenceContext
import ac.uk.ebi.biostd.persistence.integration.SaveRequest
import ac.uk.ebi.biostd.persistence.integration.SubmissionQueryService
import ac.uk.ebi.biostd.submission.events.SubmissionEvents.successfulSubmission
import ac.uk.ebi.biostd.submission.events.SuccessfulSubmission
import ac.uk.ebi.biostd.submission.model.SubmissionRequest
import ac.uk.ebi.biostd.submission.service.AccNoService
import ac.uk.ebi.biostd.submission.service.AccNoServiceRequest
import ac.uk.ebi.biostd.submission.service.ParentInfo
import ac.uk.ebi.biostd.submission.service.ParentInfoService
import ac.uk.ebi.biostd.submission.service.ProjectInfoService
import ac.uk.ebi.biostd.submission.service.ProjectRequest
import ac.uk.ebi.biostd.submission.service.ProjectResponse
import ac.uk.ebi.biostd.submission.service.Times
import ac.uk.ebi.biostd.submission.service.TimesRequest
import ac.uk.ebi.biostd.submission.service.TimesService
import ebi.ac.uk.dsl.section
import ebi.ac.uk.dsl.submission
import ebi.ac.uk.extended.mapping.serialization.to.toSimpleSubmission
import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtProcessingStatus
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.ExtSubmissionMethod
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.model.AccNumber
import ebi.ac.uk.model.SubmissionMethod.PAGE_TAB
import ebi.ac.uk.model.extensions.attachTo
import ebi.ac.uk.model.extensions.releaseDate
import ebi.ac.uk.model.extensions.title
import ebi.ac.uk.security.integration.model.api.SecurityUser
import ebi.ac.uk.util.collections.second
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime
import java.time.ZoneOffset

class SubmissionSubmitterTest {
    private val accNo = AccNumber("S-TEST", 123)
    private val testTime = OffsetDateTime.of(2017, 10, 10, 0, 0, 0, 0, ZoneOffset.UTC)
    private val submission = submission("S-TEST123") {
        title = "Test Submission"
        attachTo = "BioImages"
        releaseDate = "2018-09-21"
        section("Study") { }
    }

    private val sources = mockk<FilesSource>()
    private val timesService = mockk<TimesService>()
    private val accNoService = mockk<AccNoService>()
    private val parentInfoService = mockk<ParentInfoService>()
    private val queryService = mockk<SubmissionQueryService>()
    private val projectInfoService = mockk<ProjectInfoService>()
    private val persistenceContext = mockk<PersistenceContext>()

    private val timesRequest = slot<TimesRequest>()
    private val extSubmission = slot<ExtSubmission>()
    private val saveRequest = slot<SaveRequest>()
    private val projectRequest = slot<ProjectRequest>()
    private val notification = slot<SuccessfulSubmission>()
    private val accNoServiceRequest = slot<AccNoServiceRequest>()

    private val testInstance = SubmissionSubmitter(
        timesService, accNoService, parentInfoService, projectInfoService, persistenceContext, queryService)

    @BeforeEach
    fun beforeEach() {
        mockServices()
        mockNotificationEvents()
        mockPersistenceContext()
        mockkStatic("ebi.ac.uk.extended.mapping.serialization.to.ToSubmissionKt")
        every { any<ExtSubmission>().toSimpleSubmission() } returns submission
    }

    @AfterEach
    fun afterEach() = clearAllMocks()

    @Test
    fun submit() {
        testInstance.submit(
            SubmissionRequest(submission, testUser(notificationsEnabled = true), sources, PAGE_TAB, COPY))

        assertCapturedValues()
        assertExtendedSubmission()

        verifyProcessServices()
        verify(exactly = 1) { successfulSubmission.onNext(notification.captured) }
    }

    @Test
    fun `submit with notifications disabled`() {
        testInstance.submit(
            SubmissionRequest(submission, testUser(notificationsEnabled = false), sources, PAGE_TAB, COPY))

        assertCapturedValues()
        assertExtendedSubmission()

        verifyProcessServices()
        verify(exactly = 0) { successfulSubmission.onNext(notification.captured) }
    }

    private fun assertCapturedValues() {
        val capturedTimesRequest = timesRequest.captured
        assertThat(capturedTimesRequest.parentReleaseTime).isNull()
        assertThat(capturedTimesRequest.accNo).isEqualTo("S-TEST123")
        assertThat(capturedTimesRequest.releaseDate).isEqualTo("2018-09-21")

        val capturedProjectRequest = projectRequest.captured
        assertThat(capturedProjectRequest.accNoTemplate).isNull()
        assertThat(capturedProjectRequest.subType).isEqualTo("Study")
        assertThat(capturedProjectRequest.accNo).isEqualTo("S-TEST123")
        assertThat(capturedProjectRequest.submitter).isEqualTo("admin_user@ebi.ac.uk")

        val capturedAccNoServiceRequest = accNoServiceRequest.captured
        assertThat(capturedAccNoServiceRequest.accNo).isEqualTo("S-TEST123")
        assertThat(capturedAccNoServiceRequest.project).isEqualTo("BioImages")
        assertThat(capturedAccNoServiceRequest.projectPattern).isEqualTo("S-BIAD")
        assertThat(capturedAccNoServiceRequest.submitter).isEqualTo("admin_user@ebi.ac.uk")
    }

    private fun assertExtendedSubmission() {
        val expected = saveRequest.captured.submission
        assertThat(expected.accNo).isEqualTo("S-TEST123")
        assertThat(expected.version).isEqualTo(2)
        assertThat(expected.method).isEqualTo(ExtSubmissionMethod.PAGE_TAB)
        assertThat(expected.title).isEqualTo("Test Submission")
        assertThat(expected.relPath).isEqualTo("/a/rel/path")
        assertThat(expected.rootPath).isNull()
        assertThat(expected.secretKey).isEqualTo("a-secret-key")
        assertThat(expected.status).isEqualTo(ExtProcessingStatus.PROCESSED)
        assertThat(expected.releaseTime).isNull()
        assertThat(expected.modificationTime).isEqualTo(testTime)
        assertThat(expected.creationTime).isEqualTo(testTime)
        assertThat(expected.tags).isEmpty()
        assertThat(expected.accessTags).hasSize(1)
        assertThat(expected.accessTags.first().name).isEqualTo("BioImages")
        assertThat(expected.section.type).isEqualTo("Study")
        assertThat(expected.attributes).hasSize(2)
        assertExtAttribute(expected.attributes.first(), "Title", "Test Submission")
        assertExtAttribute(expected.attributes.second(), "AttachTo", "BioImages")
    }

    private fun assertExtAttribute(extAttr: ExtAttribute, name: String, value: String) {
        assertThat(extAttr.name).isEqualTo(name)
        assertThat(extAttr.value).isEqualTo(value)
    }

    private fun verifyProcessServices() = verify(exactly = 1) {
        accNoService.getRelPath(accNo)
        accNoService.getAccNo(accNoServiceRequest.captured)

        queryService.isNew("S-TEST123")
        queryService.getSecret("S-TEST123")

        parentInfoService.getParentInfo("BioImages")

        timesService.getTimes(timesRequest.captured)

        projectInfoService.process(projectRequest.captured)

        persistenceContext.getNextVersion("S-TEST123")
        persistenceContext.saveSubmission(saveRequest.captured)
    }

    private fun mockNotificationEvents() {
        mockkObject(successfulSubmission)
        every { successfulSubmission.onNext(capture(notification)) } answers { nothing }
    }

    private fun mockServices() {
        every { accNoService.getRelPath(accNo) } returns "/a/rel/path"
        every { accNoService.getAccNo(capture(accNoServiceRequest)) } returns accNo
        every { queryService.isNew("S-TEST123") } returns false
        every { queryService.getSecret("S-TEST123") } returns "a-secret-key"
        every { timesService.getTimes(capture(timesRequest)) } returns Times(testTime, testTime, null)
        every { projectInfoService.process(capture(projectRequest)) } returns ProjectResponse("BioImages")
        every { parentInfoService.getParentInfo("BioImages") } returns ParentInfo(emptyList(), null, "S-BIAD")
    }

    private fun mockPersistenceContext() {
        every { persistenceContext.getNextVersion("S-TEST123") } returns 2
        every { persistenceContext.saveSubmission(capture(saveRequest)) } returns mockk()
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

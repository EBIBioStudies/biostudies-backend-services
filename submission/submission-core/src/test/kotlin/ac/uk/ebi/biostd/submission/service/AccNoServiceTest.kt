package ac.uk.ebi.biostd.submission.service

import ac.uk.ebi.biostd.persistence.common.service.PersistenceService
import ac.uk.ebi.biostd.submission.exceptions.UserCanNotProvideAccessNumber
import ac.uk.ebi.biostd.submission.exceptions.UserCanNotSubmitToCollectionException
import ac.uk.ebi.biostd.submission.exceptions.UserCanNotUpdateSubmit
import ac.uk.ebi.biostd.submission.model.SubmitRequest
import ac.uk.ebi.biostd.submission.util.AccNoPatternUtil
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.model.AccNumber
import ebi.ac.uk.security.integration.components.IUserPrivilegesService
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

private const val ACC_NO = "AAB12"
private val ACC_NUM = AccNumber("AAB", "12")
private const val SUBMITTER = "submiter@email.com"
private const val COLLECTION = "CC123"
private const val PROJECT_PATTERN = "!{ABC-}"

@ExtendWith(MockKExtension::class)
class AccNoServiceTest(
    @MockK private val request: SubmitRequest,
    @MockK private val service: PersistenceService,
    @MockK private val privilegesService: IUserPrivilegesService,
) {
    private val accNoPatternUtil: AccNoPatternUtil = AccNoPatternUtil()
    private val testInstance = AccNoService(service, accNoPatternUtil, privilegesService, subBasePath = null)

    @AfterEach
    fun afterEach() = clearAllMocks()

    @BeforeEach
    fun beforeEach() {
        initRequest()
    }

    @ParameterizedTest(name = "When accNo={0}, basePath={1}, expected path should be {2}")
    @CsvSource(
        "S-DIXA-AN-002, null, S-DIXA-AN-/002/S-DIXA-AN-002",
        "S-BSST11, null, S-BSST/011/S-BSST11",
        "S-DIXA-011, null, S-DIXA-/011/S-DIXA-011",
        "1-AAA, null, 1-AAA/000/1-AAA",
        "S-SCDT-EMBOJ-2019-103549, null, S-SCDT-EMBOJ-2019-/549/S-SCDT-EMBOJ-2019-103549",
        "S-SCDT-EMBOR-2017-44445V1, null, S-SCDT-EMBOR-2017-44445V/001/S-SCDT-EMBOR-2017-44445V1",
        "S-123, base-path/, base-path/S-/123/S-123",
        "S-123, /base-path, base-path/S-/123/S-123",
        nullValues = ["null"]
    )
    fun getRelPath(value: String, subBasePath: String?, expected: String) {
        val testInstance = AccNoService(service, accNoPatternUtil, privilegesService, subBasePath)
        assertThat(testInstance.getRelPath(accNoPatternUtil.toAccNumber(value))).isEqualTo(expected)
    }

    @Nested
    inner class WhenIsNew {
        @Test
        fun `when user cannot provide accession`() = runTest {
            every { request.collection } returns null
            every { privilegesService.canProvideAccNo(SUBMITTER) } returns false

            val error = assertThrows<UserCanNotProvideAccessNumber> { testInstance.calculateAccNo(request) }
            assertThat(error.message)
                .isEqualTo("The user {submiter@email.com} is not allowed to provide accession number directly")
        }

        @Test
        fun `when user cannot submit to collection`() = runTest {
            every { privilegesService.canSubmitToCollection(SUBMITTER, COLLECTION) } returns false

            val error = assertThrows<UserCanNotSubmitToCollectionException> { testInstance.calculateAccNo(request) }
            assertThat(error.message)
                .isEqualTo("The user submiter@email.com is not allowed to submit to CC123 collection")
        }

        @Nested
        inner class WhenAccNo {
            @Test
            fun `when no project`() = runTest {
                every { request.collection } returns null
                every { privilegesService.canProvideAccNo(SUBMITTER) } returns true

                assertThat(testInstance.calculateAccNo(request)).isEqualTo(ACC_NUM)
            }

            @Test
            fun `when project`() = runTest {
                every { privilegesService.canProvideAccNo(SUBMITTER) } returns true
                every { privilegesService.canSubmitToCollection(SUBMITTER, COLLECTION) } returns true

                assertThat(testInstance.calculateAccNo(request)).isEqualTo(ACC_NUM)
            }
        }

        @Nested
        inner class WhenNoAccNo {
            @Test
            fun `when parent`() = runTest {
                every { request.submission.accNo } returns ""
                every { service.getSequenceNextValue("ABC-") } returns 10
                every { privilegesService.canSubmitToCollection(SUBMITTER, COLLECTION) } returns true

                assertThat(testInstance.calculateAccNo(request)).isEqualTo(AccNumber("ABC-", "10"))
            }

            @Test
            fun `when no parent`() = runTest {
                every { request.collection } returns null
                every { request.submission.accNo } returns ""
                every { service.getSequenceNextValue("S-BSST") } returns 99
                every { privilegesService.canProvideAccNo(SUBMITTER) } returns true
                every { privilegesService.canSubmitToCollection(SUBMITTER, COLLECTION) } returns true

                assertThat(testInstance.calculateAccNo(request)).isEqualTo(AccNumber("S-BSST", "99"))
            }
        }
    }

    @Nested
    inner class WhenIsNotNew(
        @MockK private val previousVersion: ExtSubmission,
    ) {
        @BeforeEach
        fun beforeEach() {
            every { request.previousVersion } returns previousVersion
        }

        @Test
        fun `when user cannot resubmit`() = runTest {
            coEvery { privilegesService.canResubmit(SUBMITTER, ACC_NO) } returns false

            val error = assertThrows<UserCanNotUpdateSubmit> { testInstance.calculateAccNo(request) }
            assertThat(error.message).isEqualTo("The user {$SUBMITTER} is not allowed to update the submission $ACC_NO")
        }

        @Test
        fun `superuser resubmit`() = runTest {
            every { privilegesService.canProvideAccNo(SUBMITTER) } returns true
            coEvery { privilegesService.canResubmit(SUBMITTER, ACC_NO) } returns true
            every { privilegesService.canSubmitToCollection(SUBMITTER, COLLECTION) } returns true

            assertThat(testInstance.calculateAccNo(request)).isEqualTo(AccNumber("AAB", "12"))
        }

        @Test
        fun `owner regular user resubmit`() = runTest {
            every { privilegesService.canProvideAccNo(SUBMITTER) } returns false
            coEvery { privilegesService.canResubmit(SUBMITTER, ACC_NO) } returns true
            every { privilegesService.canSubmitToCollection(SUBMITTER, COLLECTION) } returns false

            assertThat(testInstance.calculateAccNo(request)).isEqualTo(AccNumber("AAB", "12"))
        }

        @Test
        fun `non owner regular user resubmit`() = runTest {
            every { privilegesService.canProvideAccNo(SUBMITTER) } returns false
            coEvery { privilegesService.canResubmit(SUBMITTER, ACC_NO) } returns false
            every { privilegesService.canSubmitToCollection(SUBMITTER, COLLECTION) } returns true

            val error = assertThrows<UserCanNotUpdateSubmit> { testInstance.calculateAccNo(request) }

            assertThat(error.message).isEqualTo(
                "The user {submiter@email.com} is not allowed to update the submission AAB12"
            )
        }
    }

    private fun initRequest() {
        every { request.previousVersion } returns null
        every { request.submission.accNo } returns ACC_NO
        every { request.collection?.accNo } returns COLLECTION
        every { request.submitter.email } returns SUBMITTER
        every { request.collection?.accNoPattern } returns PROJECT_PATTERN
    }
}

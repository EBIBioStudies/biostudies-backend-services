package ac.uk.ebi.biostd.submission.service

import ac.uk.ebi.biostd.persistence.common.model.BasicCollection
import ac.uk.ebi.biostd.persistence.common.service.PersistenceService
import ac.uk.ebi.biostd.submission.exceptions.UserCanNotProvideAccessNumber
import ac.uk.ebi.biostd.submission.exceptions.UserCanNotSubmitToCollectionException
import ac.uk.ebi.biostd.submission.exceptions.UserCanNotUpdateSubmit
import ac.uk.ebi.biostd.submission.util.AccNoPatternUtil
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.model.Submission
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

@ExtendWith(MockKExtension::class)
class AccNoServiceTest(
    @MockK private val submission: Submission,
    @MockK private val collection: BasicCollection,
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
        nullValues = ["null"],
    )
    fun getRelPath(
        value: String,
        subBasePath: String?,
        expected: String,
    ) {
        val testInstance = AccNoService(service, accNoPatternUtil, privilegesService, subBasePath)
        assertThat(testInstance.getRelPath(accNoPatternUtil.toAccNumber(value))).isEqualTo(expected)
    }

    @Nested
    inner class WhenIsNew {
        @Test
        fun `when user cannot provide accession`() =
            runTest {
                coEvery { privilegesService.canProvideAccNo(SUBMITTER, "") } returns false

                val error =
                    assertThrows<UserCanNotProvideAccessNumber> {
                        testInstance.calculateAccNo(
                            submitter = SUBMITTER,
                            submission = submission,
                            collection = null,
                            previousVersion = null,
                        )
                    }
                assertThat(error.message)
                    .isEqualTo("The user submiter@email.com is not allowed to provide accession number directly")
            }

        @Test
        fun `when user cannot submit to collection`() =
            runTest {
                coEvery { privilegesService.canSubmitToCollection(SUBMITTER, COLLECTION) } returns false

                val error =
                    assertThrows<UserCanNotSubmitToCollectionException> {
                        testInstance.calculateAccNo(
                            submitter = SUBMITTER,
                            submission = submission,
                            collection = collection,
                            previousVersion = null,
                        )
                    }
                assertThat(error.message)
                    .isEqualTo("The user submiter@email.com is not allowed to submit to CC123 collection")
            }

        @Nested
        inner class WhenAccNo {
            @Test
            fun `when no project`() =
                runTest {
                    coEvery { privilegesService.canProvideAccNo(SUBMITTER, "") } returns true

                    val (accNo, relPath) =
                        testInstance.calculateAccNo(
                            submitter = SUBMITTER,
                            submission = submission,
                            collection = null,
                            previousVersion = null,
                        )
                    assertThat(accNo).isEqualTo(ACC_NO)
                    assertThat(relPath).isEqualTo(REL_PATH)
                }

            @Test
            fun `when project`() =
                runTest {
                    coEvery { privilegesService.canProvideAccNo(SUBMITTER, COLLECTION) } returns true
                    coEvery { privilegesService.canSubmitToCollection(SUBMITTER, COLLECTION) } returns true

                    val (accNo, relPath) =
                        testInstance.calculateAccNo(
                            submitter = SUBMITTER,
                            submission = submission,
                            collection = collection,
                            previousVersion = null,
                        )
                    assertThat(accNo).isEqualTo(ACC_NO)
                    assertThat(relPath).isEqualTo(REL_PATH)
                }
        }

        @Nested
        inner class WhenNoAccNo {
            @Test
            fun `when parent`() =
                runTest {
                    every { submission.accNo } returns ""
                    every { service.getSequenceNextValue("ABC-") } returns 10
                    coEvery { privilegesService.canSubmitToCollection(SUBMITTER, COLLECTION) } returns true

                    val (accNo, relPath) =
                        testInstance.calculateAccNo(
                            submitter = SUBMITTER,
                            submission = submission,
                            collection = collection,
                            previousVersion = null,
                        )
                    assertThat(accNo).isEqualTo("ABC-10")
                    assertThat(relPath).isEqualTo("ABC-/010/ABC-10")
                }

            @Test
            fun `when no parent`() =
                runTest {
                    every { submission.accNo } returns ""
                    every { service.getSequenceNextValue("S-BSST") } returns 99
                    coEvery { privilegesService.canProvideAccNo(SUBMITTER, "") } returns true
                    coEvery { privilegesService.canSubmitToCollection(SUBMITTER, COLLECTION) } returns true

                    val (accNo, relPath) =
                        testInstance.calculateAccNo(
                            submitter = SUBMITTER,
                            submission = submission,
                            collection = null,
                            previousVersion = null,
                        )
                    assertThat(accNo).isEqualTo("S-BSST99")
                    assertThat(relPath).isEqualTo("S-BSST/099/S-BSST99")
                }
        }
    }

    @Nested
    inner class WhenIsNotNew(
        @MockK private val previousVersion: ExtSubmission,
    ) {
        @Test
        fun `when user cannot resubmit`() =
            runTest {
                coEvery { privilegesService.canResubmit(SUBMITTER, ACC_NO) } returns false

                val error =
                    assertThrows<UserCanNotUpdateSubmit> {
                        testInstance.calculateAccNo(
                            submitter = SUBMITTER,
                            submission = submission,
                            collection = collection,
                            previousVersion = previousVersion,
                        )
                    }
                assertThat(error.message)
                    .isEqualTo("The user $SUBMITTER is not allowed to update the submission $ACC_NO")
            }

        @Test
        fun `superuser resubmit`() =
            runTest {
                coEvery { privilegesService.canProvideAccNo(SUBMITTER, COLLECTION) } returns true
                coEvery { privilegesService.canResubmit(SUBMITTER, ACC_NO) } returns true
                coEvery { privilegesService.canSubmitToCollection(SUBMITTER, COLLECTION) } returns true

                val (accNo, relPath) =
                    testInstance.calculateAccNo(
                        submitter = SUBMITTER,
                        submission = submission,
                        collection = collection,
                        previousVersion = previousVersion,
                    )
                assertThat(accNo).isEqualTo(ACC_NO)
                assertThat(relPath).isEqualTo(REL_PATH)
            }

        @Test
        fun `owner regular user resubmit`() =
            runTest {
                coEvery { privilegesService.canProvideAccNo(SUBMITTER, COLLECTION) } returns false
                coEvery { privilegesService.canResubmit(SUBMITTER, ACC_NO) } returns true
                coEvery { privilegesService.canSubmitToCollection(SUBMITTER, COLLECTION) } returns false

                val (accNo, relPath) =
                    testInstance.calculateAccNo(
                        submitter = SUBMITTER,
                        submission = submission,
                        collection = collection,
                        previousVersion = previousVersion,
                    )
                assertThat(accNo).isEqualTo(ACC_NO)
                assertThat(relPath).isEqualTo(REL_PATH)
            }

        @Test
        fun `non owner regular user resubmit`() =
            runTest {
                coEvery { privilegesService.canProvideAccNo(SUBMITTER, COLLECTION) } returns false
                coEvery { privilegesService.canResubmit(SUBMITTER, ACC_NO) } returns false
                coEvery { privilegesService.canSubmitToCollection(SUBMITTER, COLLECTION) } returns true

                val error =
                    assertThrows<UserCanNotUpdateSubmit> {
                        testInstance.calculateAccNo(
                            submitter = SUBMITTER,
                            submission = submission,
                            collection = collection,
                            previousVersion = previousVersion,
                        )
                    }

                assertThat(error.message).isEqualTo(
                    "The user submiter@email.com is not allowed to update the submission AAB12",
                )
            }
    }

    private fun initRequest() {
        every { submission.accNo } returns ACC_NO
        every { collection.accNo } returns COLLECTION
        every { collection.accNoPattern } returns PROJECT_PATTERN
    }

    private companion object {
        const val ACC_NO = "AAB12"
        const val REL_PATH = "AAB/012/AAB12"
        const val SUBMITTER = "submiter@email.com"
        const val COLLECTION = "CC123"
        const val PROJECT_PATTERN = "!{ABC-}"
    }
}

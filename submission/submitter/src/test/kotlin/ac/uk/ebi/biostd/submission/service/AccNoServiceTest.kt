package ac.uk.ebi.biostd.submission.service

import ac.uk.ebi.biostd.persistence.integration.PersistenceContext
import ac.uk.ebi.biostd.persistence.integration.SubmissionQueryService
import ac.uk.ebi.biostd.submission.exceptions.ProvideAccessNumber
import ac.uk.ebi.biostd.submission.exceptions.UserCanNotSubmitToProjectException
import ac.uk.ebi.biostd.submission.util.AccNoPatternUtil
import ebi.ac.uk.model.AccNumber
import ebi.ac.uk.security.integration.components.IUserPrivilegesService
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

private const val SUB_ACC_NO = "AAB12"
private val ACC_NUM = AccNumber("AAB", "12")
private const val SUBMITTER = "submiter@email.com"
private const val PROJECT = "CC123"
private const val PROJECT_PATTERN = "!{ABC-}"

@ExtendWith(MockKExtension::class)
class AccNoServiceTest(
    @MockK private val context: PersistenceContext,
    @MockK private val submissionQueryService: SubmissionQueryService,
    @MockK private val privilegesService: IUserPrivilegesService
) {
    private val accNoPatternUtil: AccNoPatternUtil = AccNoPatternUtil()
    private val testInstance = AccNoService(context, submissionQueryService, accNoPatternUtil, privilegesService)

    @ParameterizedTest(name = "prefix is {0} and numeric value is {1}")
    @CsvSource(
        "S-DIXA-AN-002, S-DIXA-AN-/002/S-DIXA-AN-002",
        "S-BSST11, S-BSST/011/S-BSST11",
        "S-DIXA-011, S-DIXA-/011/S-DIXA-011",
        "1-AAA, 1-AAA/000/1-AAA",
        "S-SCDT-EMBOJ-2019-103549, S-SCDT-EMBOJ-2019-/549/S-SCDT-EMBOJ-2019-103549",
        "S-SCDT-EMBOR-2017-44445V1, S-SCDT-EMBOR-2017-44445V/001/S-SCDT-EMBOR-2017-44445V1")
    fun getRelPath(value: String, expected: String) {
        assertThat(testInstance.getRelPath(accNoPatternUtil.toAccNumber(value))).isEqualTo(expected)
    }

    @Nested
    inner class WhenIsNew {

        @BeforeEach
        fun beforeEach() {
            every { submissionQueryService.isNew(SUB_ACC_NO) } returns true
        }

        @Test
        fun whenUserCanNoProvideAccession() {
            every { privilegesService.canProvideAccNo(SUBMITTER) } returns false

            assertThrows<ProvideAccessNumber> {
                testInstance.getAccNo(AccNoServiceRequest(SUBMITTER, SUB_ACC_NO))
            }
        }

        @Test
        fun whenUserCanNotSubmitToProject() {
            every { privilegesService.canSubmitToProject(SUBMITTER, PROJECT) } returns false

            assertThrows<UserCanNotSubmitToProjectException> {
                testInstance.getAccNo(AccNoServiceRequest(submitter = SUBMITTER, project = PROJECT))
            }
        }

        @Nested
        inner class WhenAccNo {

            @Test
            fun whenNoProject() {
                every { privilegesService.canProvideAccNo(SUBMITTER) } returns true

                assertThat(testInstance.getAccNo(AccNoServiceRequest(submitter = SUBMITTER, accNo = SUB_ACC_NO)))
                    .isEqualTo(ACC_NUM)
            }

            @Test
            fun whenProject() {
                every { privilegesService.canProvideAccNo(SUBMITTER) } returns true
                every { privilegesService.canSubmitToProject(SUBMITTER, PROJECT) } returns true

                assertThat(testInstance.getAccNo(
                    AccNoServiceRequest(submitter = SUBMITTER, accNo = SUB_ACC_NO, project = PROJECT)))
                    .isEqualTo(ACC_NUM)
            }
        }

        @Nested
        inner class WhenNoAccNo {
            @Test
            fun whenParent() {
                every { privilegesService.canSubmitToProject(SUBMITTER, PROJECT) } returns true
                every { context.getSequenceNextValue("ABC-") } returns 10

                assertThat(testInstance.getAccNo(AccNoServiceRequest(
                    submitter = SUBMITTER,
                    project = PROJECT,
                    projectPattern = PROJECT_PATTERN)))
                    .isEqualTo(AccNumber("ABC-", "10"))
            }

            @Test
            fun whenNoParent() {
                every { privilegesService.canProvideAccNo(SUBMITTER) } returns true
                every { privilegesService.canSubmitToProject(SUBMITTER, PROJECT) } returns true
                every { context.getSequenceNextValue("S-BSST") } returns 99

                assertThat(testInstance.getAccNo(AccNoServiceRequest(
                    submitter = SUBMITTER,
                    project = PROJECT)))
                    .isEqualTo(AccNumber("S-BSST", "99"))
            }
        }
    }

    @Nested
    inner class WhenIsNotNew {

        @BeforeEach
        fun beforeEach() {
            every { submissionQueryService.isNew(SUB_ACC_NO) } returns false
        }

        @Test
        fun whenUserCanNotSubmitToProject() {
            every { privilegesService.canSubmitToProject(SUBMITTER, PROJECT) } returns false

            val error = assertThrows<UserCanNotSubmitToProjectException> {
                testInstance.getAccNo(AccNoServiceRequest(submitter = SUBMITTER, accNo = SUB_ACC_NO, project = PROJECT))
            }
            assertThat(error.message).isEqualTo("The user submiter@email.com is not allowed to submit to CC123 project")
        }

        @Test
        fun whenUserCanNotReSubmit() {
            every { privilegesService.canSubmitToProject(SUBMITTER, PROJECT) } returns false

            val error = assertThrows<UserCanNotSubmitToProjectException> {
                testInstance.getAccNo(AccNoServiceRequest(submitter = SUBMITTER, accNo = SUB_ACC_NO, project = PROJECT))
            }
            assertThat(error.message).isEqualTo("The user submiter@email.com is not allowed to submit to CC123 project")
        }
    }
}

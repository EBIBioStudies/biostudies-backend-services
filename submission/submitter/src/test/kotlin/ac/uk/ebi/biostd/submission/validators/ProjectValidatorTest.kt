package ac.uk.ebi.biostd.submission.validators

import ac.uk.ebi.biostd.submission.exceptions.ProjectAccessTagAlreadyExistingException
import ac.uk.ebi.biostd.submission.exceptions.ProjectAlreadyExistingException
import ac.uk.ebi.biostd.submission.exceptions.ProjectInvalidAccNoPatternException
import ac.uk.ebi.biostd.submission.exceptions.UserCanNotSubmitProjectsException
import ac.uk.ebi.biostd.submission.test.createBasicExtendedSubmission
import ac.uk.ebi.biostd.submission.test.createBasicProject
import ac.uk.ebi.biostd.submission.util.AccNoPatternUtil
import ebi.ac.uk.persistence.PersistenceContext
import ebi.ac.uk.security.integration.components.IUserPrivilegesService
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class ProjectValidatorTest(
    @MockK private val accNoPatternUtil: AccNoPatternUtil,
    @MockK private val persistenceContext: PersistenceContext,
    @MockK private val userPrivilegesService: IUserPrivilegesService
) {
    private val project = createBasicProject()
    private val testInstance = ProjectValidator(accNoPatternUtil, userPrivilegesService)

    @BeforeEach
    fun beforeEach() {
        every { persistenceContext.isNew("ABC456") } returns true
        every { accNoPatternUtil.isPattern("!{S-ABC}") } returns true
        every { persistenceContext.accessTagExists("ABC456") } returns false
        every { userPrivilegesService.canSubmitProjects("user@mail.com") } returns true
    }

    @AfterEach
    fun afterEach() = clearAllMocks()

    @Test
    fun validate() {
        testInstance.validate(project, persistenceContext)
        verify(exactly = 1) {
            persistenceContext.isNew("ABC456")
            accNoPatternUtil.isPattern("!{S-ABC}")
            persistenceContext.accessTagExists("ABC456")
            userPrivilegesService.canSubmitProjects("user@mail.com")
        }
    }

    @Test
    fun `user cannot submit`() {
        every { userPrivilegesService.canSubmitProjects("user@mail.com") } returns false

        val error =
            assertThrows<UserCanNotSubmitProjectsException> { testInstance.validate(project, persistenceContext) }
        assertThat(error.message).isEqualTo("The user user@mail.com is not allowed to submit projects")

        verify(exactly = 1) { userPrivilegesService.canSubmitProjects("user@mail.com") }
        verify(exactly = 0) { accNoPatternUtil.isPattern("!{S-ABC}") }
        verify(exactly = 0) {
            persistenceContext.isNew("ABC456")
            persistenceContext.accessTagExists("ABC456")
        }
    }

    @Test
    fun `missing acc no pattern`() {
        val error = assertThrows<ProjectInvalidAccNoPatternException> {
            testInstance.validate(createBasicExtendedSubmission(), persistenceContext)
        }
        assertThat(error.message).isEqualTo(ACC_NO_TEMPLATE_REQUIRED)

        verify(exactly = 1) { userPrivilegesService.canSubmitProjects("user@mail.com") }
        verify(exactly = 0) {
            persistenceContext.isNew("ABC456")
            persistenceContext.accessTagExists("ABC456")
        }
    }

    @Test
    fun `invalid acc no pattern`() {
        every { accNoPatternUtil.isPattern("!{S-ABC}") } returns false

        val error = assertThrows<ProjectInvalidAccNoPatternException> {
            testInstance.validate(project, persistenceContext)
        }
        assertThat(error.message).isEqualTo(ACC_NO_TEMPLATE_INVALID)

        verify(exactly = 1) { userPrivilegesService.canSubmitProjects("user@mail.com") }
        verify(exactly = 0) {
            persistenceContext.isNew("ABC456")
            persistenceContext.accessTagExists("ABC456")
        }
    }

    @Test
    fun `already existing project`() {
        every { persistenceContext.isNew("ABC456") } returns false

        val error = assertThrows<ProjectAlreadyExistingException> { testInstance.validate(project, persistenceContext) }
        assertThat(error.message).isEqualTo("The project ABC456 already exists")

        verify(exactly = 0) { persistenceContext.accessTagExists("ABC456") }
        verify(exactly = 1) { accNoPatternUtil.isPattern("!{S-ABC}") }
        verify(exactly = 1) {
            persistenceContext.isNew("ABC456")
            userPrivilegesService.canSubmitProjects("user@mail.com")
        }
    }

    @Test
    fun `already existing tag`() {
        every { persistenceContext.accessTagExists("ABC456") } returns true

        val error = assertThrows<ProjectAccessTagAlreadyExistingException> {
            testInstance.validate(project, persistenceContext)
        }
        assertThat(error.message).isEqualTo("The access tag with name ABC456 already exists")

        verify(exactly = 1) {
            persistenceContext.isNew("ABC456")
            persistenceContext.accessTagExists("ABC456")
            userPrivilegesService.canSubmitProjects("user@mail.com")
        }
    }
}

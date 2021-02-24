package ac.uk.ebi.biostd.submission.service

import ac.uk.ebi.biostd.persistence.common.service.PersistenceService
import ac.uk.ebi.biostd.submission.exceptions.CollectionAccNoTemplateAlreadyExistsException
import ac.uk.ebi.biostd.submission.exceptions.CollectionAlreadyExistingException
import ac.uk.ebi.biostd.submission.exceptions.CollectionInvalidAccNoPatternException
import ac.uk.ebi.biostd.submission.exceptions.UserCanNotSubmitProjectsException
import ac.uk.ebi.biostd.submission.util.AccNoPatternUtil
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
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@ExtendWith(MockKExtension::class)
class ExtCollectionInfoServiceTest(
    @MockK private val service: PersistenceService,
    @MockK private val accNoUtil: AccNoPatternUtil,
    @MockK private val privilegesService: IUserPrivilegesService
) {
    private val testInstance = ProjectInfoService(service, accNoUtil, privilegesService)

    @AfterEach
    fun afterEach() = clearAllMocks()

    @BeforeEach
    fun beforeEach() {
        initContext()
        initAccNoUtil()
        every { privilegesService.canSubmitProjects("user@test.org") } returns true
    }

    @Test
    fun process() {
        val request = ProjectRequest("user@test.org", "Project", "!{S-PRJ}", "TheProject")
        val response = testInstance.process(request)

        assertNotNull(response)
        assertThat(response.accessTag).isEqualTo("TheProject")
        verify(exactly = 1) { service.saveAccessTag("TheProject") }
        verify(exactly = 1) { service.createAccNoPatternSequence("S-PRJ") }
    }

    @Test
    fun `non project`() {
        val request = ProjectRequest("user@test.org", "Study", "!{S-PRJ}", "TheProject")
        assertNull(testInstance.process(request))
    }

    @Test
    fun `user cant submit projects`() {
        every { privilegesService.canSubmitProjects("user@test.org") } returns false

        val request = ProjectRequest("user@test.org", "Project", "!{S-PRJ}", "TheProject")
        val exception = assertThrows<UserCanNotSubmitProjectsException> { testInstance.process(request) }

        assertThat(exception.message).isEqualTo("The user user@test.org is not allowed to submit projects")
    }

    @Test
    fun `no template`() {
        val request = ProjectRequest("user@test.org", "Project", null, "TheProject")
        val exception = assertThrows<CollectionInvalidAccNoPatternException> { testInstance.process(request) }

        assertThat(exception.message).isEqualTo(ACC_NO_TEMPLATE_REQUIRED)
    }

    @Test
    fun `invalid pattern`() {
        every { accNoUtil.getPattern("Invalid") } returns ""
        every { accNoUtil.isPattern("Invalid") } returns false

        val request = ProjectRequest("user@test.org", "Project", "Invalid", "TheProject")
        val exception = assertThrows<CollectionInvalidAccNoPatternException> { testInstance.process(request) }
        assertThat(exception.message).isEqualTo(ACC_NO_TEMPLATE_INVALID)
    }

    @Test
    fun `already existing project`() {
        every { service.accessTagExists("TheProject") } returns true

        val request = ProjectRequest("user@test.org", "Project", "!{S-PRJ}", "TheProject")
        val exception = assertThrows<CollectionAlreadyExistingException> { testInstance.process(request) }

        assertThat(exception.message).isEqualTo("The project 'TheProject' already exists")
    }

    @Test
    fun `pattern accNo already in use`() {
        every { service.sequenceAccNoPatternExists("S-PRJ") } returns true

        val request = ProjectRequest("user@test.org", "Project", "!{S-PRJ}", "TheProject")
        val exception = assertThrows<CollectionAccNoTemplateAlreadyExistsException> { testInstance.process(request) }

        assertThat(exception.message).isEqualTo("There is a project already using the accNo template 'S-PRJ'")
    }

    private fun initContext() {
        every { service.saveAccessTag("TheProject") } answers { nothing }
        every { service.accessTagExists("TheProject") } returns false
        every { service.sequenceAccNoPatternExists("S-PRJ") } returns false
        every { service.createAccNoPatternSequence("S-PRJ") } answers { nothing }
    }

    private fun initAccNoUtil() {
        every { accNoUtil.getPattern("!{S-PRJ}") } returns "S-PRJ"
        every { accNoUtil.isPattern("!{S-PRJ}") } returns true
    }
}

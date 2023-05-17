package ac.uk.ebi.biostd.submission.service

import ac.uk.ebi.biostd.persistence.common.service.PersistenceService
import ac.uk.ebi.biostd.submission.exceptions.CollectionAccNoTemplateAlreadyExistsException
import ac.uk.ebi.biostd.submission.exceptions.CollectionAlreadyExistingException
import ac.uk.ebi.biostd.submission.exceptions.CollectionInvalidAccNoPatternException
import ac.uk.ebi.biostd.submission.exceptions.UserCanNotSubmitCollectionsException
import ac.uk.ebi.biostd.submission.model.SubmitRequest
import ac.uk.ebi.biostd.submission.util.AccNoPatternUtil
import ebi.ac.uk.model.extensions.accNoTemplate
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
class CollectionProcessorTest(
    @MockK private val request: SubmitRequest,
    @MockK private val service: PersistenceService,
    @MockK private val accNoUtil: AccNoPatternUtil,
    @MockK private val privilegesService: IUserPrivilegesService,
) {
    private val testInstance = CollectionProcessor(service, accNoUtil, privilegesService)

    @AfterEach
    fun afterEach() = clearAllMocks()

    @BeforeEach
    fun beforeEach() {
        initContext()
        initRequest()
        initAccNoUtil()
    }

    @Test
    fun process() {
        assertThat(testInstance.process(request)).isEqualTo("TheCollection")
        verify(exactly = 1) { service.saveAccessTag("TheCollection") }
        verify(exactly = 1) { service.createAccNoPatternSequence("S-PRJ") }
    }

    @Test
    fun `user cant submit collections`() {
        every { privilegesService.canSubmitCollections("user@test.org") } returns false

        val exception = assertThrows<UserCanNotSubmitCollectionsException> { testInstance.process(request) }
        assertThat(exception.message).isEqualTo("The user user@test.org is not allowed to submit collections")
    }

    @Test
    fun `no template`() {
        every { request.submission.accNoTemplate } returns null

        val exception = assertThrows<CollectionInvalidAccNoPatternException> { testInstance.process(request) }
        assertThat(exception.message).isEqualTo(ACC_NO_TEMPLATE_REQUIRED)
    }

    @Test
    fun `invalid pattern`() {
        every { accNoUtil.getPattern("Invalid") } returns ""
        every { accNoUtil.isPattern("Invalid") } returns false
        every { request.submission.accNoTemplate } returns "Invalid"

        val exception = assertThrows<CollectionInvalidAccNoPatternException> { testInstance.process(request) }
        assertThat(exception.message).isEqualTo(ACC_NO_TEMPLATE_INVALID)
    }

    @Test
    fun `already existing collection`() {
        every { service.accessTagExists("TheCollection") } returns true

        val exception = assertThrows<CollectionAlreadyExistingException> { testInstance.process(request) }
        assertThat(exception.message).isEqualTo("The collection 'TheCollection' already exists")
    }

    @Test
    fun `pattern accNo already in use`() {
        every { service.sequenceAccNoPatternExists("S-PRJ") } returns true

        val exception = assertThrows<CollectionAccNoTemplateAlreadyExistsException> { testInstance.process(request) }
        assertThat(exception.message).isEqualTo("There is a collection already using the accNo template 'S-PRJ'")
    }

    private fun initContext() {
        every { service.saveAccessTag("TheCollection") } answers { nothing }
        every { service.accessTagExists("TheCollection") } returns false
        every { service.sequenceAccNoPatternExists("S-PRJ") } returns false
        every { service.createAccNoPatternSequence("S-PRJ") } answers { nothing }
        every { privilegesService.canSubmitCollections("user@test.org") } returns true
    }

    private fun initAccNoUtil() {
        every { accNoUtil.getPattern("!{S-PRJ}") } returns "S-PRJ"
        every { accNoUtil.isPattern("!{S-PRJ}") } returns true
    }

    private fun initRequest() {
        every { request.previousVersion } returns null
        every { request.submission.accNo } returns "TheCollection"
        every { request.submitter.email } returns "user@test.org"
        every { request.submission.accNoTemplate } returns "!{S-PRJ}"
    }
}

package ebi.ac.uk.security.service

import ac.uk.ebi.biostd.persistence.common.model.AccessType.ADMIN
import ac.uk.ebi.biostd.persistence.common.model.AccessType.ATTACH
import ac.uk.ebi.biostd.persistence.common.model.AccessType.DELETE
import ac.uk.ebi.biostd.persistence.common.model.BasicSubmission
import ac.uk.ebi.biostd.persistence.common.service.SubmissionMetaQueryService
import ac.uk.ebi.biostd.persistence.common.service.UserPermissionsService
import ac.uk.ebi.biostd.persistence.repositories.AccessTagDataRepo
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ebi.ac.uk.security.integration.exception.UserNotFoundByEmailException
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import ac.uk.ebi.biostd.persistence.model.DbUser as UserDB

@ExtendWith(MockKExtension::class)
class UserPrivilegesServiceTest(
    @MockK private val author: UserDB,
    @MockK private val otherAuthor: UserDB,
    @MockK private val superuser: UserDB,
    @MockK private val basicSubmission: BasicSubmission,
    @MockK private val userRepository: UserDataRepository,
    @MockK private val queryService: SubmissionMetaQueryService,
    @MockK private val tagsDataRepository: AccessTagDataRepo,
    @MockK private val userPermissionsService: UserPermissionsService
) {
    private val testInstance =
        UserPrivilegesService(userRepository, tagsDataRepository, queryService, userPermissionsService)

    @BeforeEach
    fun beforeEach() {
        initUsers()
        initSubmissionQueries()
    }

    @AfterEach
    fun afterEach() = clearAllMocks()

    @Test
    fun `super user provides acc no`() {
        assertThat(testInstance.canProvideAccNo("superuser@mail.com")).isTrue
    }

    @Test
    fun `regular user provides acc no`() {
        assertThat(testInstance.canProvideAccNo("author@mail.com")).isFalse
    }

    @Test
    fun `super user submits to collection`() {
        assertThat(testInstance.canSubmitToCollection("superuser@mail.com", "A-Collection")).isTrue
    }

    @Test
    fun `regular user without permissions submits to collection`() {
        every { userPermissionsService.hasPermission("author@mail.com", "A-Collection", ADMIN) } returns false
        every { userPermissionsService.hasPermission("author@mail.com", "A-Collection", ATTACH) } returns false

        assertThat(testInstance.canSubmitToCollection("author@mail.com", "A-Collection")).isFalse
    }

    @Test
    fun `regular user with permissions submits to collection`() {
        every { userPermissionsService.hasPermission("author@mail.com", "A-Collection", ADMIN) } returns false
        every { userPermissionsService.hasPermission("author@mail.com", "A-Collection", ATTACH) } returns true

        assertThat(testInstance.canSubmitToCollection("author@mail.com", "A-Collection")).isTrue
    }

    @Test
    fun `resubmit as super user`() {
        assertThat(testInstance.canResubmit("superuser@mail.com", "accNo")).isTrue
    }

    @Test
    fun `submit extended as super user`() {
        assertThat(testInstance.canSubmitExtended("superuser@mail.com")).isTrue
    }

    @Test
    fun `submit extended as regular user`() {
        assertThat(testInstance.canSubmitExtended("author@mail.com")).isFalse
    }

    @Test
    fun `author user with tag access resubmits a submission that is in a collection`(
        @MockK basicSubmission: BasicSubmission
    ) {
        every { basicSubmission.owner } returns "author@mail.com"
        every { queryService.findLatestBasicByAccNo("accNo") } returns basicSubmission

        assertThat(testInstance.canResubmit("author@mail.com", "accNo")).isTrue
    }

    @Test
    fun `super user deletes a submission`() {
        assertThat(testInstance.canDelete("superuser@mail.com", "accNo")).isTrue
    }

    @Test
    fun `author user deletes own private submission`(
        @MockK basicSubmission: BasicSubmission
    ) {
        every { basicSubmission.released } returns false
        every { basicSubmission.owner } returns "author@mail.com"
        every { queryService.findLatestBasicByAccNo("accNo") } returns basicSubmission

        assertThat(testInstance.canDelete("author@mail.com", "accNo")).isTrue
    }

    @Test
    fun `author user deletes own public submission`(
        @MockK basicSubmission: BasicSubmission
    ) {
        every { basicSubmission.released } returns true
        every { basicSubmission.owner } returns "author@mail.com"
        every { queryService.findLatestBasicByAccNo("accNo") } returns basicSubmission

        assertThat(testInstance.canDelete("author@mail.com", "accNo")).isFalse
    }

    @Test
    fun `other author user deletes not own submission`() {
        assertThat(testInstance.canDelete("author@mail.com", "accNo")).isFalse
    }

    @Test
    fun `other author user deletes private submission with tag`() {
        every { queryService.getAccessTags("accNo") } returns listOf("A-Collection")
        every { userPermissionsService.hasPermission("otherAuthor@mail.com", "A-Collection", DELETE) } returns true

        assertThat(testInstance.canDelete("otherAuthor@mail.com", "accNo")).isTrue
    }

    @Test
    fun `other author user deletes public submission with tag`() {
        every { basicSubmission.released } returns true
        every { queryService.getAccessTags("accNo") } returns listOf("A-Collection")
        every { userPermissionsService.hasPermission("otherAuthor@mail.com", "A-Collection", DELETE) } returns true

        assertThat(testInstance.canDelete("otherAuthor@mail.com", "accNo")).isFalse
    }

    @Test
    fun `other author user deletes submission without tag`() {
        assertThat(testInstance.canDelete("otherAuthor@mail.com", "accNo")).isFalse
    }

    @Test
    fun `non existing user`() {
        assertThrows<UserNotFoundByEmailException> { testInstance.canProvideAccNo("empty@mail.com") }
    }

    @Test
    fun `super user release`() {
        assertThat(testInstance.canRelease("superuser@mail.com")).isTrue
    }

    @Test
    fun `regular user release`() {
        every { superuser.superuser } returns false
        assertThat(testInstance.canRelease("superuser@mail.com")).isFalse
    }

    @Test
    fun `super user suppress`() {
        assertThat(testInstance.canSuppress("superuser@mail.com")).isTrue
    }

    @Test
    fun `regular user suppress`() {
        every { superuser.superuser } returns false
        assertThat(testInstance.canSuppress("superuser@mail.com")).isFalse
    }

    private fun initUsers() {
        every { superuser.id } returns 123
        every { superuser.superuser } returns true

        every { author.id } returns 124
        every { author.superuser } returns false

        every { otherAuthor.id } returns 125
        every { otherAuthor.superuser } returns false

        every { userRepository.findByEmail("empty@mail.com") } returns null
        every { userRepository.findByEmail("author@mail.com") } returns author
        every { userRepository.findByEmail("otherAuthor@mail.com") } returns otherAuthor
        every { userRepository.findByEmail("superuser@mail.com") } returns superuser
    }

    private fun initSubmissionQueries() {
        every { basicSubmission.released } returns false
        every { basicSubmission.owner } returns "nottheauthor@mail.com"
        every { queryService.getAccessTags("accNo") } returns emptyList()
        every { queryService.findLatestBasicByAccNo("accNo") } returns basicSubmission
    }
}

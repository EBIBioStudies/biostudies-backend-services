package ebi.ac.uk.security.service

import ac.uk.ebi.biostd.persistence.common.model.AccessType.ADMIN
import ac.uk.ebi.biostd.persistence.common.model.AccessType.ATTACH
import ac.uk.ebi.biostd.persistence.common.model.AccessType.DELETE
import ac.uk.ebi.biostd.persistence.common.model.AccessType.UPDATE
import ac.uk.ebi.biostd.persistence.common.model.BasicSubmission
import ac.uk.ebi.biostd.persistence.common.service.SubmissionMetaQueryService
import ac.uk.ebi.biostd.persistence.common.service.UserPermissionsService
import ac.uk.ebi.biostd.persistence.repositories.AccessTagDataRepo
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ebi.ac.uk.security.integration.exception.UserNotFoundByEmailException
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.util.Optional
import ac.uk.ebi.biostd.persistence.model.DbUser as UserDB

@ExtendWith(MockKExtension::class)
class UserPrivilegesServiceTest(
    @MockK private val author: UserDB,
    @MockK private val otherAuthor: UserDB,
    @MockK private val superuser: UserDB,
    @MockK private val adminUser: UserDB,
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

    @Test
    fun `super user provides acc no`() {
        assertThat(testInstance.canProvideAccNo("superuser@mail.com")).isTrue()
    }

    @Test
    fun `regular user provides acc no`() {
        every { superuser.superuser } returns false
        assertThat(testInstance.canProvideAccNo("superuser@mail.com")).isFalse()
    }

    @Test
    fun `resubmit as super user`() {
        assertThat(testInstance.canResubmit("superuser@mail.com", "accNo")).isTrue()
    }

    @Test
    fun `submit extended as super user`() {
        assertThat(testInstance.canSubmitExtended("superuser@mail.com")).isTrue()
    }

    @Test
    fun `submit extended as regular user`() {
        assertThat(testInstance.canSubmitExtended("author@mail.com")).isFalse()
    }

    @Test
    fun `author user with tag resubmits a submission that is in a project`(
        @MockK basicSubmission: BasicSubmission
    ) {
        every { basicSubmission.owner } returns "author@mail.com"
        every { queryService.findLatestBasicByAccNo("accNo") } returns basicSubmission

        assertThat(testInstance.canResubmit("author@mail.com", "accNo")).isTrue()
    }

    @Test
    fun `admin user with tag resubmits a submission that is in a project`() {
        every { queryService.getAccessTags("accNo") } returns listOf("tagName1", "tagName2")
        every { userPermissionsService.hasPermission("admin@mail.com", "tagName1", UPDATE) } returns false
        every { userPermissionsService.hasPermission("admin@mail.com", "tagName2", UPDATE) } returns true
        every { userPermissionsService.hasPermission("admin@mail.com", "tagName1", ADMIN) } returns true
        every { userPermissionsService.hasPermission("admin@mail.com", "tagName2", ADMIN) } returns false

        assertThat(testInstance.canResubmit("admin@mail.com", "accNo")).isTrue()
    }

    @Test
    fun `super user deletes a submission`() {
        assertThat(testInstance.canDelete("superuser@mail.com", "accNo")).isTrue()
    }

    @Test
    fun `author user deletes own submission`(@MockK basicSubmission: BasicSubmission) {
        every { basicSubmission.owner } returns "author@mail.com"
        every { queryService.findLatestBasicByAccNo("accNo") } returns basicSubmission

        assertThat(testInstance.canDelete("author@mail.com", "accNo")).isTrue()
    }

    @Test
    fun `author user deletes not own submission`() {
        assertThat(testInstance.canDelete("author@mail.com", "accNo")).isFalse()
    }

    @Test
    fun `other author user deletes submission with tag`() {
        every { queryService.getAccessTags("accNo") } returns listOf("A-Project")
        every { userPermissionsService.hasPermission("otherAuthor@mail.com", "A-Project", DELETE) } returns true
        every { userPermissionsService.hasPermission("otherAuthor@mail.com", "A-Project", ADMIN) } returns false

        assertThat(testInstance.canDelete("otherAuthor@mail.com", "accNo")).isTrue()
    }

    @Test
    fun `other author user deletes submission without tag`() {
        assertThat(testInstance.canDelete("otherAuthor@mail.com", "accNo")).isFalse()
    }

    @Test
    fun `admin deletes submission with tags`() {
        every { queryService.getAccessTags("accNo") } returns listOf("tagName1", "tagName2")
        every { userPermissionsService.hasPermission("admin@mail.com", "tagName1", DELETE) } returns false
        every { userPermissionsService.hasPermission("admin@mail.com", "tagName2", DELETE) } returns true
        every { userPermissionsService.hasPermission("admin@mail.com", "tagName1", ADMIN) } returns true
        every { userPermissionsService.hasPermission("admin@mail.com", "tagName2", ADMIN) } returns false

        assertThat(testInstance.canDelete("admin@mail.com", "accNo")).isTrue()
    }

    @Test
    fun `admin attach submission to project`() {
        every { userPermissionsService.hasPermission("admin@mail.com", "projectName", ATTACH) } returns false
        every { userPermissionsService.hasPermission("admin@mail.com", "projectName", ADMIN) } returns true

        assertThat(testInstance.canSubmitToProject("admin@mail.com", "projectName")).isTrue()
    }

    @Test
    fun `non existing user`() {
        assertThrows<UserNotFoundByEmailException> { testInstance.canProvideAccNo("empty@mail.com") }
    }

    private fun initUsers() {
        every { superuser.id } returns 123
        every { superuser.superuser } returns true

        every { author.id } returns 124
        every { author.superuser } returns false

        every { otherAuthor.id } returns 125
        every { otherAuthor.superuser } returns false

        every { adminUser.id } returns 126
        every { adminUser.superuser } returns false

        every { userRepository.findByEmail("empty@mail.com") } returns Optional.empty()
        every { userRepository.findByEmail("author@mail.com") } returns Optional.of(author)
        every { userRepository.findByEmail("otherAuthor@mail.com") } returns Optional.of(otherAuthor)
        every { userRepository.findByEmail("superuser@mail.com") } returns Optional.of(superuser)
        every { userRepository.findByEmail("admin@mail.com") } returns Optional.of(adminUser)
    }

    private fun initSubmissionQueries() {
        every { basicSubmission.owner } returns "nottheauthor@mail.com"
        every { queryService.getAccessTags("accNo") } returns emptyList()
        every { queryService.findLatestBasicByAccNo("accNo") } returns basicSubmission
    }
}

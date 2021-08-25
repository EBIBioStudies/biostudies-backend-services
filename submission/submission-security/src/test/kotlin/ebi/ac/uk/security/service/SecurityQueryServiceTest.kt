package ebi.ac.uk.security.service

import ac.uk.ebi.biostd.persistence.model.DbUser
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import arrow.core.Option
import ebi.ac.uk.security.integration.exception.UserAlreadyRegister
import ebi.ac.uk.security.integration.exception.UserNotFoundByTokenException
import ebi.ac.uk.security.integration.model.api.SecurityUser
import ebi.ac.uk.security.integration.model.api.UserInfo
import ebi.ac.uk.security.util.SecurityUtil
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class SecurityQueryServiceTest(
    @MockK private val securityUtil: SecurityUtil,
    @MockK private val profileService: ProfileService,
    @MockK private val userRepository: UserDataRepository
) {
    private val testInstance = SecurityQueryService(securityUtil, profileService, userRepository)

    @AfterEach
    fun afterEach() = clearAllMocks()

    @Test
    fun `user exist by email`() {
        every { userRepository.existsByEmailAndActive("user@test.org", active = true) } returns true

        assertThat(testInstance.existsByEmail("user@test.org")).isTrue
        verify(exactly = 1) { userRepository.existsByEmailAndActive("user@test.org", active = true) }
    }

    @Test
    fun `user does not exist by email`() {
        every { userRepository.existsByEmailAndActive("user@test.org", active = true) } returns false

        assertThat(testInstance.existsByEmail("user@test.org")).isFalse
        verify(exactly = 1) { userRepository.existsByEmailAndActive("user@test.org", active = true) }
    }

    @Test
    fun `get user`(
        @MockK dbUser: DbUser,
        @MockK securityUser: SecurityUser
    ) {
        every { profileService.asSecurityUser(dbUser) } returns securityUser
        every { userRepository.findByEmailAndActive("user@test.org", true) } returns dbUser

        assertThat(testInstance.getUser("user@test.org")).isEqualTo(securityUser)
    }

    @Test
    fun `get non existing user`() {
        every { userRepository.findByEmailAndActive("user@test.org", true) } returns null

        val err = assertThrows<UserAlreadyRegister> { testInstance.getUser("user@test.org") }
        assertThat(err.message).isEqualTo("There is a user already registered with the email address 'user@test.org'.")
    }

    @Test
    fun `get user profile`(
        @MockK dbUser: DbUser,
        @MockK userInfo: UserInfo
    ) {
        every { profileService.getUserProfile(dbUser, "the-token") } returns userInfo
        every { securityUtil.checkToken("the-token") } returns Option.fromNullable(dbUser)

        assertThat(testInstance.getUserProfile("the-token")).isEqualTo(userInfo)
    }

    @Test
    fun `get non existing user profile`() {
        every { securityUtil.checkToken("the-token") } returns Option.empty()

        val err = assertThrows<UserNotFoundByTokenException> { testInstance.getUserProfile("the-token") }
        assertThat(err.message).isEqualTo("Could not find an active session for the provided security token.")
    }

    @Test
    fun `get or create when the user exists`(
        @MockK dbUser: DbUser,
        @MockK securityUser: SecurityUser
    ) {
        every { profileService.asSecurityUser(dbUser) } returns securityUser
        every { userRepository.findByEmail("user@test.org") } returns dbUser

        assertThat(testInstance.getOrCreateInactive("user@test.org", "Test User")).isEqualTo(securityUser)
    }

    @Test
    fun `get or create when the user does not exist`(
        @MockK dbUser: DbUser,
        @MockK securityUser: SecurityUser
    ) {
        val dbUserSlot = slot<DbUser>()
        every { securityUtil.newKey() } returns "a-new-key"
        every { userRepository.save(capture(dbUserSlot)) } returns dbUser
        every { profileService.asSecurityUser(dbUser) } returns securityUser
        every { userRepository.findByEmail("user@test.org") } returns null

        assertThat(testInstance.getOrCreateInactive("user@test.org", "Test User")).isEqualTo(securityUser)
        verifyInactiveUserCreation(dbUserSlot.captured)
    }

    @Test
    fun `get or register when the user exists`(
        @MockK dbUser: DbUser,
        @MockK securityUser: SecurityUser
    ) {
        every { profileService.asSecurityUser(dbUser) } returns securityUser
        every { userRepository.findByEmail("user@test.org") } returns dbUser

        assertThat(testInstance.getOrCreateInactive("user@test.org", "Test User")).isEqualTo(securityUser)
    }

    private fun verifyInactiveUserCreation(dbUser: DbUser) {
        assertThat(dbUser.email).isEqualTo("user@test.org")
        assertThat(dbUser.fullName).isEqualTo("Test User")
        assertThat(dbUser.secret).isEqualTo("a-new-key")
        assertThat(dbUser.activationKey).isEqualTo("a-new-key")
        assertThat(dbUser.passwordDigest).isEmpty()
        assertThat(dbUser.active).isFalse
        assertThat(dbUser.notificationsEnabled).isFalse
        verify(exactly = 1) { userRepository.save(dbUser) }
    }
}

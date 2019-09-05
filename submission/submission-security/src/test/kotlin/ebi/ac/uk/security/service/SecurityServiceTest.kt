package ebi.ac.uk.security.service

import ac.uk.ebi.biostd.persistence.model.User
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ebi.ac.uk.api.security.ChangePasswordRequest
import ebi.ac.uk.api.security.LoginRequest
import ebi.ac.uk.security.events.Events
import ebi.ac.uk.security.integration.SecurityProperties
import ebi.ac.uk.security.integration.exception.LoginException
import ebi.ac.uk.security.integration.exception.UserAlreadyRegister
import ebi.ac.uk.security.integration.exception.UserNotFoundByEmailException
import ebi.ac.uk.security.integration.exception.UserPendingRegistrationException
import ebi.ac.uk.security.integration.exception.UserWithActivationKeyNotFoundException
import ebi.ac.uk.security.integration.model.api.UserInfo
import ebi.ac.uk.security.integration.model.events.PasswordReset
import ebi.ac.uk.security.integration.model.events.UserActivated
import ebi.ac.uk.security.integration.model.events.UserRegister
import ebi.ac.uk.security.test.SecurityTestEntities
import ebi.ac.uk.security.test.SecurityTestEntities.Companion.email
import ebi.ac.uk.security.test.SecurityTestEntities.Companion.instanceKey
import ebi.ac.uk.security.test.SecurityTestEntities.Companion.password
import ebi.ac.uk.security.test.SecurityTestEntities.Companion.passwordDiggest
import ebi.ac.uk.security.test.SecurityTestEntities.Companion.path
import ebi.ac.uk.security.test.SecurityTestEntities.Companion.registrationRequest
import ebi.ac.uk.security.test.SecurityTestEntities.Companion.resetPasswordRequest
import ebi.ac.uk.security.test.SecurityTestEntities.Companion.retryActivation
import ebi.ac.uk.security.test.SecurityTestEntities.Companion.securityUser
import ebi.ac.uk.security.test.SecurityTestEntities.Companion.simpleUser
import ebi.ac.uk.security.test.SecurityTestEntities.Companion.username
import ebi.ac.uk.security.util.SecurityUtil
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.reactivex.observers.TestObserver
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.util.Optional

private const val ACTIVATION_KEY: String = "code"
private const val SECRET_KEY: String = "secretKey"
private val PASSWORD_DIGGEST: ByteArray = ByteArray(0)

@ExtendWith(MockKExtension::class)
internal class SecurityServiceTest(
    @MockK private val userRepository: UserDataRepository,
    @MockK private val securityProps: SecurityProperties,
    @MockK private val securityUtil: SecurityUtil,
    @MockK private val profileService: ProfileService
) {
    private val testInstance: SecurityService =
        SecurityService(userRepository, securityUtil, securityProps, profileService)

    @Nested
    inner class Login {
        @Test
        fun `login when user is not found`() {
            every { userRepository.findByLoginOrEmailAndActive(username, username, true) } returns Optional.empty()

            assertThrows<LoginException> { testInstance.login(LoginRequest(username, password)) }
        }

        @Test
        fun `login when invalid password`() {
            every { userRepository.findByLoginOrEmailAndActive(username, username, true) } returns Optional.of(simpleUser)
            every { securityUtil.checkPassword(passwordDiggest, password) } returns false

            assertThrows<LoginException> { testInstance.login(LoginRequest(username, password)) }
        }

        @Test
        fun login() {
            val userToken = "token"

            every { userRepository.findByLoginOrEmailAndActive(username, username, true) } returns Optional.of(simpleUser)
            every { securityUtil.checkPassword(passwordDiggest, password) } returns true
            every { securityUtil.createToken(simpleUser) } returns userToken
            every { profileService.getUserProfile(simpleUser, userToken) } returns UserInfo(securityUser, userToken)

            val (user, token) = testInstance.login(LoginRequest(username, password))

            assertThat(user).isEqualTo(securityUser)
            assertThat(token).isEqualTo(userToken)
        }
    }

    @Nested
    inner class Registration {
        @BeforeEach
        fun beforeEach() {
            every { userRepository.existsByEmail(email) } returns false
            every { userRepository.save(any<User>()) } answers { firstArg() }
            every { securityUtil.getPasswordDigest(password) } returns PASSWORD_DIGGEST
            every { profileService.asSecurityUser(any()) } returns securityUser
        }

        @Test
        fun `register a user when not activation is not required`() {
            every { securityProps.requireActivation } returns false
            every { securityUtil.newKey() } returns SECRET_KEY

            val subscriber = TestObserver<UserActivated>()
            Events.userRegister.subscribe(subscriber)

            testInstance.registerUser(registrationRequest)

            assertThat(subscriber.values()).hasSize(1)
            assertThat(subscriber.values()).first().satisfies {
                assertThat(it.user.active).isTrue()
                assertThat(it.user.fullName).isEqualTo(username)
                assertThat(it.user.email).isEqualTo(email)
                assertThat(it.user.passwordDigest).isEqualTo(PASSWORD_DIGGEST)

                assertThat(it.user.superuser).isFalse()
                assertThat(it.user.activationKey).isNull()
                assertThat(it.user.login).isNull()
            }
        }

        @Test
        fun `register a user when activation is required`() {
            val activationUrl = "http://dummy-backend.com/active/1234"
            every { securityProps.requireActivation } returns true
            every { securityUtil.newKey() } returns SECRET_KEY andThen ACTIVATION_KEY
            every { securityUtil.getActivationUrl(instanceKey, path, ACTIVATION_KEY) } returns activationUrl

            val subscriber = TestObserver<UserRegister>()
            Events.userPreRegister.subscribe(subscriber)

            testInstance.registerUser(SecurityTestEntities.preRegisterRequest)

            assertThat(subscriber.values()).hasSize(1)
            assertThat(subscriber.values()).first().satisfies {
                assertThat(it.user.active).isFalse()
                assertThat(it.user.activationKey).isEqualTo(ACTIVATION_KEY)
            }
        }

        @Test
        fun `register user when user already exist`() {
            every { userRepository.existsByEmail(email) } returns true

            val exception = assertThrows<UserAlreadyRegister> { testInstance.registerUser(registrationRequest) }
            assertThat(exception.message).isEqualTo("There is already a user registered with email address 'Jhon.Doe@test.com'.")
        }
    }

    @Nested
    inner class Activation {
        @Test
        fun `activate when not pending activation`() {
            every { userRepository.findByActivationKeyAndActive(ACTIVATION_KEY, false) } returns Optional.empty()

            assertThrows<UserWithActivationKeyNotFoundException> { testInstance.activate(ACTIVATION_KEY) }
        }

        @Test
        fun `activate when user is found`() {
            val user = simpleUser
            every { userRepository.findByActivationKeyAndActive(ACTIVATION_KEY, false) } returns Optional.of(user)
            every { userRepository.save(any<User>()) } answers { firstArg() }

            testInstance.activate(ACTIVATION_KEY)

            assertThat(user.active).isTrue()
            assertThat(user.activationKey).isNull()
        }
    }

    @Nested
    inner class Retry {
        @Test
        fun `retry pre registration when user not found`() {
            every { userRepository.findByEmailAndActive(email, false) } returns Optional.empty()

            assertThrows<UserPendingRegistrationException> { testInstance.retryRegistration(retryActivation) }
        }

        @Test
        fun `retry pre registration`() {
            val activationUrl = "http://dummy-backend.com/active/1234"
            val user = simpleUser.apply { active = false }

            every { userRepository.findByEmailAndActive(email, false) } returns Optional.of(user)
            every { securityUtil.newKey() } returns ACTIVATION_KEY
            every { securityUtil.getActivationUrl(instanceKey, path, ACTIVATION_KEY) } returns activationUrl
            every { userRepository.save(any<User>()) } answers { firstArg() }
            every { profileService.asSecurityUser(any()) } returns securityUser

            val subscriber = TestObserver<UserRegister>()
            Events.userPreRegister.subscribe(subscriber)

            testInstance.retryRegistration(retryActivation)

            assertThat(subscriber.values()).hasSize(1)
            assertThat(subscriber.values()).first().satisfies {
                assertThat(it.user.active).isFalse()
                assertThat(it.user.activationKey).isEqualTo(ACTIVATION_KEY)
            }
        }
    }

    @Nested
    inner class ChangePassword {
        private val password = "new password"

        @Test
        fun `change password when not activate user found`() {
            every { userRepository.findByActivationKeyAndActive(ACTIVATION_KEY, true) } returns Optional.empty()

            assertThrows<UserWithActivationKeyNotFoundException> { testInstance.changePassword(ChangePasswordRequest(ACTIVATION_KEY, password)) }
        }

        @Test
        fun `change password`() {
            val passwordDiggest = ByteArray(0)
            every { userRepository.findByActivationKeyAndActive(ACTIVATION_KEY, true) } returns Optional.of(simpleUser)
            every { securityUtil.getPasswordDigest(password) } returns passwordDiggest
            every { userRepository.save(any<User>()) } answers { firstArg() }

            testInstance.changePassword(ChangePasswordRequest(ACTIVATION_KEY, "new password"))

            assertThat(simpleUser.activationKey).isNull()
            assertThat(simpleUser.passwordDigest).isEqualTo(passwordDiggest)
        }
    }

    @Nested
    inner class ResetPassword {
        @Test
        fun `reset password when user not found`() {
            every { userRepository.findByLoginOrEmailAndActive(email, email, true) } returns Optional.empty()

            assertThrows<UserNotFoundByEmailException> { testInstance.resetPassword(resetPasswordRequest) }
        }

        @Test
        fun resetPassword() {
            val activationUrl = "http://dummy-backend.com/active/1234"

            every { userRepository.findByLoginOrEmailAndActive(email, email, true) } returns Optional.of(simpleUser)
            every { securityUtil.newKey() } returns ACTIVATION_KEY
            every { userRepository.save(any<User>()) } answers { firstArg() }
            every { securityUtil.getActivationUrl(instanceKey, path, ACTIVATION_KEY) } returns activationUrl

            val subscriber = TestObserver<PasswordReset>()
            Events.passwordReset.subscribe(subscriber)

            testInstance.resetPassword(resetPasswordRequest)

            assertThat(subscriber.values()).hasSize(1)
            assertThat(subscriber.values()).first().satisfies {
                assertThat(it.user).isEqualTo(simpleUser)
                assertThat(it.activationLink).isEqualTo(activationUrl)
            }
        }
    }
}

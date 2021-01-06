package ebi.ac.uk.security.service

import ac.uk.ebi.biostd.common.properties.SecurityProperties
import ac.uk.ebi.biostd.persistence.model.DbUser
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ebi.ac.uk.api.security.ChangePasswordRequest
import ebi.ac.uk.api.security.LoginRequest
import ebi.ac.uk.extended.events.SecurityNotification
import ebi.ac.uk.extended.events.SecurityNotificationType.ACTIVATION
import ebi.ac.uk.extended.events.SecurityNotificationType.PASSWORD_RESET
import ebi.ac.uk.io.RWXRWX___
import ebi.ac.uk.io.RWX__X___
import ebi.ac.uk.security.integration.exception.LoginException
import ebi.ac.uk.security.integration.exception.UserAlreadyRegister
import ebi.ac.uk.security.integration.exception.UserNotFoundByEmailException
import ebi.ac.uk.security.integration.exception.UserPendingRegistrationException
import ebi.ac.uk.security.integration.exception.UserWithActivationKeyNotFoundException
import ebi.ac.uk.security.test.SecurityTestEntities
import ebi.ac.uk.security.test.SecurityTestEntities.Companion.captcha
import ebi.ac.uk.security.test.SecurityTestEntities.Companion.email
import ebi.ac.uk.security.test.SecurityTestEntities.Companion.instanceKey
import ebi.ac.uk.security.test.SecurityTestEntities.Companion.name
import ebi.ac.uk.security.test.SecurityTestEntities.Companion.password
import ebi.ac.uk.security.test.SecurityTestEntities.Companion.passwordDiggest
import ebi.ac.uk.security.test.SecurityTestEntities.Companion.path
import ebi.ac.uk.security.test.SecurityTestEntities.Companion.registrationRequest
import ebi.ac.uk.security.test.SecurityTestEntities.Companion.resetPasswordRequest
import ebi.ac.uk.security.test.SecurityTestEntities.Companion.retryActivation
import ebi.ac.uk.security.test.SecurityTestEntities.Companion.simpleUser
import ebi.ac.uk.security.util.SecurityUtil
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.events.service.EventsPublisherService
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.PosixFilePermission
import java.util.Optional
import kotlin.test.assertNotNull

private const val ACTIVATION_KEY: String = "code"
private const val SECRET_KEY: String = "secretKey"
private val PASSWORD_DIGEST: ByteArray = ByteArray(0)

@ExtendWith(MockKExtension::class, TemporaryFolderExtension::class)
internal class SecurityServiceTest(
    val temporaryFolder: TemporaryFolder,
    @MockK private val userRepository: UserDataRepository,
    @MockK private val securityProps: SecurityProperties,
    @MockK private val securityUtil: SecurityUtil,
    @MockK private val captchaVerifier: CaptchaVerifier,
    @MockK private val eventsPublisherService: EventsPublisherService
) {
    private val testInstance: SecurityService = SecurityService(
        userRepository,
        securityUtil,
        securityProps,
        ProfileService(temporaryFolder.root.toPath()),
        captchaVerifier,
        eventsPublisherService
    )

    @Nested
    inner class Login {
        @Test
        fun `login when user is not found`() {
            every { userRepository.findByLoginOrEmailAndActive(email, email, true) } returns Optional.empty()

            assertThrows<LoginException> { testInstance.login(LoginRequest(email, password)) }
        }

        @Test
        fun `login when invalid password`() {
            every { userRepository.findByLoginOrEmailAndActive(email, email, true) } returns Optional.of(simpleUser)
            every { securityUtil.checkPassword(passwordDiggest, password) } returns false

            assertThrows<LoginException> { testInstance.login(LoginRequest(email, password)) }
        }

        @Test
        fun login() {
            val userToken = "token"

            every { userRepository.findByLoginOrEmailAndActive(email, email, true) } returns Optional.of(simpleUser)
            every { securityUtil.checkPassword(passwordDiggest, password) } returns true
            every { securityUtil.createToken(simpleUser) } returns userToken

            val (user, token) = testInstance.login(LoginRequest(email, password))

            assertNotNull(user)
            assertThat(token).isEqualTo(userToken)
        }
    }

    @Nested
    inner class Registration {
        @BeforeEach
        fun beforeEach() {
            every { userRepository.existsByEmail(email) } returns false
            every { userRepository.save(any<DbUser>()) } answers { firstArg() }
            every { securityUtil.getPasswordDigest(password) } returns PASSWORD_DIGEST
            every { securityProps.checkCaptcha } returns true
            every { captchaVerifier.verifyCaptcha(captcha) } returns Unit
        }

        @Test
        fun `register a user when activation is not required`() {
            val savedUserSlot = slot<DbUser>()
            val magicFolderRoot = temporaryFolder.createDirectory("users")

            every { userRepository.save(capture(savedUserSlot)) } answers { savedUserSlot.captured }
            every { securityProps.magicDirPath } returns magicFolderRoot.absolutePath
            every { securityProps.requireActivation } returns false
            every { securityUtil.newKey() } returns SECRET_KEY

            val securityUser = testInstance.registerUser(registrationRequest)
            val dbUser = savedUserSlot.captured
            assertThat(dbUser.active).isTrue()
            assertThat(dbUser.fullName).isEqualTo(name)
            assertThat(dbUser.email).isEqualTo(email)
            assertThat(dbUser.passwordDigest).isEqualTo(PASSWORD_DIGEST)

            assertThat(dbUser.superuser).isFalse()
            assertThat(dbUser.activationKey).isNull()
            assertThat(dbUser.login).isNull()

            val userFolder = securityUser.magicFolder.path
            assertFile(userFolder.parent, RWX__X___)
            assertFile(userFolder, RWXRWX___)
            assertSymbolicLink(magicFolderRoot.resolve("b/$email").toPath(), userFolder)
        }

        private fun assertSymbolicLink(link: Path, target: Path) {
            assertThat(link).exists()
            assertThat(Files.readSymbolicLink(link)).isEqualTo(target)
        }

        private fun assertFile(path: Path, expectedPermission: Set<PosixFilePermission>) {
            assertThat(path).exists()
            assertThat(Files.getPosixFilePermissions(path)).containsExactlyInAnyOrderElementsOf(expectedPermission)
        }

        @Test
        fun `register a user when activation is required`() {
            val savedUserSlot = slot<DbUser>()
            val activationSlot = slot<SecurityNotification>()
            val activationUrl = "http://dummy-backend.com/active/1234"

            every { securityProps.requireActivation } returns true
            every { securityUtil.newKey() } returns SECRET_KEY andThen ACTIVATION_KEY
            every { securityUtil.getActivationUrl(instanceKey, path, ACTIVATION_KEY) } returns activationUrl
            every { eventsPublisherService.securityNotification(capture(activationSlot)) } answers { nothing }
            every { userRepository.save(capture(savedUserSlot)) } answers { savedUserSlot.captured }

            testInstance.registerUser(SecurityTestEntities.preRegisterRequest)

            val user = savedUserSlot.captured
            assertThat(user.active).isFalse()
            assertThat(user.activationKey).isEqualTo(ACTIVATION_KEY)

            val notification = activationSlot.captured
            assertThat(notification.email).isEqualTo(user.email)
            assertThat(notification.username).isEqualTo(user.fullName)
            assertThat(notification.activationLink).isEqualTo(activationUrl)
            assertThat(notification.type).isEqualTo(ACTIVATION)
        }

        @Test
        fun `register user when user already exist`() {
            every { userRepository.existsByEmail(email) } returns true

            val exception = assertThrows<UserAlreadyRegister> { testInstance.registerUser(registrationRequest) }
            assertThat(exception.message).isEqualTo("There is already a user registered with the email address '$email'.")
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
            every { userRepository.save(any<DbUser>()) } answers { firstArg() }
            every { securityProps.magicDirPath } returns temporaryFolder.createDirectory("users").absolutePath

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
            val savedUserSlot = slot<DbUser>()
            val activationSlot = slot<SecurityNotification>()
            val user = simpleUser.apply { active = false }
            val activationUrl = "http://dummy-backend.com/active/1234"

            every { userRepository.findByEmailAndActive(email, false) } returns Optional.of(user)
            every { securityUtil.newKey() } returns ACTIVATION_KEY
            every { securityUtil.getActivationUrl(instanceKey, path, ACTIVATION_KEY) } returns activationUrl
            every { userRepository.save(capture(savedUserSlot)) } answers { savedUserSlot.captured }
            every { eventsPublisherService.securityNotification(capture(activationSlot)) } answers { nothing }

            testInstance.retryRegistration(retryActivation)

            val dbUser = savedUserSlot.captured
            assertThat(dbUser.active).isFalse()
            assertThat(dbUser.activationKey).isEqualTo(ACTIVATION_KEY)
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
            every { userRepository.save(any<DbUser>()) } answers { firstArg() }

            testInstance.changePassword(ChangePasswordRequest(ACTIVATION_KEY, "new password"))

            assertThat(simpleUser.activationKey).isNull()
            assertThat(simpleUser.passwordDigest).isEqualTo(passwordDiggest)
        }
    }

    @Nested
    inner class ResetPassword {
        @BeforeEach
        fun beforeEach() {
            every { securityProps.checkCaptcha } returns true
            every { captchaVerifier.verifyCaptcha(captcha) } returns Unit
        }

        @Test
        fun `reset password when user not found`() {
            every { userRepository.findByLoginOrEmailAndActive(email, email, true) } returns Optional.empty()

            assertThrows<UserNotFoundByEmailException> { testInstance.resetPassword(resetPasswordRequest) }
        }

        @Test
        fun resetPassword() {
            val resetSlot = slot<SecurityNotification>()
            val activationUrl = "http://dummy-backend.com/active/1234"

            every { userRepository.findByLoginOrEmailAndActive(email, email, true) } returns Optional.of(simpleUser)
            every { securityUtil.newKey() } returns ACTIVATION_KEY
            every { userRepository.save(any<DbUser>()) } answers { firstArg() }
            every { securityUtil.getActivationUrl(instanceKey, path, ACTIVATION_KEY) } returns activationUrl
            every { eventsPublisherService.securityNotification(capture(resetSlot)) } answers { nothing }

            testInstance.resetPassword(resetPasswordRequest)

            val notification = resetSlot.captured
            assertThat(notification.email).isEqualTo(simpleUser.email)
            assertThat(notification.username).isEqualTo(simpleUser.fullName)
            assertThat(notification.activationLink).isEqualTo(activationUrl)
            assertThat(notification.type).isEqualTo(PASSWORD_RESET)
        }
    }
}

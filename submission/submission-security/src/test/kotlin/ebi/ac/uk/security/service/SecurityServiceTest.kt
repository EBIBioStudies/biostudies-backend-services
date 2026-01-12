package ebi.ac.uk.security.service

import ac.uk.ebi.biostd.common.properties.FilesProperties
import ac.uk.ebi.biostd.common.properties.SecurityProperties
import ac.uk.ebi.biostd.common.properties.StorageMode
import ac.uk.ebi.biostd.persistence.common.model.AccessType.ADMIN
import ac.uk.ebi.biostd.persistence.common.service.SubmissionFilesPersistenceService
import ac.uk.ebi.biostd.persistence.model.DbUser
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ebi.ac.uk.api.security.ChangePasswordRequest
import ebi.ac.uk.api.security.LoginRequest
import ebi.ac.uk.extended.events.SecurityNotification
import ebi.ac.uk.extended.events.SecurityNotificationType.ACTIVATION
import ebi.ac.uk.extended.events.SecurityNotificationType.ACTIVATION_BY_EMAIL
import ebi.ac.uk.extended.events.SecurityNotificationType.PASSWORD_RESET
import ebi.ac.uk.io.RWXRWX___
import ebi.ac.uk.io.RWX__X___
import ebi.ac.uk.security.integration.components.IUserPrivilegesService
import ebi.ac.uk.security.integration.exception.ActKeyNotFoundException
import ebi.ac.uk.security.integration.exception.LoginException
import ebi.ac.uk.security.integration.exception.UserAlreadyRegister
import ebi.ac.uk.security.integration.exception.UserNotFoundByEmailException
import ebi.ac.uk.security.integration.exception.UserPendingRegistrationException
import ebi.ac.uk.security.integration.exception.UserWithActivationKeyNotFoundException
import ebi.ac.uk.security.integration.model.api.FtpUserFolder
import ebi.ac.uk.security.integration.model.api.NfsUserFolder
import ebi.ac.uk.security.service.SecurityService.Companion.UNIX_RWXRWX___
import ebi.ac.uk.security.service.SecurityService.Companion.UNIX_RWX__X___
import ebi.ac.uk.security.test.SecurityTestEntities
import ebi.ac.uk.security.test.SecurityTestEntities.Companion.CAPTCHA
import ebi.ac.uk.security.test.SecurityTestEntities.Companion.EMAIL
import ebi.ac.uk.security.test.SecurityTestEntities.Companion.INSTANCE_KEY
import ebi.ac.uk.security.test.SecurityTestEntities.Companion.NAME
import ebi.ac.uk.security.test.SecurityTestEntities.Companion.ORCID
import ebi.ac.uk.security.test.SecurityTestEntities.Companion.PASSWORD
import ebi.ac.uk.security.test.SecurityTestEntities.Companion.PATH
import ebi.ac.uk.security.test.SecurityTestEntities.Companion.activateByEmailRequest
import ebi.ac.uk.security.test.SecurityTestEntities.Companion.passwordDigest
import ebi.ac.uk.security.test.SecurityTestEntities.Companion.registrationRequest
import ebi.ac.uk.security.test.SecurityTestEntities.Companion.resetPasswordRequest
import ebi.ac.uk.security.test.SecurityTestEntities.Companion.retryActivation
import ebi.ac.uk.security.test.SecurityTestEntities.Companion.simpleUser
import ebi.ac.uk.security.util.SecurityUtil
import ebi.ac.uk.util.collections.second
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.biostd.client.cluster.api.ClusterClient
import uk.ac.ebi.biostd.client.cluster.model.DataMoverQueue
import uk.ac.ebi.biostd.client.cluster.model.Job
import uk.ac.ebi.biostd.client.cluster.model.JobSpec
import uk.ac.ebi.events.service.EventsPublisherService
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.PosixFilePermission
import kotlin.test.assertNotNull

private const val ACTIVATION_KEY: String = "code"
private const val SECRET_KEY: String = "secretKey"
private const val FTP_ROOT_PATH: String = "env-test"

@ExtendWith(MockKExtension::class, TemporaryFolderExtension::class)
internal class SecurityServiceTest(
    val temporaryFolder: TemporaryFolder,
    @param:MockK private val userRepository: UserDataRepository,
    @param:MockK private val securityProps: SecurityProperties,
    @param:MockK private val securityUtil: SecurityUtil,
    @param:MockK private val captchaVerifier: CaptchaVerifier,
    @param:MockK private val eventsPublisherService: EventsPublisherService,
    @param:MockK private val clusterClient: ClusterClient,
    @param:MockK private val userPrivilegesService: IUserPrivilegesService,
    @param:MockK private val subFilesPersistenceService: SubmissionFilesPersistenceService,
) {
    private val testInstance: SecurityService =
        SecurityService(
            userRepository,
            securityUtil,
            securityProps,
            ProfileService(
                userFtpRootPath = FTP_ROOT_PATH,
                nfsUserFilesDirPath = temporaryFolder.createDirectory("nfsFile").toPath(),
                userFtpDirPath = temporaryFolder.createDirectory("ftpFiles").toPath(),
                privilegesService = userPrivilegesService,
                subFilesPersistenceService = subFilesPersistenceService,
            ),
            captchaVerifier,
            eventsPublisherService,
            clusterClient,
        )

    @Nested
    inner class Login {
        @Test
        fun `login when user is not found`() {
            every { userRepository.findByLoginOrEmailAndActive(EMAIL, EMAIL, true) } returns null

            assertThrows<LoginException> { testInstance.login(LoginRequest(EMAIL, PASSWORD)) }
        }

        @Test
        fun `login when invalid password`() {
            every { userRepository.findByLoginOrEmailAndActive(EMAIL, EMAIL, true) } returns simpleUser
            every { userRepository.findByLoginOrEmailAndActive(EMAIL, EMAIL, true) } returns simpleUser
            every { securityUtil.checkPassword(passwordDigest, PASSWORD) } returns false

            assertThrows<LoginException> { testInstance.login(LoginRequest(EMAIL, PASSWORD)) }
        }

        @Test
        fun login() {
            val userToken = "token"

            every { userPrivilegesService.allowedCollections(simpleUser.email, ADMIN) } returns emptyList()
            every { userRepository.findByLoginOrEmailAndActive(EMAIL, EMAIL, true) } returns simpleUser
            every { securityUtil.checkPassword(passwordDigest, PASSWORD) } returns true
            every { securityUtil.createToken(simpleUser) } returns userToken

            val (user, token) = testInstance.login(LoginRequest(EMAIL, PASSWORD))

            assertNotNull(user)
            assertThat(token).isEqualTo(userToken)
        }
    }

    @Nested
    inner class Registration {
        @BeforeEach
        fun beforeEach() {
            every { userRepository.existsByEmail(EMAIL) } returns false
            every { userRepository.save(any<DbUser>()) } answers { firstArg() }
            every { securityUtil.getPasswordDigest(PASSWORD) } returns passwordDigest
            every { securityProps.checkCaptcha } returns true
            every { captchaVerifier.verifyCaptcha(CAPTCHA) } returns Unit
        }

        @Test
        fun `register a user when activation is not required NFS mode`(
            @MockK filesProperties: FilesProperties,
        ) = runTest {
            val savedUserSlot = slot<DbUser>()
            val magicFolderRoot = temporaryFolder.createDirectory("users")

            every { securityProps.filesProperties } returns filesProperties
            every { userRepository.save(capture(savedUserSlot)) } answers { savedUserSlot.captured }
            every { filesProperties.magicDirPath } returns magicFolderRoot.absolutePath
            every { filesProperties.defaultMode } returns StorageMode.NFS
            every { securityProps.requireActivation } returns false
            every { securityUtil.newKey() } returns SECRET_KEY
            every { userPrivilegesService.allowedCollections(simpleUser.email, ADMIN) } returns emptyList()

            val securityUser = testInstance.registerUser(registrationRequest)
            val dbUser = savedUserSlot.captured
            assertThat(dbUser.active).isTrue
            assertThat(dbUser.fullName).isEqualTo(NAME)
            assertThat(dbUser.email).isEqualTo(EMAIL)
            assertThat(dbUser.orcid).isEqualTo(ORCID)
            assertThat(dbUser.passwordDigest).isEqualTo(passwordDigest)

            assertThat(dbUser.superuser).isFalse
            assertThat(dbUser.activationKey).isNull()
            assertThat(dbUser.login).isNull()

            assertThat(securityUser.userFolder).isInstanceOf(NfsUserFolder::class.java)
            val userFolder = (securityUser.userFolder as NfsUserFolder).path
            assertFile(userFolder.parent, RWX__X___)
            assertFile(userFolder, RWXRWX___)
            assertSymbolicLink(magicFolderRoot.resolve("b/$EMAIL").toPath(), userFolder)
        }

        @Test
        fun `register a user when activation is not required FTP mode`(
            @MockK job: Job,
            @MockK filesProperties: FilesProperties,
        ) = runTest {
            val savedUserSlot = slot<DbUser>()
            val jobSpecSlots = mutableListOf<JobSpec>()
            val magicFolderRoot = temporaryFolder.createDirectory("users")

            every { securityProps.filesProperties } returns filesProperties
            coEvery { clusterClient.triggerJobSync(capture(jobSpecSlots)) } returns job
            every { userRepository.save(capture(savedUserSlot)) } answers { savedUserSlot.captured }
            every { filesProperties.magicDirPath } returns magicFolderRoot.absolutePath
            every { filesProperties.defaultMode } returns StorageMode.FTP
            every { securityProps.requireActivation } returns false
            every { securityUtil.newKey() } returns SECRET_KEY
            every { userPrivilegesService.allowedCollections(simpleUser.email, ADMIN) } returns emptyList()

            val securityUser = testInstance.registerUser(registrationRequest)
            val dbUser = savedUserSlot.captured
            assertThat(dbUser.active).isTrue
            assertThat(dbUser.fullName).isEqualTo(NAME)
            assertThat(dbUser.email).isEqualTo(EMAIL)
            assertThat(dbUser.orcid).isEqualTo(ORCID)
            assertThat(dbUser.passwordDigest).isEqualTo(passwordDigest)

            assertThat(dbUser.superuser).isFalse
            assertThat(dbUser.activationKey).isNull()
            assertThat(dbUser.login).isNull()

            assertThat(securityUser.userFolder).isInstanceOf(FtpUserFolder::class.java)
            assertThat(jobSpecSlots).hasSize(2)

            val userFolder = (securityUser.userFolder as FtpUserFolder).path
            val parentFolderJobSpec = jobSpecSlots.first()
            assertThat(parentFolderJobSpec.queue).isEqualTo(DataMoverQueue)
            assertThat(parentFolderJobSpec.command)
                .isEqualTo(String.format("mkdir -m %d -p %s", UNIX_RWX__X___, userFolder.parent))

            val userFolderJobSpec = jobSpecSlots.second()
            assertThat(userFolderJobSpec.queue).isEqualTo(DataMoverQueue)
            assertThat(userFolderJobSpec.command)
                .isEqualTo(String.format("mkdir -m %d -p %s", UNIX_RWXRWX___, userFolder))
        }

        private fun assertSymbolicLink(
            link: Path,
            target: Path,
        ) {
            assertThat(link).exists()
            assertThat(Files.readSymbolicLink(link)).isEqualTo(target)
        }

        private fun assertFile(
            path: Path,
            expectedPermission: Set<PosixFilePermission>,
        ) {
            assertThat(path).exists()
            assertThat(Files.getPosixFilePermissions(path)).containsExactlyInAnyOrderElementsOf(expectedPermission)
        }

        @Test
        fun `register a user when activation is required`(
            @MockK filesProperties: FilesProperties,
        ) = runTest {
            val savedUserSlot = slot<DbUser>()
            val activationSlot = slot<SecurityNotification>()
            val activationUrl = "https://dummy-backend.com/active/1234"

            every { securityProps.filesProperties } returns filesProperties
            every { filesProperties.defaultMode } returns StorageMode.NFS
            every { securityProps.requireActivation } returns true
            every { securityUtil.newKey() } returns SECRET_KEY andThen ACTIVATION_KEY
            every { securityUtil.getActivationUrl(INSTANCE_KEY, PATH, ACTIVATION_KEY) } returns activationUrl
            every { eventsPublisherService.securityNotification(capture(activationSlot)) } answers { nothing }
            every { userRepository.save(capture(savedUserSlot)) } answers { savedUserSlot.captured }
            every { userPrivilegesService.allowedCollections(simpleUser.email, ADMIN) } returns emptyList()

            testInstance.registerUser(SecurityTestEntities.preRegisterRequest)

            val user = savedUserSlot.captured
            assertThat(user.active).isFalse
            assertThat(user.activationKey).isEqualTo(ACTIVATION_KEY)

            val notification = activationSlot.captured
            assertThat(notification.email).isEqualTo(user.email)
            assertThat(notification.username).isEqualTo(user.fullName)
            assertThat(notification.activationLink).isEqualTo(activationUrl)
            assertThat(notification.type).isEqualTo(ACTIVATION)
        }

        @Test
        fun `register user when user already exist`() =
            runTest {
                every { userRepository.existsByEmail(EMAIL) } returns true

                val error = assertThrows<UserAlreadyRegister> { testInstance.registerUser(registrationRequest) }
                assertThat(error.message).isEqualTo("There is a user already registered with the email address '$EMAIL'.")
            }
    }

    @Nested
    inner class Activation {
        @Test
        fun `activate when not pending activation`() =
            runTest {
                every { userRepository.findByActivationKeyAndActive(ACTIVATION_KEY, false) } returns null

                assertThrows<UserWithActivationKeyNotFoundException> { testInstance.activate(ACTIVATION_KEY) }
            }

        @Test
        fun `activate when user is found`() =
            runTest {
                every { userPrivilegesService.allowedCollections(simpleUser.email, ADMIN) } returns emptyList()
                every { userRepository.findByActivationKeyAndActive(ACTIVATION_KEY, false) } returns simpleUser
                every { userRepository.save(any<DbUser>()) } answers { firstArg() }
                every { securityProps.filesProperties.magicDirPath } returns temporaryFolder.createDirectory("users").absolutePath

                testInstance.activate(ACTIVATION_KEY)

                assertThat(simpleUser.active).isTrue
                assertThat(simpleUser.activationKey).isNull()
            }
    }

    @Nested
    inner class Retry {
        @Test
        fun `retry pre registration when user not found`() {
            every { userRepository.findByEmailAndActive(EMAIL, false) } returns null

            assertThrows<UserPendingRegistrationException> { testInstance.retryRegistration(retryActivation) }
        }

        @Test
        fun `retry pre registration`() {
            val savedUserSlot = slot<DbUser>()
            val activationSlot = slot<SecurityNotification>()
            val user = simpleUser.apply { active = false }
            val activationUrl = "https://dummy-backend.com/active/1234"

            every { userRepository.findByEmailAndActive(EMAIL, false) } returns user
            every { securityUtil.newKey() } returns ACTIVATION_KEY
            every { securityUtil.getActivationUrl(INSTANCE_KEY, PATH, ACTIVATION_KEY) } returns activationUrl
            every { userRepository.save(capture(savedUserSlot)) } answers { savedUserSlot.captured }
            every { eventsPublisherService.securityNotification(capture(activationSlot)) } answers { nothing }
            every { userPrivilegesService.allowedCollections(simpleUser.email, ADMIN) } returns emptyList()

            testInstance.retryRegistration(retryActivation)

            val dbUser = savedUserSlot.captured
            assertThat(dbUser.active).isFalse
            assertThat(dbUser.activationKey).isEqualTo(ACTIVATION_KEY)
        }
    }

    @Nested
    inner class ChangePassword {
        private val password = "new password"

        @Test
        fun `change password when not activate user found`() =
            runTest {
                every { userRepository.findByActivationKey(ACTIVATION_KEY) } returns null

                assertThrows<UserWithActivationKeyNotFoundException> {
                    testInstance.changePassword(
                        ChangePasswordRequest(
                            ACTIVATION_KEY,
                            password,
                        ),
                    )
                }
            }

        @Test
        fun `change password when active user`() =
            runTest {
                val user = simpleUser.apply { active = true }
                val passwordDigest = ByteArray(0)

                every { userPrivilegesService.allowedCollections(simpleUser.email, ADMIN) } returns emptyList()
                every { userRepository.findByActivationKey(ACTIVATION_KEY) } returns user
                every { securityUtil.getPasswordDigest(password) } returns passwordDigest
                every { userRepository.save(any<DbUser>()) } answers { firstArg() }
                every { securityProps.filesProperties.magicDirPath } returns temporaryFolder.createDirectory("users").absolutePath

                val updated = testInstance.changePassword(ChangePasswordRequest(ACTIVATION_KEY, "new password"))
                assertThat(updated.email).isEqualTo(user.email)
                assertThat(user.activationKey).isNull()
                assertThat(user.passwordDigest).isEqualTo(passwordDigest)
            }

        @Test
        fun `change password when inactive user`() =
            runTest {
                val passwordDigest = ByteArray(0)
                every { userPrivilegesService.allowedCollections(simpleUser.email, ADMIN) } returns emptyList()
                every { userRepository.findByActivationKey(ACTIVATION_KEY) } returns simpleUser
                every { securityUtil.getPasswordDigest(password) } returns passwordDigest
                every { userRepository.save(any<DbUser>()) } answers { firstArg() }
                every { securityProps.filesProperties.magicDirPath } returns temporaryFolder.createDirectory("users").absolutePath

                val updated = testInstance.changePassword(ChangePasswordRequest(ACTIVATION_KEY, "new password"))
                assertThat(updated.email).isEqualTo(simpleUser.email)
                assertThat(simpleUser.activationKey).isNull()
                assertThat(simpleUser.passwordDigest).isEqualTo(passwordDigest)
            }
    }

    @Nested
    inner class ResetPassword {
        @BeforeEach
        fun beforeEach() {
            every { securityProps.checkCaptcha } returns true
            every { captchaVerifier.verifyCaptcha(CAPTCHA) } returns Unit
        }

        @Test
        fun `reset password when user not found`() {
            every { userRepository.findByEmail(EMAIL) } returns null

            assertThrows<UserNotFoundByEmailException> { testInstance.resetPassword(resetPasswordRequest) }
        }

        @Test
        fun resetPassword() {
            val resetSlot = slot<SecurityNotification>()
            val activationUrl = "https://dummy-backend.com/active/1234"

            every { userRepository.findByEmail(EMAIL) } returns simpleUser
            every { securityUtil.newKey() } returns ACTIVATION_KEY
            every { userRepository.save(any<DbUser>()) } answers { firstArg() }
            every { securityUtil.getActivationUrl(INSTANCE_KEY, PATH, ACTIVATION_KEY) } returns activationUrl
            every { eventsPublisherService.securityNotification(capture(resetSlot)) } answers { nothing }

            testInstance.resetPassword(resetPasswordRequest)

            val notification = resetSlot.captured
            assertThat(notification.email).isEqualTo(simpleUser.email)
            assertThat(notification.username).isEqualTo(simpleUser.fullName)
            assertThat(notification.activationLink).isEqualTo(activationUrl)
            assertThat(notification.type).isEqualTo(PASSWORD_RESET)
        }
    }

    @Nested
    inner class ActivateByEmail {
        @AfterEach
        fun afterEach() = clearAllMocks()

        @Test
        fun `activate by email when user not found`() {
            every { userRepository.findByEmailAndActive(EMAIL, false) } returns null

            assertThrows<UserNotFoundByEmailException> { testInstance.activateByEmail(activateByEmailRequest) }
        }

        @Test
        fun `activate by email user without activation key`() {
            every { userRepository.findByEmailAndActive(EMAIL, false) } returns simpleUser

            assertThrows<ActKeyNotFoundException> { testInstance.activateByEmail(activateByEmailRequest) }
        }

        @Test
        fun `activate by email`() {
            val activateByEmailSlot = slot<SecurityNotification>()
            val activationUrl = "https://dummy-backend.com/active/1234"
            val user = simpleUser.apply { activationKey = "activation-key" }

            every { userRepository.findByEmailAndActive(EMAIL, false) } returns user
            every { securityUtil.getActivationUrl(INSTANCE_KEY, PATH, "activation-key") } returns activationUrl
            every { eventsPublisherService.securityNotification(capture(activateByEmailSlot)) } answers { nothing }

            testInstance.activateByEmail(activateByEmailRequest)

            val notification = activateByEmailSlot.captured
            assertThat(notification.email).isEqualTo(user.email)
            assertThat(notification.username).isEqualTo(user.fullName)
            assertThat(notification.activationLink).isEqualTo(activationUrl)
            assertThat(notification.type).isEqualTo(ACTIVATION_BY_EMAIL)
        }
    }

    @Nested
    inner class ActivateAndSetUpPassword {
        @AfterEach
        fun afterEach() = clearAllMocks()

        @Test
        fun `activate with invalid activation key`() =
            runTest {
                val request = ChangePasswordRequest("key", "password")

                every { userRepository.findByActivationKeyAndActive("key", false) } returns null

                assertThrows<UserWithActivationKeyNotFoundException> { testInstance.activateAndSetupPassword(request) }
            }

        @Test
        fun `activate and setup password`() =
            runTest {
                val userSlots = mutableListOf<DbUser>()
                val user = simpleUser.apply { activationKey = "key" }
                val request = ChangePasswordRequest("key", "password")

                every { userPrivilegesService.allowedCollections(simpleUser.email, ADMIN) } returns emptyList()
                every { userRepository.save(capture(userSlots)) } returns user
                every { userRepository.findByActivationKeyAndActive("key", true) } returns user
                every { userRepository.findByActivationKeyAndActive("key", false) } returns user
                every { securityUtil.getPasswordDigest("password") } returns "diggested-password".toByteArray()
                every { securityProps.filesProperties.magicDirPath } returns temporaryFolder.createDirectory("users").absolutePath

                testInstance.activateAndSetupPassword(request)

                val activated = userSlots.first()
                assertThat(activated.activationKey).isNull()
                assertThat(activated.active).isTrue

                val passwordSetup = userSlots.second()
                assertThat(passwordSetup.activationKey).isNull()
                assertThat(passwordSetup.passwordDigest).isEqualTo("diggested-password".toByteArray())
            }
    }
}

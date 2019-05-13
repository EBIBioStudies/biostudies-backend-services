package ebi.ac.uk.security.service

import ac.uk.ebi.biostd.persistence.model.User
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ebi.ac.uk.security.integration.SecurityProperties
import ebi.ac.uk.security.integration.exception.ActKeyNotFoundException
import ebi.ac.uk.security.integration.exception.UserAlreadyRegister
import ebi.ac.uk.security.integration.model.events.UserPreRegister
import ebi.ac.uk.security.integration.model.events.UserRegister
import ebi.ac.uk.security.test.SecurityTestEntities
import ebi.ac.uk.security.util.SecurityUtil
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.reactivex.observers.TestObserver
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.util.Optional

private const val ACTIVATION_CODE: String = "code"
private const val SECRET_KEY: String = "secretKey"
private val PASSWORD_DIGGEST: ByteArray = ByteArray(0)

@ExtendWith(MockKExtension::class)
internal class SecurityServiceTest(
    @MockK private val userRepository: UserDataRepository,
    @MockK private val securityProps: SecurityProperties,
    @MockK private val securityUtil: SecurityUtil
) {

    private val userPreRegister: Subject<UserPreRegister> = PublishSubject.create<UserPreRegister>()
    private val userRegister: Subject<UserRegister> = PublishSubject.create<UserRegister>()

    private val testInstance: SecurityService =
        SecurityService(userRepository, securityUtil, securityProps, userPreRegister, userRegister)

    @Test
    fun login() {
    }

    @Nested
    inner class Registration {

        @BeforeEach
        fun beforeEach() {
            every { userRepository.existsByEmail(SecurityTestEntities.email) } returns false
            every { userRepository.save(any<User>()) } answers { firstArg() }
            every { securityUtil.getPasswordDigest(SecurityTestEntities.password) } returns PASSWORD_DIGGEST
        }

        @Test
        fun `register a user when not activation is not required`() {
            every { securityProps.requireActivation } returns false
            every { securityUtil.newKey() } returns SECRET_KEY

            val subscriber = TestObserver<UserRegister>()
            userRegister.subscribe(subscriber)

            testInstance.registerUser(SecurityTestEntities.simpleRegistrationRequest)

            assertThat(subscriber.values()).hasSize(1)
            assertThat(subscriber.values()).first().satisfies {
                assertThat(it.user.active).isTrue()
                assertThat(it.user.fullName).isEqualTo(SecurityTestEntities.username)
                assertThat(it.user.email).isEqualTo(SecurityTestEntities.email)
                assertThat(it.user.passwordDigest).isEqualTo(PASSWORD_DIGGEST)

                assertThat(it.user.superuser).isFalse()
                assertThat(it.user.activationKey).isNull()
                assertThat(it.user.login).isNull()
            }
        }

        @Test
        fun `register a user when activation is required`() {
            val instanceUrl = "http://dummy-backend.com"
            every { securityProps.requireActivation } returns true
            every { securityUtil.newKey() } returns SECRET_KEY andThen ACTIVATION_CODE
            every { securityUtil.getInstanceUrl(SecurityTestEntities.instanceKey, SecurityTestEntities.path) } returns instanceUrl

            val subscriber = TestObserver<UserPreRegister>()
            userPreRegister.subscribe(subscriber)

            testInstance.registerUser(SecurityTestEntities.preRegisterRequest)

            assertThat(subscriber.values()).hasSize(1)
            assertThat(subscriber.values()).first().satisfies {
                assertThat(it.user.active).isFalse()
                assertThat(it.user.activationKey).isEqualTo(ACTIVATION_CODE)
            }
        }
    }

    @Test
    fun `register user when user already exist`() {
        every { userRepository.existsByEmail(SecurityTestEntities.email) } returns true

        val exception = assertThrows<UserAlreadyRegister> { testInstance.registerUser(SecurityTestEntities.simpleRegistrationRequest) }
        assertThat(exception.message).isEqualTo("There is already a user register with Jhon.Doe@test.com")
    }

    @Test
    fun `activate when not inactive user is found`(@MockK user: User) {
        every { userRepository.findByActivationKeyAndActive(ACTIVATION_CODE, false) } returns Optional.empty()

        assertThrows<ActKeyNotFoundException> { testInstance.activate(ACTIVATION_CODE) }
    }

    @Test
    fun `activate when user is found`() {
        val user = SecurityTestEntities.simpleUser
        every { userRepository.findByActivationKeyAndActive(ACTIVATION_CODE, false) } returns Optional.of(user)
        every { userRepository.save(any<User>()) } answers { firstArg() }

        testInstance.activate(ACTIVATION_CODE)

        assertThat(user.active).isTrue()
        assertThat(user.activationKey).isNull()
    }
}

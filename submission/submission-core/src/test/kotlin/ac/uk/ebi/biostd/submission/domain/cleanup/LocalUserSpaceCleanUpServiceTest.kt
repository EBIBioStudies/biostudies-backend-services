package ac.uk.ebi.biostd.submission.domain.cleanup

import ac.uk.ebi.biostd.common.properties.CleanUpProperties
import ac.uk.ebi.biostd.persistence.common.service.NotificationErrorDataService
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ac.uk.ebi.biostd.submission.domain.cleanup.LocalUserSpaceCleanUpService.Companion.CLEAN_UP_ERROR
import ac.uk.ebi.biostd.submission.domain.cleanup.LocalUserSpaceCleanUpService.Companion.FINAL_WARNING_SUBJECT
import ac.uk.ebi.biostd.submission.domain.cleanup.LocalUserSpaceCleanUpService.Companion.FINAL_WARNING_TEMPLATE
import ac.uk.ebi.biostd.submission.domain.cleanup.LocalUserSpaceCleanUpService.Companion.WARNING_SUBJECT
import ac.uk.ebi.biostd.submission.domain.cleanup.LocalUserSpaceCleanUpService.Companion.WARNING_TEMPLATE
import ebi.ac.uk.extended.events.CleanUpNotification
import ebi.ac.uk.security.integration.components.SecurityQueryService
import ebi.ac.uk.security.integration.model.api.NfsUserFolder
import ebi.ac.uk.security.integration.model.api.SecurityUser
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.events.service.EventsPublisherService
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.io.path.createDirectories

@ExtendWith(MockKExtension::class)
class LocalUserSpaceCleanUpServiceTest(
    @param:MockK private val userRepository: UserDataRepository,
    @param:MockK private val securityQueryService: SecurityQueryService,
    @param:MockK private val eventsPublisherService: EventsPublisherService,
    @param:MockK private val notificationErrorService: NotificationErrorDataService,
) {
    private val cleanUpProperties =
        CleanUpProperties(
            enabled = true,
            firstWarningDays = 30,
            secondWarningDays = 60,
            thirdWarningDays = 90,
            cleanUpPeriodDays = 120,
        )

    private val testInstance =
        LocalUserSpaceCleanUpService(
            userRepository,
            cleanUpProperties,
            securityQueryService,
            eventsPublisherService,
            notificationErrorService,
        )

    @BeforeEach
    fun beforeEach() {
        mockkStatic(LocalDate::class)
    }

    @AfterEach
    fun afterEach() {
        unmockkStatic(LocalDate::class)
        clearAllMocks()
    }

    @Test
    fun `send notifications uses expected subjects and templates for each warning time`() =
        runTest {
            val today = LocalDate.parse("2026-06-16")
            val notifications = mutableListOf<CleanUpNotification>()
            val firstWarningUser = createUser("first@ebi.ac.uk", "First User", today.minusDays(30).atTime(10, 0))
            val secondWarningUser = createUser("second@ebi.ac.uk", "Second User", today.minusDays(60).atTime(11, 0))
            val finalWarningUser = createUser("final@ebi.ac.uk", "Final User", today.minusDays(90).atTime(12, 0))

            every { LocalDate.now() } returns today
            every {
                userRepository.findAllByLastActivityIsBetween(
                    today.minusDays(30).atStartOfDay(),
                    today
                        .minusDays(30)
                        .plusDays(1)
                        .atStartOfDay()
                        .minusSeconds(1),
                )
            } returns listOf(firstWarningUser.email)
            every {
                userRepository.findAllByLastActivityIsBetween(
                    today.minusDays(60).atStartOfDay(),
                    today
                        .minusDays(60)
                        .plusDays(1)
                        .atStartOfDay()
                        .minusSeconds(1),
                )
            } returns listOf(secondWarningUser.email)
            every {
                userRepository.findAllByLastActivityIsBetween(
                    today.minusDays(90).atStartOfDay(),
                    today
                        .minusDays(90)
                        .plusDays(1)
                        .atStartOfDay()
                        .minusSeconds(1),
                )
            } returns listOf(finalWarningUser.email)
            every { securityQueryService.getUser(firstWarningUser.email) } returns firstWarningUser
            every { securityQueryService.getUser(secondWarningUser.email) } returns secondWarningUser
            every { securityQueryService.getUser(finalWarningUser.email) } returns finalWarningUser
            every { eventsPublisherService.cleanupNotification(capture(notifications)) } answers { nothing }

            testInstance.sendNotifications()

            assertThat(notifications).hasSize(3)
            assertThat(notifications.map { it.email }).containsExactly(
                firstWarningUser.email,
                secondWarningUser.email,
                finalWarningUser.email,
            )
            assertThat(notifications.map { it.emailSubject }).containsExactly(
                WARNING_SUBJECT,
                WARNING_SUBJECT,
                FINAL_WARNING_SUBJECT,
            )
            assertThat(notifications.map { it.emailTemplate }).containsExactly(
                WARNING_TEMPLATE,
                WARNING_TEMPLATE,
                FINAL_WARNING_TEMPLATE,
            )
            coVerify(exactly = 0) {
                notificationErrorService.saveNotificationError(any(), any(), any(), any())
            }
        }

    @Test
    fun `send notifications stores notification error when there is an error`() =
        runTest {
            val today = LocalDate.parse("2026-06-16")
            val brokenUser = createUser("broken@ebi.ac.uk", "Broken User", today.minusDays(30).atTime(10, 0), createBrokenFolderPath())
            val expectedError =
                "Error checking user folder for '${brokenUser.email}', secret: ${brokenUser.userFolder.path}"

            every { LocalDate.now() } returns today
            every {
                userRepository.findAllByLastActivityIsBetween(
                    today.minusDays(30).atStartOfDay(),
                    today
                        .minusDays(30)
                        .plusDays(1)
                        .atStartOfDay()
                        .minusSeconds(1),
                )
            } returns listOf(brokenUser.email)
            every {
                userRepository.findAllByLastActivityIsBetween(
                    today.minusDays(60).atStartOfDay(),
                    today
                        .minusDays(60)
                        .plusDays(1)
                        .atStartOfDay()
                        .minusSeconds(1),
                )
            } returns emptyList()
            every {
                userRepository.findAllByLastActivityIsBetween(
                    today.minusDays(90).atStartOfDay(),
                    today
                        .minusDays(90)
                        .plusDays(1)
                        .atStartOfDay()
                        .minusSeconds(1),
                )
            } returns emptyList()
            every { securityQueryService.getUser(brokenUser.email) } returns brokenUser
            every { eventsPublisherService.cleanupNotification(any()) } answers { nothing }
            coEvery { notificationErrorService.saveNotificationError(any(), any(), any(), any()) } returns Unit

            testInstance.sendNotifications()

            coVerify(exactly = 1) {
                notificationErrorService.saveNotificationError(
                    brokenUser.email,
                    expectedError,
                    CLEAN_UP_ERROR,
                    any(),
                )
            }
            verify(exactly = 0) { eventsPublisherService.cleanupNotification(any()) }
        }

    private fun createUser(
        email: String,
        fullName: String,
        lastActivity: LocalDateTime,
        userFolderPath: Path = createNonEmptyUserFolder(email),
    ): SecurityUser =
        SecurityUser(
            id = 1,
            email = email,
            fullName = fullName,
            login = null,
            orcid = null,
            secret = "secret",
            superuser = false,
            lastActivity = lastActivity,
            userFolder =
                NfsUserFolder(
                    relativePath = Path.of(email),
                    path = userFolderPath,
                ),
            groupsFolders = emptyList(),
            permissions = emptySet(),
            adminCollections = emptyList(),
            notificationsEnabled = true,
        )

    private fun createNonEmptyUserFolder(email: String): Path {
        val folder = Files.createTempDirectory("cleanup-$email")
        Files.writeString(folder.resolve("marker.txt"), "not empty")
        return folder
    }

    private fun createBrokenFolderPath(): Path {
        val parent = Files.createTempDirectory("cleanup-broken-parent")
        return parent.resolve("missing-folder").createDirectories().resolve("marker.txt")
    }
}

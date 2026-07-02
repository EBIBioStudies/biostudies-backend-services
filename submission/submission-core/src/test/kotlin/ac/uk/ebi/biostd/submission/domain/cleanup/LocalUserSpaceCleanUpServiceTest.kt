package ac.uk.ebi.biostd.submission.domain.cleanup

import ac.uk.ebi.biostd.common.properties.CleanUpProperties
import ac.uk.ebi.biostd.persistence.common.service.CleanUpLogDataService
import ac.uk.ebi.biostd.persistence.common.service.NotificationLogDataService
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ac.uk.ebi.biostd.submission.domain.cleanup.LocalUserSpaceCleanUpService.Companion.FINAL_WARNING_SUBJECT
import ac.uk.ebi.biostd.submission.domain.cleanup.LocalUserSpaceCleanUpService.Companion.FINAL_WARNING_TEMPLATE
import ac.uk.ebi.biostd.submission.domain.cleanup.LocalUserSpaceCleanUpService.Companion.NOTIFICATION_ERROR
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
import io.mockk.slot
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.biostd.client.cluster.api.ClusterClient
import uk.ac.ebi.biostd.client.cluster.model.DataMoverQueue
import uk.ac.ebi.biostd.client.cluster.model.Job
import uk.ac.ebi.biostd.client.cluster.model.JobSpec
import uk.ac.ebi.events.service.EventsPublisherService
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.io.path.absolutePathString
import kotlin.io.path.createDirectories

@ExtendWith(MockKExtension::class)
class LocalUserSpaceCleanUpServiceTest(
    @param:MockK private val clusterClient: ClusterClient,
    @param:MockK private val userRepository: UserDataRepository,
    @param:MockK private val securityQueryService: SecurityQueryService,
    @param:MockK private val eventsPublisherService: EventsPublisherService,
    @param:MockK private val notificationErrorService: NotificationLogDataService,
    @param:MockK private val cleanUpLogDataService: CleanUpLogDataService,
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
            clusterClient,
            userRepository,
            cleanUpProperties,
            securityQueryService,
            cleanUpLogDataService,
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
            everyWarningUsers(today, 30, firstWarningUser.email)
            everyWarningUsers(today, 60, secondWarningUser.email)
            everyWarningUsers(today, 90, finalWarningUser.email)
            every { securityQueryService.getUser(firstWarningUser.email) } returns firstWarningUser
            every { securityQueryService.getUser(secondWarningUser.email) } returns secondWarningUser
            every { securityQueryService.getUser(finalWarningUser.email) } returns finalWarningUser
            every { eventsPublisherService.cleanupNotification(capture(notifications)) } answers { nothing }

            testInstance.sendNotifications()

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
                notificationErrorService.logNotificationError(any(), any(), any(), any())
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
            everyWarningUsers(today, 30, brokenUser.email)
            everyWarningUsers(today, 60)
            everyWarningUsers(today, 90)
            every { securityQueryService.getUser(brokenUser.email) } returns brokenUser
            every { eventsPublisherService.cleanupNotification(any()) } answers { nothing }
            coEvery { notificationErrorService.logNotificationError(any(), any(), any(), any()) } returns Unit

            testInstance.sendNotifications()

            coVerify(exactly = 1) {
                notificationErrorService.logNotificationError(
                    brokenUser.email,
                    brokenUser.userFolder.path.toString(),
                    NOTIFICATION_ERROR,
                    expectedError,
                )
            }
            verify(exactly = 0) { eventsPublisherService.cleanupNotification(any()) }
        }

    @Test
    fun `clean up dispatches a data mover job for each matching non-empty active user`() =
        runTest {
            val today = LocalDate.parse("2026-06-16")
            val userToClean = createUser("cleanup@ebi.ac.uk", "Cleanup User", today.minusDays(120).atTime(10, 0))
            val emptyUser =
                createUser(
                    "empty@ebi.ac.uk",
                    "Empty User",
                    today.minusDays(120).atTime(11, 0),
                    Files.createTempDirectory("cleanup-empty"),
                )
            val job = Job("12345", "datamover", "/logs/12345")
            val jobSpec = slot<JobSpec>()

            every { LocalDate.now() } returns today
            every {
                userRepository.findAllByLastActivityIsBetweenAndActive(
                    today.minusDays(120).atStartOfDay(),
                    today
                        .minusDays(120)
                        .plusDays(1)
                        .atStartOfDay()
                        .minusSeconds(1),
                )
            } returns listOf(userToClean.email, emptyUser.email)
            every { securityQueryService.getUser(userToClean.email) } returns userToClean
            every { securityQueryService.getUser(emptyUser.email) } returns emptyUser
            coEvery { clusterClient.triggerJobAsync(capture(jobSpec)) } returns Result.success(job)
            coEvery { cleanUpLogDataService.logCleanUp(any(), any(), any(), any()) } returns Unit

            testInstance.cleanUpUserSpaces()

            assertThat(jobSpec.captured.queue).isEqualTo(DataMoverQueue)
            assertThat(jobSpec.captured.command)
                .isEqualTo("find '${userToClean.userFolder.path.absolutePathString()}' -mindepth 1 -delete")
            coVerify(exactly = 1) {
                cleanUpLogDataService.logCleanUp(
                    userToClean.email,
                    job.id,
                    userToClean.lastActivity,
                    userToClean.userFolder.path.absolutePathString(),
                )
            }
            coVerify(exactly = 0) {
                cleanUpLogDataService.logCleanUpError(any(), any(), any(), any())
            }
        }

    @Test
    fun `clean up logs dispatch errors with the user email and path`() =
        runTest {
            val today = LocalDate.parse("2026-06-16")
            val userToClean = createUser("cleanup@ebi.ac.uk", "Cleanup User", today.minusDays(120).atTime(10, 0))
            val failure = IllegalStateException("cluster unavailable")

            every { LocalDate.now() } returns today
            every {
                userRepository.findAllByLastActivityIsBetweenAndActive(
                    today.minusDays(120).atStartOfDay(),
                    today
                        .minusDays(120)
                        .plusDays(1)
                        .atStartOfDay()
                        .minusSeconds(1),
                )
            } returns listOf(userToClean.email)
            every { securityQueryService.getUser(userToClean.email) } returns userToClean
            coEvery { clusterClient.triggerJobAsync(any()) } returns Result.failure(failure)
            coEvery { cleanUpLogDataService.logCleanUpError(any(), any(), any(), any()) } returns Unit

            testInstance.cleanUpUserSpaces()

            coVerify(exactly = 1) {
                cleanUpLogDataService.logCleanUpError(
                    userToClean.email,
                    failure.message.orEmpty(),
                    userToClean.userFolder.path.absolutePathString(),
                )
            }
            coVerify(exactly = 0) {
                cleanUpLogDataService.logCleanUp(any(), any(), any(), any())
            }
        }

    private fun everyWarningUsers(
        today: LocalDate,
        daysAgo: Long,
        vararg emails: String,
    ) {
        every {
            userRepository.findAllByLastActivityIsBetweenAndActive(
                today.minusDays(daysAgo).atStartOfDay(),
                today
                    .minusDays(daysAgo)
                    .plusDays(1)
                    .atStartOfDay()
                    .minusSeconds(1),
            )
        } returns emails.toList()
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

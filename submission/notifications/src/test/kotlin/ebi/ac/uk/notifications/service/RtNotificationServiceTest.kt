package ebi.ac.uk.notifications.service

import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtCollection
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.ExtSubmissionMethod
import ebi.ac.uk.extended.model.ExtTag
import ebi.ac.uk.extended.model.StorageMode
import ebi.ac.uk.notifications.util.TemplateLoader
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.OffsetDateTime
import java.time.ZoneOffset

@ExtendWith(MockKExtension::class)
internal class RtNotificationServiceTest(
    @MockK private val loader: TemplateLoader,
    @MockK private val ticketService: RtTicketService,
) {
    @AfterEach
    fun afterEach() = clearAllMocks()

    private val testInstance: RtNotificationService = RtNotificationService(loader, ticketService)

    @Nested
    inner class NotifySuccessfulSubmission {
        @Nested
        inner class WhenSubmitting {
            @Test
            fun `when no released but release date`() {
                testNotification(
                    submission = testSubmission(
                        releaseTime = OffsetDateTime.of(2019, 9, 21, 10, 30, 34, 15, ZoneOffset.UTC),
                        released = false
                    ),
                    """
                        Dear owner@mail.org,
                        
                        Thank you for submitting your data to BioStudies. Your submission has been assigned the BioStudies accession number S-TEST1.
                        
                        You will be able to see it at ui-url/studies/S-TEST1 in the next 24 hours. The release date of this study is set to 2019-09-21 and it will be publicly available after that. You will be able to see it only by logging in or by accessing it through this link: ui-url/studies/S-TEST1?key=a-secret-key
                        
                        Should you have any further questions, please reply to this message making sure you keep the subject or contact us at biostudies@ebi.ac.uk
                        
                        Best regards,
                        
                        BioStudies Team

                    """.trimIndent()
                )
            }

            @Test
            fun `when no released no release date`() {
                testNotification(
                    submission = testSubmission(releaseTime = null, released = false),
                    """
                        Dear owner@mail.org,
                        
                        Thank you for submitting your data to BioStudies. Your submission has been assigned the BioStudies accession number S-TEST1.
                        
                        You will be able to see it at ui-url/studies/S-TEST1 in the next 24 hours. The release date of this study is not set so it's not publicly available. You will be able to see it only by logging in or by accessing it through this link: ui-url/studies/S-TEST1?key=a-secret-key
                        
                        Should you have any further questions, please reply to this message making sure you keep the subject or contact us at biostudies@ebi.ac.uk
                        
                        Best regards,
                        
                        BioStudies Team

                    """.trimIndent()
                )
            }

            @Test
            fun `when released`() {
                testNotification(
                    submission = testSubmission(released = true),
                    """
                        Dear owner@mail.org,

                        Thank you for submitting your data to BioStudies. Your submission has been assigned the BioStudies accession number S-TEST1.

                        You will be able to see it at ui-url/studies/S-TEST1 in the next 24 hours. 

                        Should you have any further questions, please reply to this message making sure you keep the subject or contact us at biostudies@ebi.ac.uk

                        Best regards,

                        BioStudies Team

                    """.trimIndent()
                )
            }

            @Test
            fun `when submission title`() {
                testNotification(
                    submission = testSubmission(title = "Sub Title", version = 1),
                    """
                        Dear owner@mail.org,

                        Thank you for submitting your data to BioStudies. Your submission "Sub Title" has been assigned the BioStudies accession number S-TEST1.

                        You will be able to see it at ui-url/studies/S-TEST1 in the next 24 hours. The release date of this study is set to 2019-09-21 and it will be publicly available after that. You will be able to see it only by logging in or by accessing it through this link: ui-url/studies/S-TEST1?key=a-secret-key

                        Should you have any further questions, please reply to this message making sure you keep the subject or contact us at biostudies@ebi.ac.uk

                        Best regards,

                        BioStudies Team

                    """.trimIndent()
                )
            }

            @Test
            fun `when section title`() {
                testNotification(
                    submission = testSubmission(title = "Sect Title", version = 1),
                    """
                        Dear owner@mail.org,

                        Thank you for submitting your data to BioStudies. Your submission "Sect Title" has been assigned the BioStudies accession number S-TEST1.

                        You will be able to see it at ui-url/studies/S-TEST1 in the next 24 hours. The release date of this study is set to 2019-09-21 and it will be publicly available after that. You will be able to see it only by logging in or by accessing it through this link: ui-url/studies/S-TEST1?key=a-secret-key

                        Should you have any further questions, please reply to this message making sure you keep the subject or contact us at biostudies@ebi.ac.uk

                        Best regards,

                        BioStudies Team

                    """.trimIndent()
                )
            }

            private fun testNotification(submission: ExtSubmission, content: String) {
                val slot = slot<String>()

                every {
                    loader.loadTemplateOrDefault(submission, SUCCESSFUL_SUBMISSION_TEMPLATE)
                } returns asText("/templates/submission/Default.txt")
                every {
                    ticketService.saveRtTicket(
                        "S-TEST1",
                        "BioStudies Submission - S-TEST1",
                        "owner@mail.org",
                        capture(slot)
                    )
                } returns Unit

                testInstance.notifySuccessfulSubmission(submission, "owner@mail.org", "ui-url", "st-url")

                assertThat(slot.captured).isEqualTo(content)
            }
        }

        @Nested
        inner class WhenReSubmitting {
            @Test
            fun `when released`() {
                testNotification(
                    submission = testSubmission(released = true, version = 2),
                    """
                        Dear owner@mail.org,

                        Your BioStudies submission with accession number S-TEST1 has been updated. You will be able to see the updated version at ui-url/studies/S-TEST1 in the next 24 hours. 

                        Should you have any further questions, please reply to this message making sure you keep the subject or contact us at biostudies@ebi.ac.uk

                        Best regards,

                        BioStudies Team

                    """.trimIndent()
                )
            }

            private fun testNotification(submission: ExtSubmission, content: String) {
                val slot = slot<String>()

                every {
                    loader.loadTemplateOrDefault(submission, SUCCESSFUL_RESUBMISSION_TEMPLATE)
                } returns asText("/templates/resubmission/Default.txt")
                every {
                    ticketService.saveRtTicket(
                        "S-TEST1",
                        "BioStudies Submission - S-TEST1",
                        "owner@mail.org",
                        capture(slot)
                    )
                } returns Unit

                testInstance.notifySuccessfulSubmission(submission, "owner@mail.org", "ui-url", "st-url")

                assertThat(slot.captured).isEqualTo(content)
            }
        }
    }

    @Nested
    inner class NotifySubmissionRelease {
        @Test
        fun `when no title`() {
            testNotification(
                submission = testSubmission(released = true),
                """
                    Dear owner@mail.org,

                    Your submission with accession number S-TEST1 will be made public on 2019-09-21.

                    If you want to keep your submission private after this date or make it public earlier, please log in to the BioStudies Submission Tool at st-url and change the release date.

                    Should you have any further questions, please reply to this message making sure you keep the subject or contact us at biostudies@ebi.ac.uk

                    Best regards,

                    BioStudies Team

                """.trimIndent()
            )
        }

        @Test
        fun `when submission title`() {
            testNotification(
                submission = testSubmission(title = "Sub Title", version = 1),
                """
                Dear owner@mail.org,

                Your submission with accession number S-TEST1 - "Sub Title" will be made public on 2019-09-21.

                If you want to keep your submission private after this date or make it public earlier, please log in to the BioStudies Submission Tool at st-url and change the release date.

                Should you have any further questions, please reply to this message making sure you keep the subject or contact us at biostudies@ebi.ac.uk
 
                Best regards,

                BioStudies Team

                """.trimIndent()
            )
        }

        @Test
        fun `when section title`() {
            testNotification(
                submission = testSubmission(title = "Sect Title", version = 1),
                """
                    Dear owner@mail.org,
                    
                    Your submission with accession number S-TEST1 - "Sect Title" will be made public on 2019-09-21.
                    
                    If you want to keep your submission private after this date or make it public earlier, please log in to the BioStudies Submission Tool at st-url and change the release date.
                    
                    Should you have any further questions, please reply to this message making sure you keep the subject or contact us at biostudies@ebi.ac.uk
                    
                    Best regards,
                    
                    BioStudies Team

                """.trimIndent()
            )
        }

        private fun testNotification(submission: ExtSubmission, content: String) {
            val slot = slot<String>()

            every {
                loader.loadTemplateOrDefault(submission, SUBMISSION_RELEASE_TEMPLATE)
            } returns asText("/templates/release/Default.txt")
            every {
                ticketService.saveRtTicket(
                    "S-TEST1",
                    "BioStudies Submission - S-TEST1",
                    "owner@mail.org",
                    capture(slot)
                )
            } returns Unit

            testInstance.notifySubmissionRelease(submission, "owner@mail.org", "ui-url", "st-url")

            assertThat(slot.captured).isEqualTo(content)
        }
    }

    private fun asText(path: String): String =
        object {}.javaClass.getResource(path)!!.readText()

    private fun testSubmission(
        title: String? = null,
        secTitle: String? = null,
        version: Int = 1,
        released: Boolean = false,
        releaseTime: OffsetDateTime? = OffsetDateTime.of(2019, 9, 21, 10, 30, 34, 15, ZoneOffset.UTC)
    ): ExtSubmission = ExtSubmission(
        accNo = "S-TEST1",
        version = version,
        schemaVersion = "1.0",
        owner = "owner@mail.org",
        submitter = "submitter@mail.org",
        title = title,
        method = ExtSubmissionMethod.PAGE_TAB,
        relPath = "/a/rel/path",
        rootPath = "/a/root/path",
        released = released,
        secretKey = "a-secret-key",
        releaseTime = releaseTime,
        modificationTime = OffsetDateTime.of(2019, 9, 20, 10, 30, 34, 15, ZoneOffset.UTC),
        creationTime = OffsetDateTime.of(2019, 9, 19, 10, 30, 34, 15, ZoneOffset.UTC),
        attributes = listOf(ExtAttribute("AttachTo", "BioImages")),
        tags = listOf(ExtTag("component", "web")),
        collections = listOf(ExtCollection("BioImages")),
        section = ExtSection(
            type = "Study",
            attributes = secTitle?.let { listOf(ExtAttribute(name = "Title", value = it)) } ?: listOf()
        ),
        storageMode = StorageMode.NFS
    )
}

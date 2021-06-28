package ac.uk.ebi.biostd.persistence.repositories.data

import ac.uk.ebi.biostd.persistence.mapping.extended.to.ToExtSubmissionMapper
import ac.uk.ebi.biostd.persistence.repositories.SectionDataRepository
import ac.uk.ebi.biostd.persistence.repositories.SubmissionDataRepository
import ac.uk.ebi.biostd.persistence.repositories.SubmissionRequestDataRepository
import ac.uk.ebi.biostd.persistence.repositories.SubmissionStatsDataRepository
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import java.time.OffsetDateTime
import java.time.ZoneOffset

@ExtendWith(MockKExtension::class)
class SubmissionRepositoryTest(
    @MockK private val submissionRepository: SubmissionDataRepository,
    @MockK private val sectionRepository: SectionDataRepository,
    @MockK private val statsRepository: SubmissionStatsDataRepository,
    @MockK private val requestRepository: SubmissionRequestDataRepository,
    @MockK private val extSerializationService: ExtSerializationService,
    @MockK private var submissionMapper: ToExtSubmissionMapper
) {
    private val testInstance: SubmissionRepository = SubmissionRepository(
        submissionRepository,
        sectionRepository,
        statsRepository,
        requestRepository,
        extSerializationService,
        submissionMapper
    )

    private val someDate = OffsetDateTime.of(2018, 1, 1, 12, 0, 22, 1, ZoneOffset.UTC)
    private val someDate1 = OffsetDateTime.of(2019, 1, 1, 12, 0, 22, 1, ZoneOffset.UTC)

    @Test
    fun `expire submissions`() {
        mockkStatic(OffsetDateTime::class)
        every { OffsetDateTime.now() } returns someDate1
        every { submissionRepository.deleteSubmissions(listOf("abc"), someDate1) } answers { nothing }

        testInstance.expireSubmission("abc")

        verify(exactly = 1) { submissionRepository.deleteSubmissions(listOf("abc"), someDate1) }
        unmockkStatic(OffsetDateTime::class)
    }
}

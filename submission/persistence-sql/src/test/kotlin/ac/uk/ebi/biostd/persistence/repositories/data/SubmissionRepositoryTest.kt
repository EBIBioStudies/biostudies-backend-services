package ac.uk.ebi.biostd.persistence.repositories.data

import ac.uk.ebi.biostd.persistence.common.exception.FileListNotFoundException
import ac.uk.ebi.biostd.persistence.mapping.extended.to.ToExtSubmissionMapper
import ac.uk.ebi.biostd.persistence.repositories.SectionDataRepository
import ac.uk.ebi.biostd.persistence.repositories.SubmissionDataRepository
import ac.uk.ebi.biostd.persistence.repositories.SubmissionRequestDataRepository
import ac.uk.ebi.biostd.persistence.repositories.SubmissionStatsDataRepository
import ac.uk.ebi.biostd.persistence.test.dbFileList
import ac.uk.ebi.biostd.persistence.test.dbInnerFileList
import ac.uk.ebi.biostd.persistence.test.dbInnerSection
import ac.uk.ebi.biostd.persistence.test.dbSection
import ac.uk.ebi.biostd.persistence.test.dbSubmission
import ebi.ac.uk.extended.model.ExtFile
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import java.time.OffsetDateTime
import java.time.ZoneOffset.UTC

@ExtendWith(MockKExtension::class)
class SubmissionRepositoryTest(
    @MockK private val submissionRepository: SubmissionDataRepository,
    @MockK private val sectionRepository: SectionDataRepository,
    @MockK private val statsRepository: SubmissionStatsDataRepository,
    @MockK private val requestRepository: SubmissionRequestDataRepository,
    @MockK private val extSerializationService: ExtSerializationService,
    @MockK private var submissionMapper: ToExtSubmissionMapper
) {
    private val someDate1 = OffsetDateTime.of(2019, 1, 1, 12, 0, 22, 1, UTC)

    private val testInstance: SubmissionRepository = SubmissionRepository(
        submissionRepository,
        sectionRepository,
        statsRepository,
        requestRepository,
        extSerializationService,
        submissionMapper
    )

    @AfterEach
    fun afterEach() = clearAllMocks()

    @BeforeEach
    fun beforeEach() {
        every { sectionRepository.getById(123) } returns dbSection
        every { sectionRepository.getById(456) } returns dbInnerSection
        every { submissionRepository.getByAccNoAndVersionGreaterThan("S-BSST1", 0) } returns dbSubmission
    }

    @Test
    fun `expire submissions`() {
        mockkStatic(OffsetDateTime::class)
        every { OffsetDateTime.now() } returns someDate1
        every { submissionRepository.deleteSubmissions(listOf("abc"), someDate1) } answers { nothing }

        testInstance.expireSubmission("abc")

        verify(exactly = 1) { submissionRepository.deleteSubmissions(listOf("abc"), someDate1) }
        unmockkStatic(OffsetDateTime::class)
    }

    @Test
    fun `get referenced files`(
        @MockK extFile: ExtFile
    ) {
        every { submissionMapper.toExtFileList(dbSubmission, dbFileList) } returns listOf(extFile)

        val files = testInstance.getReferencedFiles("S-BSST1", "file-list")
        assertThat(files).containsExactly(extFile)
        verify(exactly = 1) { submissionMapper.toExtFileList(dbSubmission, dbFileList) }
    }

    @Test
    fun `get referenced files for inner subsection`(
        @MockK extFile: ExtFile
    ) {
        every { submissionMapper.toExtFileList(dbSubmission, dbInnerFileList) } returns listOf(extFile)

        val files = testInstance.getReferencedFiles("S-BSST1", "inner-file-list")
        assertThat(files).containsExactly(extFile)
        verify(exactly = 1) { submissionMapper.toExtFileList(dbSubmission, dbInnerFileList) }
    }

    @Test
    fun `get referenced files for non existing file list`() {
        val exception = assertThrows<FileListNotFoundException> {
            testInstance.getReferencedFiles("S-BSST1", "non-existing")
        }

        assertThat(exception.message).isEqualTo(
            "The file list 'non-existing' could not be found in the submission 'S-BSST1'"
        )
    }
}

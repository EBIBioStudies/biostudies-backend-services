package ac.uk.ebi.biostd.submission.validators

import ac.uk.ebi.biostd.submission.exceptions.InvalidSectionAccNoException
import ac.uk.ebi.biostd.submission.test.createBasicExtendedSubmission
import ebi.ac.uk.model.ExtendedSection
import ebi.ac.uk.model.LibraryFile
import ebi.ac.uk.persistence.PersistenceContext
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class LibraryFileSectionValidatorTest(@MockK private val mockPersistenceContext: PersistenceContext) {
    private val testInstance = LibraryFileSectionValidator()

    @Test
    fun `validate library file section without accNo`() {
        val submission = createBasicExtendedSubmission().apply {
            extendedSection = ExtendedSection("Study").apply {
                libraryFile = LibraryFile("LibraryFile.tsv")
            }
        }

        assertThrows<InvalidSectionAccNoException> { testInstance.validate(submission, mockPersistenceContext) }
    }

    @Test
    fun `validate library valid file section`() {
        val submission = createBasicExtendedSubmission().apply {
            extendedSection = ExtendedSection("Study").apply {
                accNo = "SECT-001"
                libraryFile = LibraryFile("LibraryFile.tsv")
            }
        }

        testInstance.validate(submission, mockPersistenceContext)
    }
}

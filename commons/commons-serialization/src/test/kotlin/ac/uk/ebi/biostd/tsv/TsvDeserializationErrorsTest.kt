package ac.uk.ebi.biostd.tsv

import ac.uk.ebi.biostd.test.submissionWithInvalidInnerSubsection
import ac.uk.ebi.biostd.test.submissionWithInvalidNameAttributeDetail
import ac.uk.ebi.biostd.test.submissionWithInvalidValueAttributeDetail
import ac.uk.ebi.biostd.test.submissionWithTableWithMoreAttributes
import ac.uk.ebi.biostd.test.submissionWithTableWithNoRows
import ac.uk.ebi.biostd.tsv.deserialization.TsvDeserializer
import ac.uk.ebi.biostd.validation.InvalidChunkSizeException
import ac.uk.ebi.biostd.validation.InvalidElementException
import ac.uk.ebi.biostd.validation.MISPLACED_ATTR_NAME
import ac.uk.ebi.biostd.validation.MISPLACED_ATTR_VAL
import ac.uk.ebi.biostd.validation.REQUIRED_ATTR_NAME
import ac.uk.ebi.biostd.validation.REQUIRED_FILE_PATH
import ac.uk.ebi.biostd.validation.REQUIRED_TABLE_ROWS
import ac.uk.ebi.biostd.validation.SerializationException
import ebi.ac.uk.dsl.tsv.Tsv
import ebi.ac.uk.dsl.tsv.line
import ebi.ac.uk.dsl.tsv.tsv
import ebi.ac.uk.model.BioFile
import ebi.ac.uk.model.Link
import ebi.ac.uk.model.Submission
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class TsvDeserializationErrorsTest {
    private val deserializer = TsvDeserializer()

    @Test
    fun `subsection with invalid parent`() {
        assertThrows<SerializationException> {
            deserializer.deserialize(submissionWithInvalidInnerSubsection().toString())
        }
    }

    @Test
    fun `invalid single element`() {
        val tsv = tsv {
            line("Submission", "S-BIAD2")
            line("Title", "A Title")
            line()
        }.toString()

        assertThrows<NotImplementedError> { deserializer.deserializeElement<Submission>(tsv) }
    }

    @Test
    fun `invalid processing class`() {
        val tsv = tsv {
            line("Links", "Attr")
            line("http://alink.org", "Value")
            line()
        }.toString()

        assertThrows<ClassCastException> { deserializer.deserializeElement<BioFile>(tsv) }
    }

    @Test
    fun `empty single element`() {
        assertThrows<InvalidChunkSizeException> { deserializer.deserializeElement<BioFile>("") }
    }

    @Test
    fun `invalid chunk size`() {
        val tsv = tsv {
            line("Links", "Attr")
            line("http://alink.org", "Value")
            line()

            line("Links", "Attr")
            line("http://otherlink.org", "Value")
            line()
        }.toString()

        assertThrows<InvalidChunkSizeException> { deserializer.deserializeElement<Link>(tsv) }
    }

    @Test
    fun `empty file path`() {
        val tsv = tsv {
            line("File", "")
            line("Type", "Empty Path")
            line()
        }.toString()

        val exception = assertThrows<InvalidElementException> { deserializer.deserializeElement<BioFile>(tsv) }
        assertThat(exception.message).isEqualTo("$REQUIRED_FILE_PATH. Element was not created.")
    }

    @Test
    fun `empty attribute name`() {
        val tsv = tsv {
            line("File", "file1.txt")
            line("", "Empty Path")
            line()
        }.toString()

        val exception = assertThrows<InvalidElementException> { deserializer.deserializeElement<BioFile>(tsv) }
        assertThat(exception.message).isEqualTo("$REQUIRED_ATTR_NAME. Element was not created.")
    }

    @Test
    fun `invalid name attribute detail`(): Unit =
        testInvalidElement(submissionWithInvalidNameAttributeDetail(), MISPLACED_ATTR_NAME)

    @Test
    fun `invalid value attribute detail`(): Unit =
        testInvalidElement(submissionWithInvalidValueAttributeDetail(), MISPLACED_ATTR_VAL)

    @Test
    fun `table with no rows`(): Unit =
        testInvalidElement(submissionWithTableWithNoRows(), REQUIRED_TABLE_ROWS)

    @Test
    fun `table with more attributes than expected`(): Unit = testInvalidElement(
        submissionWithTableWithMoreAttributes(), "A row table can't have more attributes than the table header"
    )

    private fun testInvalidElement(submissionTsv: Tsv, expectedMessage: String) {
        val exception = assertThrows<SerializationException> { deserializer.deserialize(submissionTsv.toString()) }
        assertThat(exception.errors.values()).hasSize(1)

        val cause = exception.errors.values().first().cause
        assertThat(cause).isInstanceOf(InvalidElementException::class.java)
        assertThat(cause).hasMessage("$expectedMessage. Element was not created.")
    }
}

package ac.uk.ebi.biostd.xml.deserializer

import ac.uk.ebi.biostd.xml.XmlSerializer
import ac.uk.ebi.biostd.xml.common.createXmlDocument
import ac.uk.ebi.biostd.xml.deserializer.TestDeserializerFactory.Companion.submissionXmlDeserializer
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.Section
import ebi.ac.uk.model.Submission
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.redundent.kotlin.xml.xml

class SubmissionXmlDeserializerTest {
    private val testInstance = XmlSerializer.mapper

    @Test
    fun `deserialize section`() {
        val xmlSubmission =
            xml("submission") {
                attribute("accno", "ABC123")
                "attributes" {
                    "attribute" {
                        "name" { -"attr1" }
                        "value" { -"attr 1 value" }
                    }
                }
                "section" {
                    attribute("accno", "SECT-123")
                    attribute("type", "Study")
                }
            }.toString()


        val submission = testInstance.readValue(xmlSubmission, Submission::class.java)
        assertThat(submission.accNo).isEqualTo("ABC123")
        assertThat(submission.section).isEqualTo(Section("Study", "SECT-123"))
        assertThat(submission.attributes.first()).isEqualTo(Attribute("attr1", "attr 1 value"))
    }
}

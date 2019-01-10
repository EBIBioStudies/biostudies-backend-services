package ac.uk.ebi.biostd.xml.deserializer

import ac.uk.ebi.biostd.xml.common.createXmlDocument
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.Section
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.redundent.kotlin.xml.xml

class SubmissionXmlDeserializerTest {
    private val testInstance = submissionXmlDeserializer()

    @Test
    fun `deserialize section`() {
        val xmlSubmission = createXmlDocument(
            xml("submission") {
                attribute("accNo", "ABC123")
                "attributes" {
                    "attribute" {
                        "name" { -"attr1" }
                        "value" { -"attr 1" }
                    }
                }
                "section" {
                    attribute("accNo", "SECT-123")
                    attribute("type", "Study")
                }
            }.toString())

        val submission = testInstance.deserialize(xmlSubmission)
        assertThat(submission.accNo).isEqualTo("ABC123")
        assertThat(submission.section).isEqualTo(Section("Study", "SECT-123"))
        assertThat(submission.attributes.first()).isEqualTo(Attribute("attr1", "attr 1"))
    }
}

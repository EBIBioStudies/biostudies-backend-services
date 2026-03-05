package ac.uk.ebi.biostd.json.deserialization

import ac.uk.ebi.biostd.json.JsonSerializer
import ac.uk.ebi.biostd.validation.InvalidElementException
import ebi.ac.uk.dsl.json.jsonArray
import ebi.ac.uk.dsl.json.jsonObj
import ebi.ac.uk.model.Submission
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class SubmissionJsonDeserializerTest {
    private val testInstance = JsonSerializer.mapper

    @Test
    fun `deserialize submission without ReleaseDate`() {
        val json =
            jsonObj {
                "accno" to "S-EPMC123"
                "attributes" to
                    jsonArray({
                        "name" to "Title"
                        "value" to "Test Submission"
                    })
            }.toString()

        val exception = assertThrows<InvalidElementException> { testInstance.readValue(json, Submission::class.java) }
        assertThat(exception.message).contains("ReleaseDate is required")
    }

    @Test
    fun `deserialize submission with empty ReleaseDate`() {
        val json =
            jsonObj {
                "accno" to "S-EPMC123"
                "attributes" to
                    jsonArray({
                        "name" to "ReleaseDate"
                        "value" to ""
                    }, {
                        "name" to "Title"
                        "value" to "Test Submission"
                    })
            }.toString()

        val exception = assertThrows<InvalidElementException> { testInstance.readValue(json, Submission::class.java) }
        assertThat(exception.message).contains("ReleaseDate is required")
    }

    @Test
    fun `deserialize submission with ReleaseDate`() {
        val json =
            jsonObj {
                "accno" to "S-EPMC123"
                "attributes" to
                    jsonArray({
                        "name" to "ReleaseDate"
                        "value" to "2023-02-12"
                    }, {
                        "name" to "Title"
                        "value" to "Test Submission"
                    })
            }.toString()

        val submission = testInstance.readValue(json, Submission::class.java)
        assertThat(submission.accNo).isEqualTo("S-EPMC123")
        assertThat(submission.attributes).hasSize(2)
        assertThat(submission.attributes).anySatisfy {
            assertThat(it.name).isEqualTo("ReleaseDate")
            assertThat(it.value).isEqualTo("2023-02-12")
        }
        assertThat(submission.attributes).anySatisfy {
            assertThat(it.name).isEqualTo("Title")
            assertThat(it.value).isEqualTo("Test Submission")
        }
    }
}

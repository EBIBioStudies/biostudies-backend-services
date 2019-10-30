package ac.uk.ebi.biostd.json.serialization

import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import ebi.ac.uk.dsl.attribute
import ebi.ac.uk.dsl.json.jsonArray
import ebi.ac.uk.dsl.json.jsonObj
import ebi.ac.uk.dsl.submission
import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.User
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.skyscreamer.jsonassert.JSONAssert.assertEquals
import org.skyscreamer.jsonassert.JSONCompareMode.LENIENT
import java.time.OffsetDateTime
import java.time.ZoneOffset

@ExtendWith(MockKExtension::class)
internal class SubmissionJsonSerializerTest {
    private val testInstance = createSerializer()
    private val submission = submission("abc123") { attribute("attr-name", "attr-value") }

    @Test
    fun `serialize submission`() {
        val json = testInstance.writeValueAsString(submission)
        val expected = jsonObj {
            "accno" to "abc123"
            "section" to jsonObj { }
            "attributes" to jsonArray(jsonObj { "reference" to false; "name" to "attr-name"; "value" to "attr-value" })
        }
        assertEquals("invalid submission jso", json, expected.toString(), LENIENT)
    }

    @Test
    fun `serialize extended submission`(@MockK user: User) {
        val dateTime = OffsetDateTime.of(2018, 12, 31, 0, 0, 0, 0, ZoneOffset.UTC)
        val extendedSubmission = ExtendedSubmission(submission, user).apply { releaseTime = dateTime }

        val json = testInstance.writeValueAsString(extendedSubmission)
        val expected = jsonObj {
            "accno" to "abc123"
            "section" to jsonObj { }
            "rtime" to "2018-12-31Z"
            "attributes" to jsonArray(jsonObj { "reference" to false; "name" to "attr-name"; "value" to "attr-value" })
        }
        assertEquals("invalid submission jso", json, expected.toString(), LENIENT)
    }

    companion object {

        fun createSerializer(): ObjectMapper {
            val module = SimpleModule()
            module.addSerializer(Submission::class.java, SubmissionJsonSerializer())

            return jacksonObjectMapper().apply {
                registerModule(module)
                setSerializationInclusion(Include.NON_EMPTY)
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            }
        }
    }
}

package ac.uk.ebi.biostd.submission.web.resources

import ac.uk.ebi.biostd.submission.domain.service.JsonSchemaValidationService
import ebi.ac.uk.model.constants.APPLICATION_JSON
import ebi.ac.uk.model.constants.FILE_LIST_NAME
import ebi.ac.uk.model.constants.SUBMISSION_TYPE
import ebi.ac.uk.model.constants.TEXT_PLAIN
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.multipart
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.servlet.function.RequestPredicates.param

@ExtendWith(MockKExtension::class)
public class JsonSchemaValidationResourceTest(
    //@MockK private val jsonSchemaValidationService: JsonSchemaValidationService
) {
    private val jsonSchemaValidationService = JsonSchemaValidationService("src/test/resources/submission-bia-help-guidelines-schema.json")
    private val mvc = MockMvcBuilders
        .standaloneSetup(JsonSchemaValidationResource(jsonSchemaValidationService))
        .build()

    @Test
    fun `validate json`() {
        mvc.post("/jsonschema-validation") {
            header("${SUBMISSION_TYPE}", "${APPLICATION_JSON}")
            content = """{ "toValidate": "test.json" }"""
        }.andExpect {
            status { isOk }
        }.andExpect { jsonPath("\$.valid").value(true) }
    }

    @Test
    fun `validate tsv`() {
        mvc.post("/jsonschema-validation") {
            header("${SUBMISSION_TYPE}", "${TEXT_PLAIN}")
            content = """Submission
                Title   Test submission
                ReleaseDate 2021-11-01
            """.trimIndent()
        }.andExpect {
            status { isOk }
        }.andExpect { jsonPath("\$.valid").value(true) }
    }

    @Test
    fun `validate json with custom schema`() {
        val customSchema = MockMultipartFile(
            "schema", "custom-schema.json", "application/json",
            """{ "test": "schema"}""".toByteArray(charset("UTF8")))
        val toValidate = MockMultipartFile(
            "toValidate", "to-validate.json", "application/json",
            """
                {
                    "type": "submission",
                    "title": "Test Submission"
                }
            """.trimIndent().toByteArray(charset("UTF8"))
        )
        mvc.multipart( "/jsonschema-validation/custom-schema") {
            header("${SUBMISSION_TYPE}", "${ APPLICATION_JSON}")
            file(customSchema)
            file(toValidate)
        }.andExpect {
            status { isOk }
        }.andExpect { jsonPath("\$.valid").value(false) }
    }

    @Test
    fun `validate pagetab with custom schema`() {
        val customSchema = MockMultipartFile(
            "schema", "custom-schema.json", "application/json",
            """{ "test": "schema"}""".toByteArray(charset("UTF8")))
        val toValidate = MockMultipartFile(
            "toValidate", "to-validate.tsv", "text/plain",
            """
                Submission
                Title   Test Submission
            """.trimIndent().toByteArray(charset("UTF8"))
        )
        mvc.multipart( "/jsonschema-validation/custom-schema") {
            header("${SUBMISSION_TYPE}", "${TEXT_PLAIN}")
            file(customSchema)
            file(toValidate)
        }.andExpect {
            status { isOk }
        }.andExpect { jsonPath("\$.valid").value(false) }
    }
}

package ac.uk.ebi.biostd.submission.domain.service

import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class JsonSchemaValidatorServiceTest() {

    private val testInstance = JsonSchemaValidationService("src/test/resources/submission-bia-help-guidelines-schema.json")

    @Test
    fun `validate basic submission`() {
        //testInstance.setSchemaFromPath()
        assertThat(testInstance.getSchema()).isNotNull

        val basic_submission = """
            {
                "type": "submission",
                "section": {
                    "title": "Test submission"
                }
            }
        """.trimIndent()
        val output = testInstance.validateBasic(basic_submission)
        assertThat(output?.valid).isTrue()
    }

    @Test
    fun `validate basic submission with custom schema`() {
        //testInstance.setSchemaFromPath()
        assertThat(testInstance.getSchema()).isNotNull
        // Source: https://github.com/pwall567/json-kotlin-schema
        val customSchema = """
            {
              "title": "Product",
              "type": "object",
              "required": ["id", "name", "price"],
              "properties": {
                "id": {
                  "type": "number",
                  "description": "Product identifier"
                },
                "name": {
                  "type": "string",
                  "description": "Name of the product"
                },
                "price": {
                  "type": "number",
                  "minimum": 0
                },
                "tags": {
                  "type": "array",
                  "items": {
                    "type": "string"
                  }
                }
              }
            }
        """.trimIndent()
        val basicSubmission = """
            {
                    "id": 1,
                    "name": "Test product",
                    "price": 10,
                    "tags": ["tag1", "tag2", "tag3"]
            }
        """.trimIndent()
        val output = testInstance.validateBasic(customSchema, basicSubmission)
        assertThat(output?.valid).isTrue()
    }
}

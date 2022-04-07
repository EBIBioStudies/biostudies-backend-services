package ac.uk.ebi.biostd.submission.domain.service

import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class JsonSchemaValidatorServiceTest() {

    private val testInstance = JsonSchemaValidationService()

    @Test
    fun `validate basic submission`() {
        testInstance.setSchemaFromPath("src/test/resources/submission-bia-help-guidelines-schema.json")
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
}

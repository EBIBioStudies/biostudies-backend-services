package ac.uk.ebi.biostd.submission.web.resources

import ac.uk.ebi.biostd.submission.domain.service.JsonSchemaValidationService
import ebi.ac.uk.model.constants.FILE_LIST_NAME
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.servlet.function.RequestPredicates.param

@ExtendWith(MockKExtension::class)
public class JsonSchemaValidationResourceTest(
    //@MockK private val jsonSchemaValidationService: JsonSchemaValidationService
) {
    private val mvc = MockMvcBuilders
        .standaloneSetup(JsonSchemaValidationResource(jsonSchemaValidationService))
        .build()

    @Test
    fun `validate post`() {
        mvc.post("/jsonschema-validation") {
            content = """{ "toValidate": "test.json" }"""
        }.andExpect {
            status { isOk }
        }
    }


}

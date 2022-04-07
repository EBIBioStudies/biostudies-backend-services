package ac.uk.ebi.biostd.submission.web.resources

import ac.uk.ebi.biostd.submission.domain.service.JsonSchemaValidationService
import ebi.ac.uk.model.constants.APPLICATION_JSON
import net.pwall.json.schema.output.BasicOutput
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/jsonschema-validation")
class JsonSchemaValidationResource(private val jsonSchemaValidationService: JsonSchemaValidationService) {
    @PostMapping(
        //produces = [APPLICATION_JSON_VALUE]
    )
    fun validate(
        @RequestBody toValidate: String
    ) : String {
        val output = jsonSchemaValidationService.validateBasic(toValidate)
        System.out.println("Validation output: ${output}")
        return output.toString()
    }
}

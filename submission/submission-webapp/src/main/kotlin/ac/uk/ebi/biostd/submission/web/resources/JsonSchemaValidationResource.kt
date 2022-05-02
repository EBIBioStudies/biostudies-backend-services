package ac.uk.ebi.biostd.submission.web.resources

import ac.uk.ebi.biostd.integration.SerializationConfig
import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat
import ac.uk.ebi.biostd.submission.domain.service.JsonSchemaValidationService
import com.mysql.cj.x.protobuf.MysqlxDatatypes
import ebi.ac.uk.dsl.tsv.tsv
import ebi.ac.uk.model.constants.APPLICATION_JSON
import ebi.ac.uk.model.constants.SUBMISSION_TYPE
import ebi.ac.uk.model.constants.TEXT_PLAIN
import net.pwall.json.schema.output.BasicOutput
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.File

@RestController
@RequestMapping("/jsonschema-validation")
class JsonSchemaValidationResource {
    private val jsonSchemaValidationService: JsonSchemaValidationService
    private val serializationService = SerializationConfig.serializationService()

    constructor(jsonSchemaValidationService: JsonSchemaValidationService) {
        this.jsonSchemaValidationService = jsonSchemaValidationService
    }

    @PostMapping(
        headers = ["$SUBMISSION_TYPE=$APPLICATION_JSON"],
        produces = [APPLICATION_JSON_VALUE]
    )
    fun validateJson(
        @RequestBody toValidate: String
    ) : BasicOutput {
        return validate(toValidate)
    }

    @PostMapping(
        headers = ["$SUBMISSION_TYPE=$TEXT_PLAIN"],
        produces = [APPLICATION_JSON_VALUE]
    )
    fun validateTsv(
        @RequestBody toValidate: String
    ) : BasicOutput {
        return validate(tsvToJson(toValidate))
    }

    @PostMapping(
        path = ["custom-schema"],
        headers = ["$SUBMISSION_TYPE=$APPLICATION_JSON"],
        produces = [APPLICATION_JSON_VALUE]
    )
    fun validateJsonCustom(
        @RequestParam ("toValidate") toValidate: MultipartFile,
        @RequestParam("schema") schema: MultipartFile
    ) : BasicOutput {
        val schemaContents = String(schema.bytes)
        val toValidateContents = String(toValidate.bytes)
        System.out.println("Using ${schemaContents}")
        return jsonSchemaValidationService.validateBasic(schemaContents, toValidateContents)
        //return validate(toValidate)
    }

    @PostMapping(
        path = ["custom-schema"],
        headers = ["$SUBMISSION_TYPE=$TEXT_PLAIN"],
        produces = [APPLICATION_JSON_VALUE]
    )
    fun validateTsvCustom(
        @RequestParam ("toValidate") toValidate: MultipartFile,
        @RequestParam("schema") schema: MultipartFile
    ) : BasicOutput {
        val schemaContents = String(schema.bytes)
        val toValidateContents = tsvToJson(String(toValidate.bytes))

        return jsonSchemaValidationService.validateBasic(schemaContents, toValidateContents)
        //return validate(toValidate)
    }
    private fun validate(toValidate: String) : BasicOutput {
        val output = jsonSchemaValidationService.validateBasic(toValidate)
        System.out.println("Validation input: ${toValidate}")
        System.out.println("Validation output: ${output}")
        return output
    }

    private fun tsvToJson(tsv: String) : String {
        val submission = serializationService.deserializeSubmission(tsv, SubFormat.TSV)
        return serializationService.serializeSubmission(submission, SubFormat.JSON)
    }
}

/*fun main(args: Array<String>) {
    if (args.size != 1) {
        println("You need to supply path to Page tab file as only argument!")
        return
    }

    val pageTabPath = args[0]
    println("Processing ${pageTabPath}")


    val contents = File(pageTabPath).readText()
    val submission = serializationService.deserializeSubmission(contents, SubFormat.TSV)
    val submissionJson = serializationService.serializeSubmission(submission, SubFormat.JSON_PRETTY)
    println("${submissionJson}")

    // Convert into an object
}
*/

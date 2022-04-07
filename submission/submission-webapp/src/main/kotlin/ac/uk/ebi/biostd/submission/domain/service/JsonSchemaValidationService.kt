package ac.uk.ebi.biostd.submission.domain.service

import net.pwall.json.schema.JSONSchema
import net.pwall.json.schema.output.BasicOutput

class JsonSchemaValidationService {

    private var schema : JSONSchema = JSONSchema.parseFile("src/test/resources/submission-bia-help-guidelines-schema.json")

    public fun getSchema() : JSONSchema? {
        return this.schema
    }
    public fun setSchemaFromPath(schemaPath : String) {
        this.schema = JSONSchema.parseFile(schemaPath)
    }

    public fun validateBasic(json : String) : BasicOutput? {
        return this.schema?.validateBasic(json)
    }
}

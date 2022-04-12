package ac.uk.ebi.biostd.submission.domain.service

import net.pwall.json.schema.JSONSchema
import net.pwall.json.schema.output.BasicOutput
import org.springframework.stereotype.Component

class JsonSchemaValidationService(schemaPath : String) {

    private var schema : JSONSchema = JSONSchema.parseFile(schemaPath)

    public fun getSchema() : JSONSchema? {
        return this.schema
    }
    public fun setSchemaFromPath(schemaPath : String) {
        this.schema = JSONSchema.parseFile(schemaPath)
    }

    public fun validateBasic(json : String) : BasicOutput {

        return this.schema?.validateBasic(json)
    }

    public fun validateBasic(schema: String, json : String) : BasicOutput {
        return JSONSchema.parse(schema)?.validateBasic(json)
    }
}

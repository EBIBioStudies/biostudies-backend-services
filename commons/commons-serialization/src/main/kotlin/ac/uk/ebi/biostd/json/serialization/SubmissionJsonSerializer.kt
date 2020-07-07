package ac.uk.ebi.biostd.json.serialization

import ac.uk.ebi.biostd.json.common.writeJsonArray
import ac.uk.ebi.biostd.json.common.writeJsonObject
import ac.uk.ebi.biostd.json.common.writeJsonString
import ac.uk.ebi.biostd.json.common.writeObj
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.constants.SubFields

internal class SubmissionJsonSerializer : StdSerializer<Submission>(Submission::class.java) {
    override fun serialize(subm: Submission, gen: JsonGenerator, provider: SerializerProvider) {

        gen.writeObj {
            writeJsonString(SubFields.ACC_NO, subm.accNo)
            writeJsonArray(SubFields.ATTRIBUTES, subm.attributes)
            writeJsonObject(SubFields.SECTION, subm.section)
        }
    }
}

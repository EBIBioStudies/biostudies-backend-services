package ac.uk.ebi.biostd.json.serialization

import ac.uk.ebi.biostd.json.common.writeJsonArray
import ac.uk.ebi.biostd.json.common.writeJsonNumber
import ac.uk.ebi.biostd.json.common.writeJsonObject
import ac.uk.ebi.biostd.json.common.writeJsonString
import ac.uk.ebi.biostd.json.common.writeObj
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.constants.SubFields
import ebi.ac.uk.model.extensions.creationTime
import ebi.ac.uk.model.extensions.modificationTime
import ebi.ac.uk.model.extensions.releaseTime
import ebi.ac.uk.model.extensions.secretKey

internal class SubmissionJsonSerializer : StdSerializer<Submission>(Submission::class.java) {
    override fun serialize(subm: Submission, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeObj {
            writeJsonString(SubFields.ACC_NO, subm.accNo)
            writeJsonArray(SubFields.ATTRIBUTES, subm.attributes)
            writeJsonObject(SubFields.SECTION, subm.section)
            addExtendedFields(subm, gen)
        }
    }

    private fun JsonGenerator.addExtendedFields(subm: Submission, gen: JsonGenerator) {
        writeJsonArray(SubFields.ACCESS_TAGS, subm.accessTags, gen::writeString)
        writeJsonNumber(SubFields.RELEASE_TIME, subm.releaseTime?.toEpochSecond())
        writeJsonNumber(SubFields.CREATION_TIME, subm.creationTime?.toEpochSecond())
        writeJsonNumber(SubFields.MODIFICATION_TIME, subm.modificationTime?.toEpochSecond())
        writeJsonString(SubFields.SECRET, subm.secretKey)
    }
}

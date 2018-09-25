package ac.uk.ebi.biostd.serialization.json

import ac.uk.ebi.biostd.serialization.json.extensions.writeJsonArray
import ac.uk.ebi.biostd.serialization.json.extensions.writeJsonNumber
import ac.uk.ebi.biostd.serialization.json.extensions.writeJsonObject
import ac.uk.ebi.biostd.serialization.json.extensions.writeJsonString
import ac.uk.ebi.biostd.serialization.json.extensions.writeObj
import ac.uk.ebi.biostd.submission.SubFields
import ac.uk.ebi.biostd.submission.Submission
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import ebi.ac.uk.util.collections.listFrom

class SubmissionJsonSerializer : StdSerializer<Submission>(Submission::class.java) {

    override fun serialize(subm: Submission, gen: JsonGenerator, provider: SerializerProvider) {
        val isInternalView = provider.activeView == InternalSubmission::class.java
        val accessTags = getAccessTags(subm, isInternalView)

        gen.writeObj {
            writeJsonString(SubFields.ACC_NO, subm.accNo)
            writeJsonArray(SubFields.ATTRIBUTES, subm.allAttributes())
            writeJsonArray(SubFields.ACCESS_TAGS, accessTags, gen::writeString)
            writeJsonObject(SubFields.SECTION, subm.section)

            if (isInternalView) {
                writeJsonNumber(SubFields.RELEASE_TIME, subm.rtime)
                writeJsonNumber(SubFields.CREATION_TIME, subm.ctime)
                writeJsonNumber(SubFields.MODIFICATION_TIME, subm.mtime)
                writeJsonString(SubFields.SECRET, subm.secretKey)
            }
        }
    }

    private fun getAccessTags(subm: Submission, isInternalView: Boolean): List<String> {
        return if (isInternalView) listFrom(subm.accessTags, subm.user.email, subm.user.id) else subm.accessTags
    }
}

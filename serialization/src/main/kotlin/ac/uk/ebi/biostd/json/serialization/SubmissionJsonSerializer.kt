package ac.uk.ebi.biostd.json.serialization

import ac.uk.ebi.biostd.json.common.InternalSubmission
import ac.uk.ebi.biostd.json.common.writeJsonArray
import ac.uk.ebi.biostd.json.common.writeJsonObject
import ac.uk.ebi.biostd.json.common.writeJsonString
import ac.uk.ebi.biostd.json.common.writeObj
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import ebi.ac.uk.model.SubFields
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.accNo
import ebi.ac.uk.util.collections.listFrom

class SubmissionJsonSerializer : StdSerializer<Submission>(Submission::class.java) {

    override fun serialize(subm: Submission, gen: JsonGenerator, provider: SerializerProvider) {
        val isInternalView = provider.activeView == InternalSubmission::class.java
        val accessTags = getAccessTags(subm, isInternalView)

        gen.writeObj {
            writeJsonString(SubFields.ACC_NO, subm.accNo)
            writeJsonArray(SubFields.ATTRIBUTES, subm.attributes)
            writeJsonArray(SubFields.ACCESS_TAGS, accessTags, gen::writeString)
            writeJsonObject(SubFields.SECTION, subm.rootSection)

            /*   if (isInternalView) {
                   writeJsonNumber(SubFields.RELEASE_TIME, subm.rtime)
                   writeJsonNumber(SubFields.CREATION_TIME, subm.ctime)
                   writeJsonNumber(SubFields.MODIFICATION_TIME, subm.mtime)
                   writeJsonString(SubFields.SECRET, subm.secretKey)
               }*/
        }
    }

    private fun getAccessTags(subm: Submission, isInternalView: Boolean): List<String> {
        return listFrom(subm.accessTags)
        //return if (isInternalView) listFrom(subm.accessTags, subm.user.email, subm.user.id) else subm.accessTags
    }
}

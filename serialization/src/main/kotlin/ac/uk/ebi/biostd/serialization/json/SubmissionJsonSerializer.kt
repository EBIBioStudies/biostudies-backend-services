package ac.uk.ebi.biostd.serialization.json

import ac.uk.ebi.biostd.extensions.writeArrayFieldIfNotEmpty
import ac.uk.ebi.biostd.extensions.writeNumberFieldIfNotEmpty
import ac.uk.ebi.biostd.extensions.writeObj
import ac.uk.ebi.biostd.extensions.writeStringFieldIfNotEmpty
import ac.uk.ebi.biostd.submission.Submission
import ac.uk.ebi.biostd.submission.User
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer

class SubmissionJsonSerializer : StdSerializer<Submission>(Submission::class.java) {

    override fun serialize(subm: Submission, gen: JsonGenerator, provider: SerializerProvider) {
        val isInternalView = provider.activeView == Views.Internal::class.java
        val accessTags = subm.accessTags.toMutableList()

        if (isInternalView && subm.user !== User.EMPTY_USER) {
            accessTags.addAll(listOf(subm.user.email, subm.user.id))
        }

        gen.writeObj {
            writeStringFieldIfNotEmpty("accNo", subm.accNo)
            writeStringFieldIfNotEmpty("title", subm.title)
            writeNumberFieldIfNotEmpty("rtime", subm.rtime)
            writeStringFieldIfNotEmpty("rootPath", subm.rootPath)
            writeArrayFieldIfNotEmpty("attributes", subm.attributes, gen::writeObject)
            writeArrayFieldIfNotEmpty("accessTags", accessTags, gen::writeString)
            provider.defaultSerializeField("section", subm.section, gen)

            if (isInternalView) {

                writeNumberFieldIfNotEmpty("ctime", subm.ctime)
                writeNumberFieldIfNotEmpty("mtime", subm.mtime)
                writeStringFieldIfNotEmpty("relPath", subm.relPath)
                writeStringFieldIfNotEmpty("secretKey", subm.secretKey)
            }
        }
    }
}

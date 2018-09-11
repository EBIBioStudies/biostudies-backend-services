package ac.uk.ebi.biostd.serialization.json

import ac.uk.ebi.biostd.submission.Submission
import ac.uk.ebi.biostd.submission.User
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer


class SubmissionJsonSerializer : StdSerializer<Submission>(Submission::class.java) {

    override fun serialize(subm: Submission, gen: JsonGenerator, provider: SerializerProvider) {
        val view:Class<*>? = provider.activeView

        gen.writeStartObject()

        provider.defaultSerializeField("accNo", subm.accNo, gen)
        provider.defaultSerializeField("rootPath", null, gen)//subm.rootPath, gen)
        provider.defaultSerializeField("attributes", subm.attributes, gen)

        val accessTags = subm.accessTags.toMutableList()

        if (view == Views.Internal::class.java) {
            provider.defaultSerializeField("cTime", subm.cTime, gen)
            provider.defaultSerializeField("mTime", subm.mTime, gen)
            provider.defaultSerializeField("relPath", subm.relPath, gen)
            provider.defaultSerializeField("secretKey", subm.secretKey, gen)

            if (subm.user !== User.EMPTY_USER) {
                accessTags.addAll(listOf(subm.user.email, subm.user.id))
            }
        }

        provider.defaultSerializeField("accessTags", accessTags, gen)
        provider.defaultSerializeField("section", subm.section, gen)
        gen.writeEndObject()
    }
}


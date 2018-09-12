package ac.uk.ebi.biostd.serialization.json

import ac.uk.ebi.biostd.submission.Submission
import ac.uk.ebi.biostd.submission.User
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.BeanDescription
import com.fasterxml.jackson.databind.SerializationConfig
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier
import com.fasterxml.jackson.databind.ser.std.StdSerializer


class SubmissionJsonSerializer : StdSerializer<Submission>(Submission::class.java) {

    override fun serialize(subm: Submission, gen: JsonGenerator, provider: SerializerProvider) {

        val view: Class<*>? = provider.activeView

        gen.writeStartObject()


        gen.writeStringField("accNo", subm.accNo)
        //provider.defaultSerializeField("accNo", subm.accNo, gen)
        gen.writeStringField("rootPath", null)//subm.rootPath, gen)

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

class BeanSerializerModifier : BeanSerializerModifier() {
    override fun changeProperties(config: SerializationConfig, beanDesc: BeanDescription, beanProperties: MutableList<BeanPropertyWriter>): MutableList<BeanPropertyWriter> {
        val beanClass = beanDesc.beanClass
        if (Submission::class.java == beanClass) {
            return SubmissionBeanSerializerModifier.changeProperties(config, beanProperties)
        }
        return super.changeProperties(config, beanDesc, beanProperties)
    }
}

object SubmissionBeanSerializerModifier {
    private val defaultProperties = setOf("accNo", "rTime", "rootPath", "attributes", "accessTags", "section")

    private val internalProperties = setOf("cTime", "mTime", "relPath", "secretKey")

    private val defaultPropertyModifier = { p: BeanPropertyWriter -> p }

    private val accessTagPropertyModifier = { p: BeanPropertyWriter ->
        if (p.name == "accessTags") AccessTagPropertyWriter(p) else p
    }

    private fun propertyModifierIf(isInternalView: Boolean): (BeanPropertyWriter) -> BeanPropertyWriter =
            if (isInternalView) accessTagPropertyModifier else defaultPropertyModifier

    private fun propertyNamesIf(isInternalView: Boolean): Set<String> =
            if (isInternalView) defaultProperties + internalProperties else defaultProperties


    fun changeProperties(config: SerializationConfig, beanProperties: MutableList<BeanPropertyWriter>): MutableList<BeanPropertyWriter> {
        val isInternalView = config.activeView == Views.Internal::class.java
        val propertyNames = propertyNamesIf(isInternalView)
        val modifier = propertyModifierIf(isInternalView)

        return beanProperties.filter { propertyNames.contains(it.name) }
                .map { modifier(it) }
                .toMutableList()
    }
}

class AccessTagPropertyWriter(internal var _writer: BeanPropertyWriter) : BeanPropertyWriter(_writer) {
    override fun serializeAsField(bean: Any, gen: JsonGenerator, provider: SerializerProvider) {
        val subm = bean as Submission
        val accessTags = subm.accessTags.toMutableList()

        if (subm.user !== User.EMPTY_USER) {
            accessTags.addAll(listOf(subm.user.email, subm.user.id))
        }
        gen.writeObjectField("accessTags", accessTags)
    }
}


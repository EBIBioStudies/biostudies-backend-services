package ac.uk.ebi.biostd.serialization.json

import ac.uk.ebi.biostd.submission.Submission
import ac.uk.ebi.biostd.submission.User
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter
import com.fasterxml.jackson.databind.ser.PropertyWriter
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter

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

class SubmissionPropertyFilter : SimpleBeanPropertyFilter() {

    private fun propertyNamesIf(isInternalView: Boolean): Set<String> =
            if (isInternalView) INTERNAL_PROPERTIES else DEFAULT_PROPERTIES

    private val accessTagPropertyModifier = { p: BeanPropertyWriter ->
        if (p.name == "accessTags") AccessTagPropertyWriter(p) else p
    }

    private fun propertyModifierIf(isInternalView: Boolean): (BeanPropertyWriter) -> BeanPropertyWriter =
            if (isInternalView) accessTagPropertyModifier else DEFAULT_PROPERTY_MODIFIER

    override fun serializeAsField(pojo: Any, jgen: JsonGenerator, provider: SerializerProvider, writer: PropertyWriter) {
        val isInternalView = provider.activeView == Views.Internal::class.java
        val propertyNames = propertyNamesIf(isInternalView)
        val propertyModifier = propertyModifierIf(isInternalView)

        if (propertyNames.contains(writer.name)) {
            propertyModifier(writer as BeanPropertyWriter).serializeAsField(pojo, jgen, provider)
        }
        writer.serializeAsOmittedField(pojo, jgen, provider)
    }

    companion object {

        private val DEFAULT_PROPERTIES = setOf("accNo", "title", "rtime", "rootPath", "attributes", "accessTags", "section")

        private val INTERNAL_PROPERTIES = DEFAULT_PROPERTIES + setOf("ctime", "mtime", "relPath", "secretKey")

        private val DEFAULT_PROPERTY_MODIFIER = { p: BeanPropertyWriter -> p }
    }
}

package ebi.ac.uk.extended.serialization

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.cfg.MapperConfig
import com.fasterxml.jackson.databind.introspect.AnnotatedClass
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition
import com.fasterxml.jackson.databind.ser.VirtualBeanPropertyWriter
import com.fasterxml.jackson.databind.util.Annotations
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileTable
import ebi.ac.uk.extended.model.ExtLink
import ebi.ac.uk.extended.model.ExtLinkTable
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSectionTable
import ebi.ac.uk.extended.serialization.ExtSerializationConstants.FILE
import ebi.ac.uk.extended.serialization.ExtSerializationConstants.FILES_TABLE
import ebi.ac.uk.extended.serialization.ExtSerializationConstants.LINK
import ebi.ac.uk.extended.serialization.ExtSerializationConstants.LINKS_TABLE
import ebi.ac.uk.extended.serialization.ExtSerializationConstants.SECTION
import ebi.ac.uk.extended.serialization.ExtSerializationConstants.SECTIONS_TABLE

class ExtElementTypeWriter : VirtualBeanPropertyWriter {
    constructor() : super()

    override fun value(bean: Any, gen: JsonGenerator, prov: SerializerProvider): String = when(bean) {
        is ExtLink -> LINK
        is ExtFile -> FILE
        is ExtSection -> SECTION
        is ExtLinkTable -> LINKS_TABLE
        is ExtFileTable -> FILES_TABLE
        is ExtSectionTable -> SECTIONS_TABLE
        else -> ""
    }

    override fun withConfig(
        config: MapperConfig<*>,
        declaringClass: AnnotatedClass,
        propDef: BeanPropertyDefinition,
        type: JavaType
    ): VirtualBeanPropertyWriter = ExtElementTypeWriter(propDef, declaringClass.annotations, type)

    private constructor(
        propDef: BeanPropertyDefinition,
        annotations: Annotations,
        type: JavaType
    ) : super(propDef, annotations, type)
}

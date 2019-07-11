package ac.uk.ebi.biostd.common.deserialization.stream

import ac.uk.ebi.biostd.common.NAME
import ac.uk.ebi.biostd.common.VALUE
import ebi.ac.uk.model.Attribute

internal class AttributeStreamDeserializerBuilder : StreamDeserializerBuilder<Attribute>() {
    override fun build() = Attribute(fields[NAME]!!, fields[VALUE]!!)
}

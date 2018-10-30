package ac.uk.ebi.biostd.mapping

import ac.uk.ebi.biostd.integration.AttributeDb
import ac.uk.ebi.biostd.integration.FileDb
import ac.uk.ebi.biostd.integration.LinkDb
import ebi.ac.uk.base.orFalse
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.File
import ebi.ac.uk.model.FileFields
import ebi.ac.uk.model.Link

class AttributesMapper {

    fun toLink(link: LinkDb) = Link(link.url, toAttributes(link.attributes))

    fun toFile(file: FileDb) = File(file.name, toAttributes(file.attributes) + Attribute(FileFields.SIZE, file.size))

    fun toAttributes(attrs: Set<AttributeDb>) = attrs.mapTo(mutableListOf()) { toAttribute(it) }

    private fun toAttribute(attrDb: AttributeDb) = Attribute(attrDb.name, attrDb.value, attrDb.reference.orFalse())
}

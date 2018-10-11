package ac.uk.ebi.biostd.mapping

import ac.uk.ebi.biostd.submission.Attribute
import ac.uk.ebi.biostd.submission.File
import ac.uk.ebi.biostd.submission.Link
import ac.uk.ebi.biostd.submission.SimpleAttribute
import ebi.ac.uk.base.orFalse
import ebi.ac.uk.model.IAttribute
import ebi.ac.uk.model.IFile
import ebi.ac.uk.model.ILink
import ebi.ac.uk.model.ISimpleAttribute

class AttributesMapper {

    fun toLink(link: ILink): Link {
        return Link().apply {
            url = link.url
            attributes = toAttributes(link.attributes)
        }
    }

    fun toFile(file: IFile): File {
        return File().apply {
            name = file.name
            attributes = toAttributes(file.attributes)
            size = file.size
        }
    }

    fun toAttributes(attrs: Set<IAttribute>) = attrs.mapTo(mutableListOf()) { toAttribute(it) }

    private fun toAttribute(attrDb: IAttribute) =
            attrDb.run { Attribute(name, value, reference.orFalse(), toSimpleAttributes(attrDb.valueAttributes)) }

    private fun toSimpleAttributes(valueAttributes: MutableList<ISimpleAttribute>) =
            valueAttributes.mapTo(mutableListOf()) { SimpleAttribute(it.name, it.value) }
}

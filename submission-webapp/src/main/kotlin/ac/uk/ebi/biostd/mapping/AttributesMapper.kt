package ac.uk.ebi.biostd.mapping

import ac.uk.ebi.biostd.config.AttributeDb
import ac.uk.ebi.biostd.config.FileDb
import ac.uk.ebi.biostd.config.LinkDb
import ac.uk.ebi.biostd.extensions.second
import ac.uk.ebi.biostd.submission.Attribute
import ac.uk.ebi.biostd.submission.File
import ac.uk.ebi.biostd.submission.Link
import ac.uk.ebi.biostd.submission.Term
import ebi.ac.uk.base.orFalse

class AttributesMapper {

    fun toLink(linkDb: LinkDb): Link {
        return Link().apply { url = linkDb.url; attributes = toAttributes(linkDb.attributes) }
    }

    fun toFile(fileDb: FileDb): File {
        return File().apply { name = fileDb.name; attributes = toAttributes(fileDb.attributes) }
    }

    fun toAttributes(attrs: Set<AttributeDb>) = attrs.map { toAttribute(it) }.toMutableList()

    private fun toAttribute(attrDb: AttributeDb): Attribute {
        return attrDb.run { Attribute(name, value, reference.orFalse(), getTerms(valueQualifier).orEmpty()) }
    }

    private fun getTerms(valueQualifier: String?): List<Term>? {
        return valueQualifier?.split(";")?.map { it.split("=") }?.map { Term(it.first(), it.second()) }
    }
}

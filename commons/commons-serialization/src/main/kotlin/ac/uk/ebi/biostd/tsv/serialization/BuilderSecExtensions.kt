package ac.uk.ebi.biostd.tsv.serialization

import ebi.ac.uk.base.isNotBlank
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.BioFile
import ebi.ac.uk.model.Link

internal const val LINK_KEY = "Link"
internal const val FILE_KEY = "File"

internal fun TsvBuilder.addSecDescriptor(
    type: String,
    accNo: String?,
    parentAccNo: String?,
) {
    append(type)

    when {
        accNo.isNotBlank() && parentAccNo.isNotBlank() -> append("\t$accNo\t$parentAccNo")
        accNo.isNotBlank() && parentAccNo.isNullOrBlank() -> append("\t$accNo")
        accNo.isNullOrBlank() && parentAccNo.isNotBlank() -> append("\t\t$parentAccNo")
    }

    append("\n")
}

internal fun TsvBuilder.addSecLink(link: Link) = with(LINK_KEY, link.url)

internal fun TsvBuilder.addSecFile(file: BioFile) = with(FILE_KEY, file.path)

internal fun TsvBuilder.addAttributes(attributes: List<Attribute>) = attributes.forEach { with(it.name, it.value) }

internal fun TsvBuilder.addAttr(attr: Attribute) {
    with(if (attr.reference) "<${attr.name}>" else attr.name, attr.value)
    attr.nameAttrs.forEach { with("(${it.name})", it.value) }
    attr.valueAttrs.forEach { with("[${it.name}]", it.value) }
}

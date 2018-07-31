package ac.uk.ebi.biostd.serialization.tsv

import ac.uk.ebi.biostd.submission.Attribute
import ac.uk.ebi.biostd.submission.File
import ac.uk.ebi.biostd.submission.Link

internal const val LINK_KEY = "Link"
internal const val FILE_KEY = "File"

fun TsvBuilder.addSecDescriptor(type: String, accNo: String) {
    append("$type\t$accNo\n")
}

fun TsvBuilder.addSecLink(link: Link) {
    with(LINK_KEY, link.url)
}

fun TsvBuilder.addSecFile(file: File) {
    with(FILE_KEY, file.name)
}

fun TsvBuilder.addAttributes(attributes: List<Attribute>) {
    attributes.forEach { with(it.name, it.value) }
}

fun TsvBuilder.addSecAttr(attr: Attribute) {
    with(attr.name, attr.value)
    attr.terms.forEach { with("[${it.first}]", it.second) }
}

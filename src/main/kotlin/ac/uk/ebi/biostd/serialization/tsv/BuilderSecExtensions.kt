package ac.uk.ebi.biostd.serialization.tsv

import ac.uk.ebi.biostd.submission.Attribute
import ac.uk.ebi.biostd.submission.Link

fun TsvBuilder.addSecDescriptor(type: String, accNo: String) {
    append("$type\t$accNo\n")
}

fun TsvBuilder.addSecLink(link: Link) {
    with(LINK_KEY, link.url)
}

fun TsvBuilder.addSecLinkAttributes(attributes: List<Attribute>) {
    attributes.forEach { with(it.name, it.value) }
}

fun TsvBuilder.addSecAttr(attr: Attribute) {
    with(attr.name, attr.value)
    attr.terms.forEach { with("[${it.first}]", it.second) }
}

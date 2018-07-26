package ac.uk.ebi.biostd.serialization.tsv

import ac.uk.ebi.biostd.extensions.second
import ac.uk.ebi.biostd.submission.Attribute
import ac.uk.ebi.biostd.submission.Link

fun TsvBuilder.addSecType(type: String) {
    append("$type\n")
}

fun TsvBuilder.addSecLink(link: Link) {
    with(linkKey, link.url)
}

fun TsvBuilder.addSecLinkAttributes(attributes: List<Attribute>) {
    attributes.forEach { with(it.name, it.value) }
}

fun TsvBuilder.addSecAttr(attr: Attribute) {
    with(attr.name, attr.value)
    extractsValues(attr.qualifierVal).forEach { with("[${it.first}]", it.second) }
}

private fun extractsValues(value: String?): List<Pair<String, String>> {
    return value?.split(attrValSeparator).orEmpty()
            .map { it.split("=") }
            .map { Pair(it.first(), it.second()) }
}
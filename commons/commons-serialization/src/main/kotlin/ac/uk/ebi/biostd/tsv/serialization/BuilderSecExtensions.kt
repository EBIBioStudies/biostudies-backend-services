package ac.uk.ebi.biostd.tsv.serialization

import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.File
import ebi.ac.uk.model.LibraryFile
import ebi.ac.uk.model.Link

internal const val LINK_KEY = "Link"
internal const val FILE_KEY = "File"
internal const val LIB_FILE_KEY = "LibraryFile"

fun TsvBuilder.addSecDescriptor(type: String, accNo: String?) {
    append(type)
    accNo?.let { append("\t$accNo") }
    append("\n")
}

fun TsvBuilder.addSecLink(link: Link) = with(LINK_KEY, link.url)

fun TsvBuilder.addSecFile(file: File) = with(FILE_KEY, file.path)

fun TsvBuilder.addLibFile(libFile: LibraryFile) = with(LIB_FILE_KEY, libFile.name)

fun TsvBuilder.addAttributes(attributes: List<Attribute>) = attributes.forEach { with(it.name, it.value) }

fun TsvBuilder.addAttr(attr: Attribute) {
    with(if (attr.reference) "<${attr.name}>" else attr.name, attr.value)
    attr.nameAttrs.forEach { with("(${it.name})", it.value) }
    attr.valueAttrs.forEach { with("[${it.name}]", it.value) }
}

@file:Suppress("TooManyFunctions")

package ac.uk.ebi.biostd.submission

import arrow.core.Either
import arrow.core.Left
import arrow.core.Right
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.AttributeDetail
import ebi.ac.uk.model.File
import ebi.ac.uk.model.Link
import ebi.ac.uk.model.LinksTable
import ebi.ac.uk.model.Section
import ebi.ac.uk.model.SectionsTable
import ebi.ac.uk.model.Submission

fun submission(block: Submission.() -> Unit): Submission = Submission().apply(block)

fun section(block: Section.() -> Unit): Section = Section().apply(block)

fun Submission.section(block: Section.() -> Unit): Section {
    val section = Section().apply(block)
    this.rootSection = section
    return section
}

fun Submission.attribute(name: String, value: String): Attribute {
    val attribute = Attribute(name = name, value = value, valueAttrs = mutableListOf())
    this.attributes.add(attribute)
    return attribute
}

fun Section.attribute(
        name: String,
        value: String,
        terms: MutableList<AttributeDetail> = mutableListOf(),
        ref: Boolean = false
): Attribute {
    val attribute = Attribute(
            name = name,
            value = value,
            valueAttrs = terms,
            reference = ref
    )
    attributes.add(attribute)
    return attribute
}

fun Section.section(block: Section.() -> Unit): Section {
    val section = Section().apply(block)
    sections.add(Left(section))
    return section
}

fun Section.link(block: Link.() -> Unit): Link {
    val link = Link("").apply(block)
    links.add(Left(link))
    return link
}

fun Link.attribute(name: String, value: String, terms: MutableList<AttributeDetail> = mutableListOf()): Attribute {
    val attribute = Attribute(name = name, value = value, valueAttrs = terms)
    attributes.add(attribute)
    return attribute
}

fun LinksTable.link(block: Link.() -> Unit): Link {
    val link = Link("").apply(block)
    addRow(link)
    return link
}

fun Section.sectionsTable(block: SectionsTable.() -> Unit) {
    val table = SectionsTable()
    table.apply(block)
    this.sections.add(Either.Right(table))
}

fun SectionsTable.section(block: Section.() -> Unit): Section {
    val section = Section().apply(block)
    addRow(section)
    return section
}

fun Section.linksTable(block: LinksTable.() -> Unit) {
    val table = LinksTable().apply(block)
    links.add(Right(table))
}

fun Section.file(block: File.() -> Unit) {
    val file = File("").apply(block)
    files.add(Left(file))
}

fun File.attribute(
        name: String,
        value: String,
        terms: MutableList<AttributeDetail> = mutableListOf(),
        ref: Boolean = false
): Attribute {
    val attribute = Attribute(
            name = name,
            value = value,
            valueAttrs = terms,
            reference = ref
    )
    attributes.add(attribute)
    return attribute
}

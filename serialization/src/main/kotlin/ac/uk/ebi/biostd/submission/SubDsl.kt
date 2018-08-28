@file:Suppress("TooManyFunctions")

package ac.uk.ebi.biostd.submission

import ac.uk.ebi.biostd.common.LinksTable
import ac.uk.ebi.biostd.common.SectionsTable
import arrow.core.Left
import arrow.core.Right

fun submission(block: Submission.() -> Unit): Submission {
    return Submission().apply(block)
}

fun Submission.section(block: Section.() -> Unit): Section {
    val section = Section().apply(block)
    this.section = section
    return section
}

fun Submission.attribute(name: String, value: String): Attribute {
    val attribute = Attribute(name = name, value = value, terms = emptyList())
    attributes.add(attribute)
    return attribute
}

fun Section.attribute(
        name: String,
        value: String,
        terms: List<Term> = emptyList(),
        ref: Boolean = false
): Attribute {
    val attribute = Attribute(
            name = name,
            value = value,
            terms = terms,
            reference = ref
    )
    attributes.add(attribute)
    return attribute
}

fun Section.section(block: Section.() -> Unit): Section {
    val section = Section().apply(block)
    subsections.add(Left(section))
    return section
}

fun Section.link(block: Link.() -> Unit): Link {
    val link = Link().apply(block)
    links.add(Left(link))
    return link
}

fun Link.attribute(name: String, value: String, terms: List<Term> = emptyList()): Attribute {
    val attribute = Attribute(name = name, value = value, terms = terms)
    attributes.add(attribute)
    return attribute
}

fun LinksTable.link(block: Link.() -> Unit): Link {
    val link = Link().apply(block)
    addRow(link)
    return link
}

fun Section.sectionsTable(type: String, block: SectionsTable.() -> Unit) {
    val table = SectionsTable(parentAccNo = this.accNo)
    table.apply(block)
    this.subsections.add(Right(table))
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
    val file = File().apply(block)
    files.add(Left(file))
}

fun File.attribute(
        name: String,
        value: String,
        terms: List<Term> = emptyList(),
        ref: Boolean = false
): Attribute {
    val attribute = Attribute(
            name = name,
            value = value,
            terms = terms,
            reference = ref
    )
    attributes.add(attribute)
    return attribute
}

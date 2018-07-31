package ac.uk.ebi.biostd.submission

import ac.uk.ebi.biostd.common.Left
import ac.uk.ebi.biostd.common.LinksTable
import ac.uk.ebi.biostd.common.Right
import ac.uk.ebi.biostd.common.SectionTable

typealias Term = Pair<String, String>

fun submission(block: Submission.() -> Unit): Submission {
    return Submission().apply(block)
}

fun Submission.section(block: Section.() -> Unit): Section {
    val section = Section().apply(block)
    this.sections.add(section)
    return section
}

fun Submission.attribute(name: String, value: String): Attribute {
    val attribute = Attribute(name = name, value = value, order = attributes.size, terms = emptyList())
    attributes.add(attribute)
    return attribute
}

fun Section.attribute(
        name: String,
        value: String,
        terms: List<Pair<String, String>> = emptyList(),
        ref: Boolean = false
): Attribute {
    val attribute = Attribute(
            name = name,
            value = value,
            order = attrs.size,
            terms = terms,
            reference = ref
    )
    attrs.add(attribute)
    return attribute
}

fun Section.section(block: Section.() -> Unit): Section {
    val section = Section().apply(block)
    sections.add(Left(section))
    return section
}

fun Section.link(block: Link.() -> Unit): Link {
    val link = Link().apply(block)
    links.add(Left(link))
    return link
}

fun Link.attribute(name: String, value: String, terms: List<Pair<String, String>> = emptyList()): Attribute {
    val attribute = Attribute(name = name, value = value, order = attrs.size, terms = terms)
    attrs.add(attribute)
    return attribute
}

fun LinksTable.link(block: Link.() -> Unit): Link {
    val link = Link().apply(block)
    addRow(link)
    return link
}

fun Section.sectionsTable(type: String, block: SectionTable.() -> Unit) {
    val table = SectionTable(type, this.accNo)
    table.apply(block)
    this.sections.add(Right(table))
}

fun SectionTable.section(block: Section.() -> Unit): Section {
    val section = Section().apply(block)
    addRow(section)
    return section
}

fun Section.linksTable(block: LinksTable.() -> Unit) {
    val table = LinksTable()
    table.apply(block)
    this.links.add(Right(table))
}

package ac.uk.ebi.biostd.submission

import ac.uk.ebi.biostd.common.Left
import ac.uk.ebi.biostd.common.Right
import ac.uk.ebi.biostd.common.Table
import ac.uk.ebi.biostd.serialization.tsv.LINK_TABLE_URL_HEADER

fun submission(block: Submission.() -> Unit): Submission {
    return Submission().apply(block)
}

fun Submission.section(block: Section.() -> Unit): Section {
    val section = Section().apply(block)
    this.sections.add(section)
    return section
}

fun Submission.attribute(name: String, value: String): Attribute {
    val attribute = Attribute(name = name, value = value, order = attributes.size)
    attributes.add(attribute)
    return attribute
}

fun Section.attribute(name: String, value: String, qualifierVal: String? = null, ref: Boolean = false): Attribute {
    val attribute = Attribute(
            name = name,
            value = value,
            order = attrs.size,
            qualifierVal = qualifierVal,
            reference = ref
    )
    attrs.add(attribute)
    return attribute
}

fun Section.section(block: Section.() -> Unit): Section {
    val section = Section().apply(block)
    sections.add(section)
    return section
}

fun Section.link(block: Link.() -> Unit): Link {
    val link = Link().apply(block)
    links.add(Left(link))
    return link
}

fun Link.attribute(name: String, value: String, qualifierVal: String? = null): Attribute {
    val attribute = Attribute(name = name, value = value, order = attrs.size, qualifierVal = qualifierVal)
    attrs.add(attribute)
    return attribute
}

fun Section.linksTable(block: Table<Link>.() -> Unit) {
    val table = Table<Link>(mainHeaderName = LINK_TABLE_URL_HEADER)
    table.apply(block)
    this.links.add(Right(table))
}

fun Table<Link>.link(block: Link.() -> Unit): Link {
    val link = Link().apply(block)
    addRow(link)
    return link
}

fun Section.sectionsTable(block: Section.() -> Unit) {
    this.apply(block)
    sections.mapIndexed { index, sec -> sec.tableIndex = index }
}




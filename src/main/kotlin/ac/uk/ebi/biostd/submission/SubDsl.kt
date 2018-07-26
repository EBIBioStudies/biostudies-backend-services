package ac.uk.ebi.biostd.submission

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
    links.add(link)
    return link
}

fun Link.attribute(name: String, value: String, qualifierVal: String? = null): Attribute {
    val attribute = Attribute(name = name, value = value, order = attributes.size, qualifierVal = qualifierVal)
    attributes.add(attribute)
    return attribute
}

fun Section.table(block: Section.() -> Unit) {
    this.apply(block)
    links.mapIndexed { index, link -> link.tableIndex = index }
}

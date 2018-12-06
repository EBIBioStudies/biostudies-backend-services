package ac.uk.ebi.biostd.tsv.deserialization.model

import ebi.ac.uk.model.Section

class SectionContext private constructor(
    val rootSection: Section,
    private var current: Section,
    private val sections: MutableMap<String, Section> = mutableMapOf()
)
    : Map<String, Section> by sections {

    constructor(rootSection: Section) : this(rootSection, rootSection, mutableMapOf())

    init {
        update(rootSection)
    }

    fun update(section: Section) {
        this.current = section
        section.accNo?.let { sections[it] = section }
    }

    val currentSection: Section
        get() {
            return current
        }
}

package ebi.ac.uk.model

fun Section.allFiles(): List<File> {
    return files.map { it.fold({ file -> listOf(file) }, { table -> table.elements }) }.flatten()
}

fun Section.allSections(): List<Section> {
    return sections.map { it.fold({ section -> listOf(section) }, { table -> table.elements }) }.flatten()
}

var Section.type: String
    get() = this[SectionFields.TYPE]
    set(value) {
        this[SectionFields.TYPE] = value
    }

var Section.accNo: String
    get() = this[SectionFields.ACC_NO]
    set(value) {
        this[SectionFields.ACC_NO] = value
    }

var Section.parentAccNo: String
    get() = this[SectionFields.PARENT_ACC_NO]
    set(value) {
        this[SectionFields.PARENT_ACC_NO] = value
    }



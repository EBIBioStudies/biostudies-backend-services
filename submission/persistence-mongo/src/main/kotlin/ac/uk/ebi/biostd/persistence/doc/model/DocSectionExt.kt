package ac.uk.ebi.biostd.persistence.doc.model

import ebi.ac.uk.model.constants.SectionFields

val DocSection.title: String?
    get() = attributes.find { it.name == SectionFields.TITLE.value }?.value

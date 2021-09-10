package ac.uk.ebi.biostd.persistence.filesystem.service

import ebi.ac.uk.extended.model.ExtSection

typealias Section = UpdatedSection

data class UpdatedSection(val changed: Boolean, val section: ExtSection)

package ebi.ac.uk.extended.mapping.to

import ebi.ac.uk.base.Either
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSectionTable
import ebi.ac.uk.model.Section
import ebi.ac.uk.model.SectionsTable
import ebi.ac.uk.model.constants.AUTHOR_TYPE
import ebi.ac.uk.model.constants.ORG_TYPES
import ebi.ac.uk.util.collections.mapRigh

class ToSectionMapper(
    private val toFileListMapper: ToFileListMapper,
) {
    suspend fun convert(
        sec: ExtSection,
        anonymize: Boolean,
    ): Section {
        fun skipSection(section: ExtSection): Boolean = anonymize && (section.isOrganization() || section.isAuthor())

        suspend fun convert(sec: ExtSection): Section = convert(sec, anonymize)

        suspend fun toTable(extSectionTable: ExtSectionTable): SectionsTable =
            SectionsTable(extSectionTable.sections.map { section -> convert(section) })

        val subSections =
            sec.sections
                .filterNot {
                    when (it) {
                        is Either.Left -> skipSection(it.value)
                        is Either.Right -> false
                    }
                }.mapRigh {
                    it.copy(sections = it.sections.filterNot { skipSection(it) })
                }

        return Section(
            type = sec.type,
            accNo = sec.accNo,
            fileList = sec.fileList?.let { toFileListMapper.convert(it) },
            attributes = sec.attributes.mapTo(mutableListOf()) { it.toAttribute() },
            files = sec.files.mapTo(mutableListOf()) { either -> either.bimap({ it.toFile() }, { it.toTable() }) },
            links = sec.links.mapTo(mutableListOf()) { either -> either.bimap({ it.toLink() }, { it.toTable() }) },
            sections = subSections.mapTo(mutableListOf()) { either -> either.bimap({ convert(it) }, { toTable(it) }) },
        )
    }

    private fun ExtSection.isAuthor(): Boolean = type.equals(AUTHOR_TYPE, ignoreCase = true)

    private fun ExtSection.isOrganization(): Boolean = ORG_TYPES.contains(type.lowercase())
}

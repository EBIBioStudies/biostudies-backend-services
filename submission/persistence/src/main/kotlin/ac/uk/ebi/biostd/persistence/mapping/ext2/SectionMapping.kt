package ac.uk.ebi.biostd.persistence.mapping.ext2

import ac.uk.ebi.biostd.persistence.model.Section
import ac.uk.ebi.biostd.persistence.model.ext.isTableElement
import arrow.core.Either
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSectionTable
import ebi.ac.uk.util.collections.component1
import ebi.ac.uk.util.collections.component2

class SectionMapper(private val fileMapper: FileMapper) {

    internal fun toExtSection(sectionDb: Section): ExtSection = sectionDb.run {
        ExtSection(
            type = type,
            accNo = accNo,
            attributes = toAttributes(attributes),
            links = toExtLinkList(links),
            files = fileMapper.toExtFileList(files),
            sections = toExtSectionList(sections))
    }

    internal fun toExtSectionList(files: Iterable<Section>) = files
        .groupBy { it.isTableElement() }
        .mapValues { it.value.map(::toExtSection) }
        .let { (filesTable, file) -> file.map { Either.left(it) }.plus(Either.right(ExtSectionTable(filesTable))) }

}


package ac.uk.ebi.biostd.mapping

import ac.uk.ebi.biostd.integration.SectionDb
import ac.uk.ebi.biostd.persistence.model.AbstractSection
import ac.uk.ebi.biostd.persistence.model.NO_TABLE_INDEX
import arrow.core.Either
import ebi.ac.uk.model.Section
import ebi.ac.uk.model.SectionsTable
import ebi.ac.uk.util.collections.ifNotEmpty

class SectionMapper(private val attributesMapper: AttributesMapper, private val tabularMapper: TabularMapper) {

    fun toSection(sectionDb: AbstractSection): Section {
        return mapSectionAttrs(sectionDb).apply {
            // links = tabularMapper.toLinks(sectionDb.links)
            // files = tabularMapper.toFiles(sectionDb.files)
            sections = mapSections(sectionDb.sections)
        }
    }

    private fun mapSectionAttrs(sectionDb: AbstractSection): Section {
        return Section().apply {
            //type = sectionDb.type
            //accNo = sectionDb.accNo
            attributes = attributesMapper.toAttributes(sectionDb.attributes)
        }
    }

    private fun mapSections(sections: MutableSet<SectionDb>): MutableList<Either<Section, SectionsTable>> {
        val (listElements, tableElements) = sections.partition { it.tableIndex == NO_TABLE_INDEX }

        val map: MutableMap<Int, Either<Section, ebi.ac.uk.model.SectionsTable>> = mutableMapOf()
        listElements.forEach { map[it.order] = Either.Left(toSection(it)) }
        tableElements.ifNotEmpty { map[min(tableElements)] = Either.Right(asTable(tableElements)) }

        return map.toSortedMap().values.toMutableList()
    }

    private fun min(sections: List<SectionDb>): Int {
        return sections.map { it.order }.min()!!
    }

    private fun asTable(sections: List<SectionDb>) = SectionsTable(sections.mapTo(mutableListOf()) { mapSectionAttrs(it) })
}

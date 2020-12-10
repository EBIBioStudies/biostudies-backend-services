package ac.uk.ebi.biostd.persistence.test

import ac.uk.ebi.biostd.persistence.common.NO_TABLE_INDEX
import ac.uk.ebi.biostd.persistence.model.DbSection
import arrow.core.Either.Companion.left
import arrow.core.Either.Companion.right
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSectionTable
import org.assertj.core.api.Assertions.assertThat
import java.util.SortedSet

const val ACC_NO = "accNo"
const val SEC_TYPE = "Study"

internal val extSection
    get() = ExtSection(
        accNo = ACC_NO,
        type = SEC_TYPE,
        fileList = extFileList,
        attributes = listOf(extAttribute),
        sections = listOf(right(extSectionTable), left(simpleExtSection)),
        files = extFiles,
        links = extLinks
    )

internal val extSectionTable
    get() = ExtSectionTable(listOf(simpleExtSection, simpleExtSection))

internal val simpleExtSection
    get() = ExtSection(type = "type", attributes = listOf(extAttribute))

internal fun assertDbExtSection(section: DbSection) {
    assertThat(section.accNo).isEqualTo(ACC_NO)
    assertThat(section.type).isEqualTo(SEC_TYPE)

    assertDbExtSections(section.sections)
    assertDbRefFiles(section.fileList)
    assertDbFiles(section.files)
    assertDbLinks(section.links)
}

private fun assertDbExtSections(sectionsSet: SortedSet<DbSection>) {
    val sections = sectionsSet.toList()

    assertThat(sections).hasSize(3)
    assertThat(sections[0].tableIndex).isEqualTo(0)
    assertThat(sections[0].order).isEqualTo(0)

    assertThat(sections[1].tableIndex).isEqualTo(1)
    assertThat(sections[1].order).isEqualTo(1)

    assertThat(sections[2].tableIndex).isEqualTo(NO_TABLE_INDEX)
    assertThat(sections[2].order).isEqualTo(2)
}

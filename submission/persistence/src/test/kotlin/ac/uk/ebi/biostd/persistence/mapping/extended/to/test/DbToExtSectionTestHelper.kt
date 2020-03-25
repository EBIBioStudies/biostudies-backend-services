package ac.uk.ebi.biostd.persistence.mapping.extended.to.test

import ac.uk.ebi.biostd.persistence.model.DbSection
import ebi.ac.uk.extended.model.ExtSection
import org.assertj.core.api.Assertions.assertThat

internal val sectionDb
    get() = DbSection(
        accNo = "accNo",
        type = "section-type",
        attributes = sortedSetOf(sectAttributeDb),
        links = sortedSetOf(),
        files = sortedSetOf()
    )

internal fun assertExtSection(sectionDb: DbSection, extSection: ExtSection) {
    assertThat(extSection.accNo).isEqualTo(sectionDb.accNo)
    assertThat(extSection.type).isEqualTo(sectionDb.type)

    assertThat(extSection.attributes).hasSize(1)
    assertExtAttribute(extSection.attributes.first())
}

package ac.uk.ebi.biostd.persistence.mapping.extended.to.test

import ac.uk.ebi.biostd.persistence.model.AttributeDetail
import ac.uk.ebi.biostd.persistence.model.DbAttribute
import ac.uk.ebi.biostd.persistence.model.DbFileAttribute
import ac.uk.ebi.biostd.persistence.model.DbLinkAttribute
import ac.uk.ebi.biostd.persistence.model.DbReferencedFileAttribute
import ac.uk.ebi.biostd.persistence.model.DbSectionAttribute
import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtAttributeDetail
import org.assertj.core.api.Assertions.assertThat

internal val attributeDb get() = DbAttribute("a", "b", 1, false, listOf(nameDetailDb), listOf(valueDetailDb))
internal val nameDetailDb get() = AttributeDetail("name attribute name", "name attribute value")
internal val valueDetailDb get() = AttributeDetail("value attribute name", "value attribute value")

internal val fileAttributeDb = DbFileAttribute(attributeDb)
internal val refAttributeDb = DbReferencedFileAttribute(attributeDb)
internal val linkAttributeDb = DbLinkAttribute(attributeDb)
internal val sectAttributeDb = DbSectionAttribute(attributeDb)

internal fun assertExtAttribute(extAttribute: ExtAttribute) {
    assertThat(extAttribute.name).isEqualTo(attributeDb.name)
    assertThat(extAttribute.value).isEqualTo(attributeDb.value)
    assertNameAttribute(extAttribute.nameAttrs)
    assertValueAttribute(extAttribute.valueAttrs)
}

private fun assertNameAttribute(nameAttrs: List<ExtAttributeDetail>) {
    assertThat(nameAttrs).hasSize(1)

    val nameAttribute = nameAttrs.first()
    assertThat(nameAttribute.name).isEqualTo(nameDetailDb.name)
    assertThat(nameAttribute.value).isEqualTo(nameDetailDb.value)
}

private fun assertValueAttribute(valueAttrs: List<ExtAttributeDetail>) {
    assertThat(valueAttrs).hasSize(1)

    val valueAttribute = valueAttrs.first()
    assertThat(valueAttribute.name).isEqualTo(valueDetailDb.name)
    assertThat(valueAttribute.value).isEqualTo(valueDetailDb.value)
}

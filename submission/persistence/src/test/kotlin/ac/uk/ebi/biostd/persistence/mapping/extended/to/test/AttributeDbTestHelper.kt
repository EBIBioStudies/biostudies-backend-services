package ac.uk.ebi.biostd.persistence.mapping.extended.to.test

import ac.uk.ebi.biostd.persistence.model.Attribute
import ac.uk.ebi.biostd.persistence.model.AttributeDetail
import ac.uk.ebi.biostd.persistence.model.FileAttribute
import ac.uk.ebi.biostd.persistence.model.ReferencedFileAttribute
import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtAttributeDetail
import org.assertj.core.api.Assertions

internal val attributeDb get() = Attribute("a", "b", 1, false, listOf(nameDetailDb), listOf(valueDetailDb))
internal val nameDetailDb get() = AttributeDetail("name attribute name", "name attribute value")
internal val valueDetailDb get() = AttributeDetail("value attribute name", "value attribute value")

internal val fileAttributeDb = FileAttribute(attributeDb)
internal val refAttributeDb = ReferencedFileAttribute(attributeDb)

internal fun assertExtAttribute(extAttribute: ExtAttribute) {
    Assertions.assertThat(extAttribute.name).isEqualTo(attributeDb.name)
    Assertions.assertThat(extAttribute.value).isEqualTo(attributeDb.value)
    assertNameAttribute(extAttribute.nameAttrs)
    assertValueAttribute(extAttribute.valueAttrs)
}

private fun assertNameAttribute(nameAttrs: List<ExtAttributeDetail>) {
    Assertions.assertThat(nameAttrs).hasSize(1)

    val nameAttribute = nameAttrs.first()
    Assertions.assertThat(nameAttribute.name).isEqualTo(nameDetailDb.name)
    Assertions.assertThat(nameAttribute.value).isEqualTo(nameDetailDb.value)
}

private fun assertValueAttribute(valueAttrs: List<ExtAttributeDetail>) {
    Assertions.assertThat(valueAttrs).hasSize(1)

    val valueAttribute = valueAttrs.first()
    Assertions.assertThat(valueAttribute.name).isEqualTo(valueDetailDb.name)
    Assertions.assertThat(valueAttribute.value).isEqualTo(valueDetailDb.value)
}

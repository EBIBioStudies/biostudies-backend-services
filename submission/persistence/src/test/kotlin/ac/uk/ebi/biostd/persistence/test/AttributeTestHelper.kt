package ac.uk.ebi.biostd.persistence.test

import ac.uk.ebi.biostd.persistence.model.AttributeDetail
import ac.uk.ebi.biostd.persistence.model.DbAttribute
import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtAttributeDetail
import org.assertj.core.api.Assertions.assertThat

internal val extAttribute
    get() = ExtAttribute("a", "b", false, listOf(extNameDetail), listOf(extValueDetail))

internal val extNameDetail
    get() = ExtAttributeDetail("name attribute name", "name attribute value")

internal val extValueDetail
    get() = ExtAttributeDetail("value attribute name", "value attribute value")

internal fun assertDbAttribute(dbAttribute: DbAttribute, simpleAttribute: ExtAttribute) {
    assertThat(dbAttribute.name).isEqualTo(simpleAttribute.name)
    assertThat(dbAttribute.value).isEqualTo(simpleAttribute.value)

    assertDetails(dbAttribute.nameQualifier, simpleAttribute.nameAttrs)
    assertDetails(dbAttribute.valueQualifier, simpleAttribute.valueAttrs)
}

private fun assertDetails(dbDetail: List<AttributeDetail>, extDetail: List<ExtAttributeDetail>) {
    assertThat(dbDetail).hasSameSizeAs(extDetail)
    for (index in dbDetail.indices) assertDbDetails(dbDetail[index], extDetail[index])
}

private fun assertDbDetails(dbDetail: AttributeDetail, extDetail: ExtAttributeDetail) {
    assertThat(dbDetail.name).isEqualTo(extDetail.name)
    assertThat(dbDetail.value).isEqualTo(extDetail.value)
}

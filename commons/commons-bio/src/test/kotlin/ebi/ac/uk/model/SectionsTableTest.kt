package ebi.ac.uk.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class SectionsTableTest {

    private val nameDetail = AttributeDetail("name detail name", "name detail value")
    private val valueDetail = AttributeDetail("value detail name", "value detail value")
    private val attribute = Attribute(
        name = "attributeName",
        value = "attributeValue",
        nameAttrs = mutableListOf(nameDetail),
        valueAttrs = mutableListOf(valueDetail)
    )

    private val subSection = Section(accNo = "acc-1", type = "element1", attributes = listOf(attribute))
    private val table = SectionsTable(listOf(subSection))

    @Test
    fun headers() {
        assertThat(table.headers).containsExactly(
            Header(name = "${subSection.type}[]"),
            Header(name = attribute.name, termNames = listOf(nameDetail.name), termValues = listOf(valueDetail.name)))
        assertThat(table.elements).containsExactly(subSection)
        assertThat(table.rows).containsExactly(listOf("acc-1", "attributeValue", "name detail value", "value detail value"))
    }
}
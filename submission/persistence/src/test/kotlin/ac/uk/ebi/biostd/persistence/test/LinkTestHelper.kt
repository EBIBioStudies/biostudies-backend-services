package ac.uk.ebi.biostd.persistence.test

import ac.uk.ebi.biostd.persistence.common.NO_TABLE_INDEX
import ac.uk.ebi.biostd.persistence.model.Link
import ebi.ac.uk.extended.model.ExtLink
import org.assertj.core.api.Assertions

internal val extTestLink get() = ExtLink("bio-link", listOf(extAttribute))

internal fun assertDbLink(link: Link, extLink: ExtLink, order: Int, tableOrder: Int = NO_TABLE_INDEX) {
    Assertions.assertThat(link.url).isEqualTo(extLink.url)
    Assertions.assertThat(link.order).isEqualTo(order)
    Assertions.assertThat(link.tableIndex).isEqualTo(tableOrder)

    Assertions.assertThat(link.attributes).hasSize(1)
    assertDbAttribute(link.attributes.first(), extAttribute)
}

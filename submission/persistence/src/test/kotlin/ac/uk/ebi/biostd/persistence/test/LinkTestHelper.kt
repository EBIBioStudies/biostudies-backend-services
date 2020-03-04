package ac.uk.ebi.biostd.persistence.test

import ac.uk.ebi.biostd.persistence.common.NO_TABLE_INDEX
import ac.uk.ebi.biostd.persistence.model.DbLink
import ebi.ac.uk.extended.model.ExtLink
import org.assertj.core.api.Assertions.assertThat

internal val extTestLink get() = ExtLink("bio-link", listOf(extAttribute))

internal fun assertDbLink(link: DbLink, extLink: ExtLink, order: Int, tableOrder: Int = NO_TABLE_INDEX) {
    assertThat(link.url).isEqualTo(extLink.url)
    assertThat(link.order).isEqualTo(order)
    assertThat(link.tableIndex).isEqualTo(tableOrder)

    assertThat(link.attributes).hasSize(1)
    assertDbAttribute(link.attributes.first(), extAttribute)
}

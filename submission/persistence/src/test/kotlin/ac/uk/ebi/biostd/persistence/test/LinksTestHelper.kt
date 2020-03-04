package ac.uk.ebi.biostd.persistence.test

import ac.uk.ebi.biostd.persistence.model.DbLink
import arrow.core.Either.Companion.left
import arrow.core.Either.Companion.right
import ebi.ac.uk.extended.model.ExtLinkTable
import org.assertj.core.api.Assertions.assertThat
import java.util.SortedSet

internal val extLinkTable
    get() = ExtLinkTable(listOf(extTestLink, extTestLink))

internal val extLinks
    get() = listOf(right(extLinkTable), left(extTestLink), right(extLinkTable))

internal fun assertDbLinks(links: SortedSet<DbLink>) {
    val linkList = links.toList()
    assertThat(linkList).hasSize(5)

    assertDbLink(linkList[0], extTestLink, order = 0, tableOrder = 0)
    assertDbLink(linkList[1], extTestLink, order = 1, tableOrder = 1)
    assertDbLink(linkList[2], extTestLink, order = 2, tableOrder = -1)
    assertDbLink(linkList[3], extTestLink, order = 3, tableOrder = 0)
    assertDbLink(linkList[4], extTestLink, order = 4, tableOrder = 1)
}

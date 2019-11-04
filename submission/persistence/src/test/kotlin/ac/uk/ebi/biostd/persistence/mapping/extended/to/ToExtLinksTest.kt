package ac.uk.ebi.biostd.persistence.mapping.extended.to

import ac.uk.ebi.biostd.persistence.common.NO_TABLE_INDEX
import ac.uk.ebi.biostd.persistence.mapping.extended.to.test.linkDb
import ac.uk.ebi.biostd.persistence.test.assertDbLink
import ebi.ac.uk.asserts.assertThat
import ebi.ac.uk.util.collections.second
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ToExtLinksTest {
    private val link = linkDb.apply { order = 0; tableIndex = NO_TABLE_INDEX; }
    private val tableLink = linkDb.apply { order = 1; tableIndex = 0 }
    private val anotherTableLink = linkDb.apply { order = 2; tableIndex = 1; }

    private val links = sortedSetOf(link, tableLink, anotherTableLink)

    @Test
    fun `Links to ExtLinkTable`() {
        val links = links.toExtLinks()

        assertThat(links.first()).hasLeftValueSatisfying { assertDbLink(link, it, 0, NO_TABLE_INDEX) }
        assertThat(links.second()).hasRightValueSatisfying { table ->
            assertThat(table.links).hasSize(2)
            assertDbLink(tableLink, table.links.first(), 1, 0)
            assertDbLink(anotherTableLink, table.links.second(), 2, 1)
        }
    }
}

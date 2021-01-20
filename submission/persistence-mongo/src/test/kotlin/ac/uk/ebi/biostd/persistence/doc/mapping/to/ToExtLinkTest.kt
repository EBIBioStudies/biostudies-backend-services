package ac.uk.ebi.biostd.persistence.doc.mapping.to

import ac.uk.ebi.biostd.persistence.doc.test.LinkTestHelper.assertExtLink
import ac.uk.ebi.biostd.persistence.doc.test.LinkTestHelper.docLink
import ac.uk.ebi.biostd.persistence.doc.test.LinkTestHelper.docLinkTable
import arrow.core.Either.Companion.left
import arrow.core.Either.Companion.right
import ebi.ac.uk.util.collections.ifLeft
import ebi.ac.uk.util.collections.ifRight
import ebi.ac.uk.util.collections.second
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ToExtLinkTest {
    @Test
    fun `to ext link`() {
        val extLink = docLink.toExtLink()
        assertExtLink(extLink)
    }

    @Test
    fun `to ext links table`() {
        val extLinksTable = docLinkTable.toExtLinkTable()
        assertThat(extLinksTable.links).hasSize(1)
        assertExtLink(extLinksTable.links.first())
    }

    @Test
    fun `to ext links`() {
        val links = listOf(left(docLink), right(docLinkTable))
        val extLinks = links.map { it.toExtLinks() }

        assertThat(extLinks).hasSize(2)
        extLinks.first().ifLeft { assertExtLink(it) }
        extLinks.second().ifRight {
            assertThat(it.links).hasSize(1)
            assertExtLink(it.links.first())
        }
    }
}

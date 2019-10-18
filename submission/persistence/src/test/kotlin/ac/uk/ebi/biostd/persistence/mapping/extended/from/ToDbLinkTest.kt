package ac.uk.ebi.biostd.persistence.mapping.extended.from

import ac.uk.ebi.biostd.persistence.test.assertDbLink
import ac.uk.ebi.biostd.persistence.test.extTestLink
import org.junit.jupiter.api.Test

internal class ToDbLinkTest {
    @Test
    fun `to db link when list link`() {
        val extLink = extTestLink
        val dbLink = extLink.toDbLink(1)

        assertDbLink(dbLink, extLink, 1)
    }

    @Test
    fun `to db link when table link`() {
        val extLink = extTestLink
        val dbLink = extLink.toDbLink(1, 6)

        assertDbLink(dbLink, extLink, 1, 6)
    }
}

package ac.uk.ebi.biostd.persistence.mapping.extended.from

import ac.uk.ebi.biostd.persistence.test.assertDbLinks
import ac.uk.ebi.biostd.persistence.test.extLinks
import org.junit.jupiter.api.Test

internal class ToDbLinksTest {
    @Test
    fun toDbFiles() {
        val extLinks = extLinks
        val dbLinks = extLinks.toDbLinks()

        assertDbLinks(dbLinks)
    }
}

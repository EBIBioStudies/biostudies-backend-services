package ac.uk.ebi.biostd.persistence.mapping.extended.to

import ac.uk.ebi.biostd.persistence.mapping.extended.to.test.assertExtLink
import ac.uk.ebi.biostd.persistence.mapping.extended.to.test.linkDb
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class ToExtLinkTest {
    @Test
    fun `Link to ExtLink`() {
        val extLink = linkDb.toExtLink()

        assertExtLink(extLink)
    }
}

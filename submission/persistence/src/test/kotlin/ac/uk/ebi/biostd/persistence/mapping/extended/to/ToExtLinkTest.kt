package ac.uk.ebi.biostd.persistence.mapping.extended.to

import ac.uk.ebi.biostd.persistence.mapping.extended.to.test.attribute
import ac.uk.ebi.biostd.persistence.mapping.extended.to.test.extTestAttribute2
import ac.uk.ebi.biostd.persistence.mapping.extended.to.test.linkAttribute
import ac.uk.ebi.biostd.persistence.model.Link
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockkStatic
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class ToExtLinkTest {
    @Test
    fun `Link to ExtLink`() {
        val link = Link(url = "link name", order = 1, attributes = sortedSetOf(linkAttribute))

        mockkStatic(TO_EXT_ATTRIBUTE_EXTENSIONS) {
            every { attribute.toExtAttribute() } returns extTestAttribute2

            val extLink = link.toExtLink()
            assertThat(extLink.url).isEqualTo(link.url)
            assertThat(extLink.attributes).containsOnly(extTestAttribute2)
        }
    }
}

package ebi.ac.uk.extended.mapping.from

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkStatic
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.biostd.extended.model.ExtAttribute
import uk.ac.ebi.biostd.model.Attribute
import uk.ac.ebi.biostd.model.Link

@ExtendWith(MockKExtension::class)
internal class ToExtLinkTest(
    @MockK val attribute: Attribute,
    @MockK val extAttribute: ExtAttribute,
) {
    private val link = Link("link", listOf(attribute))

    @Test
    fun toExtLink() {
        mockkStatic(TO_EXT_ATTRIBUTE_EXTENSIONS) {
            every { attribute.toExtAttribute() } returns extAttribute

            val extLink = link.toExtLink()

            assertThat(extLink.attributes).containsExactly(extAttribute)
            assertThat(extLink.url).isEqualTo(link.url)
        }
    }
}

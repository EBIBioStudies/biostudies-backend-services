package ebi.ac.uk.extended.mapping.serialization.from

import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.Link
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkStatic
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class ToExtLinkTest(
    @MockK val attribute: Attribute,
    @MockK val extAttribute: ExtAttribute
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

package uk.ac.ebi.biostd.extended.mapping.to

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkStatic
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.biostd.extended.model.ExtAttribute
import uk.ac.ebi.biostd.extended.model.ExtLink
import uk.ac.ebi.biostd.model.Attribute

@ExtendWith(MockKExtension::class)
internal class ToLinkTest(
    @MockK val attribute: Attribute,
    @MockK val extAttribute: ExtAttribute,
) {
    private val extLink = ExtLink("link", listOf(extAttribute))

    @Test
    fun toExtLink() {
        mockkStatic(TO_ATTRIBUTE_EXTENSIONS) {
            every { extAttribute.toAttribute() } returns attribute

            val extLink = extLink.toLink()

            assertThat(extLink.attributes).containsExactly(attribute)
            assertThat(extLink.url).isEqualTo(this.extLink.url)
        }
    }
}

package ac.uk.ebi.biostd.persistence.mapping.extended.to.test

import ac.uk.ebi.biostd.persistence.model.Link
import ebi.ac.uk.extended.model.ExtLink
import org.assertj.core.api.Assertions

private const val URL = "link-url"

internal val linkDb get() = Link(URL, 1, sortedSetOf(linkAttributeDb))

internal fun assertLinkDb(extFile: ExtLink) {
    Assertions.assertThat(extFile.url).isEqualTo(URL)

    Assertions.assertThat(extFile.attributes).hasSize(1)
    assertExtAttribute(extFile.attributes.first())
}

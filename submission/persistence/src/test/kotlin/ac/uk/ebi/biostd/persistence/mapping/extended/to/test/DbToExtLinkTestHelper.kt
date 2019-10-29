package ac.uk.ebi.biostd.persistence.mapping.extended.to.test

import ac.uk.ebi.biostd.persistence.model.Link
import ebi.ac.uk.extended.model.ExtLink
import org.assertj.core.api.Assertions.assertThat

private const val URL = "link-url"

internal val linkDb get() = Link(URL, 1, sortedSetOf(linkAttributeDb))

internal fun assertExtLink(extFile: ExtLink) {
    assertThat(extFile.url).isEqualTo(URL)
    assertThat(extFile.attributes).hasSize(1)
    assertExtAttribute(extFile.attributes.first())
}

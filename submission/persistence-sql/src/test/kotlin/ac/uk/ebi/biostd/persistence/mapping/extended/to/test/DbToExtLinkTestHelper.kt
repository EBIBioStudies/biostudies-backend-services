package ac.uk.ebi.biostd.persistence.mapping.extended.to.test

import ac.uk.ebi.biostd.persistence.model.DbLink
import ebi.ac.uk.extended.model.ExtLink
import org.assertj.core.api.Assertions.assertThat

private const val URL = "link-url"

internal val linkDb get() = DbLink(URL, 1, sortedSetOf(linkAttributeDb))

internal fun assertExtLink(extFile: ExtLink) {
    assertThat(extFile.url).isEqualTo(URL)
    assertThat(extFile.attributes).hasSize(1)
    assertExtAttribute(extFile.attributes.first())
}

package ac.uk.ebi.biostd.persistence.doc.test

import ac.uk.ebi.biostd.persistence.doc.model.DocLink
import ac.uk.ebi.biostd.persistence.doc.model.DocLinkTable
import ac.uk.ebi.biostd.persistence.doc.test.AttributeTestHelper.assertBasicExtAttribute
import ac.uk.ebi.biostd.persistence.doc.test.AttributeTestHelper.basicDocAttribute
import org.assertj.core.api.Assertions.assertThat
import uk.ac.ebi.biostd.extended.model.ExtLink

private const val TEST_URL = "http://mylink.org"

object LinkTestHelper {
    val docLink = DocLink(TEST_URL, listOf(basicDocAttribute))
    val docLinkTable = DocLinkTable(listOf(docLink))

    fun assertExtLink(extLink: ExtLink) {
        assertThat(extLink.url).isEqualTo(TEST_URL)
        extLink.attributes.forEach(::assertBasicExtAttribute)
    }
}

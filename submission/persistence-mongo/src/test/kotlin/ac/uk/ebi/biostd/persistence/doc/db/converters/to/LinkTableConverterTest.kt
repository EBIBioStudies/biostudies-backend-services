package ac.uk.ebi.biostd.persistence.doc.db.converters.to

import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocLinkTableFields
import ac.uk.ebi.biostd.persistence.doc.model.DocLink
import ac.uk.ebi.biostd.persistence.doc.model.DocLinkTable
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.bson.Document
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class LinkTableConverterTest(
    @MockK private val linkConverter: LinkConverter,
    @MockK val document: Document,
    @MockK val docLink: DocLink
) {
    private val testInstance = LinkTableConverter(linkConverter)

    @Test
    fun converter() {
        val links = listOf(docLink)
        every { linkConverter.convert(docLink) } returns document

        val docLinkTable = DocLinkTable(links)

        val result = testInstance.convert(docLinkTable)

        assertThat(result[DocLinkTableFields.LINK_TABLE_DOC_LINKS]).isEqualTo(listOf(document))
    }
}

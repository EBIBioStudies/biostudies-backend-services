package ac.uk.ebi.biostd.persistence.doc.db.converters.to

import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocLinkFields
import ac.uk.ebi.biostd.persistence.doc.model.DocAttribute
import ac.uk.ebi.biostd.persistence.doc.model.DocLink
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.bson.Document
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class LinkConverterTest(
    @MockK val attributeConverter: AttributeConverter,
    @MockK val document: Document,
    @MockK val docAttribute: DocAttribute,
) {
    private val testInstance = LinkConverter(attributeConverter)

    @Test
    fun converter() {
        val attributes = listOf(docAttribute)
        every { attributeConverter.convert(docAttribute) } returns document
        val docLink = DocLink(DOC_LINK_URL, attributes)

        val result = testInstance.convert(docLink)

        assertThat(result[DocLinkFields.LINK_DOC_URL]).isEqualTo(DOC_LINK_URL)
        assertThat(result[DocLinkFields.LINK_DOC_ATTRIBUTES]).isEqualTo(listOf(document))
    }

    private companion object {
        const val DOC_LINK_URL = "url"
    }
}

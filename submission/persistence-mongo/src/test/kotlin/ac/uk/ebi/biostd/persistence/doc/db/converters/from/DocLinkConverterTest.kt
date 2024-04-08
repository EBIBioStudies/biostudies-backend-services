package ac.uk.ebi.biostd.persistence.doc.db.converters.from

import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocLinkFields
import ac.uk.ebi.biostd.persistence.doc.db.converters.to.CommonsConverter
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
internal class DocLinkConverterTest(
    @MockK val docAttributeConverter: DocAttributeConverter,
    @MockK val documentAttr: Document,
    @MockK val docAttribute: DocAttribute,
) {
    private val testInstance = DocLinkConverter(docAttributeConverter)

    @Test
    fun convert() {
        every { docAttributeConverter.convert(documentAttr) } returns docAttribute

        val result = testInstance.convert(createDocLinkDocument())

        assertThat(result).isInstanceOf(DocLink::class.java)
        assertThat(result.url).isEqualTo(URL)
        assertThat(result.attributes).isEqualTo(listOf(docAttribute))
    }

    private fun createDocLinkDocument(): Document {
        val linkDoc = Document()
        linkDoc[CommonsConverter.CLASS_FIELD] = DocLinkFields.DOC_LINK_CLASS
        linkDoc[DocLinkFields.LINK_DOC_URL] = URL
        linkDoc[DocLinkFields.LINK_DOC_ATTRIBUTES] = listOf(documentAttr)
        return linkDoc
    }

    companion object {
        const val URL = "url"
    }
}

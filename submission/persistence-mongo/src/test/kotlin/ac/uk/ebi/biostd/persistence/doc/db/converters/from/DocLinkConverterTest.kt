package ac.uk.ebi.biostd.persistence.doc.db.converters.from

import ac.uk.ebi.biostd.persistence.doc.db.converters.to.CommonsConverter
import ac.uk.ebi.biostd.persistence.doc.db.converters.to.LinkConverter
import ac.uk.ebi.biostd.persistence.doc.model.DocAttribute
import ac.uk.ebi.biostd.persistence.doc.model.DocFile
import ac.uk.ebi.biostd.persistence.doc.model.DocLink
import ac.uk.ebi.biostd.persistence.doc.model.docLinkClass
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.bson.Document
import org.junit.jupiter.api.Test
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class DocLinkConverterTest(
    @MockK val docAttributeConverter: DocAttributeConverter,
    @MockK val documentAttr: Document,
    @MockK val docAttribute: DocAttribute
) {
    private val testInstance = DocLinkConverter(docAttributeConverter)

    @Test
    fun convert() {
        every { docAttributeConverter.convert(documentAttr) } returns docAttribute

        val result = testInstance.convert(createDocLinkDocument())

        assertThat(result).isInstanceOf(docLinkClazz)
        assertThat(result.url).isEqualTo(url)
        assertThat(result.attributes).isEqualTo(listOf(docAttribute))

    }

    private fun createDocLinkDocument(): Document {
        val linkDoc = Document()
        linkDoc[CommonsConverter.classField] = docLinkClass
        linkDoc[DocLinkConverter.docLinkUrl] = url
        linkDoc[DocLinkConverter.docLinkAttributes] = listOf(documentAttr)
        return linkDoc
    }
    companion object {
        val docLinkClazz = DocLink::class.java
        const val url = "url"
    }
}


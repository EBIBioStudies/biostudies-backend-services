package ac.uk.ebi.biostd.persistence.doc.db.converters.from

import ac.uk.ebi.biostd.persistence.doc.db.converters.to.CommonsConverter
import ac.uk.ebi.biostd.persistence.doc.db.converters.to.LinkTableConverter
import ac.uk.ebi.biostd.persistence.doc.model.*
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.bson.Document
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class DocLinkTableConverterTest(
    @MockK val docLinkConverter: DocLinkConverter,
    @MockK val documentLink: Document,
    @MockK val docLink: DocLink
) {
    private val testInstance = DocLinkTableConverter(docLinkConverter)

    @Test
    fun convert() {
        every { docLinkConverter.convert(documentLink) } returns docLink

        val result = testInstance.convert(createDocLinkTableDocument())
        assertThat(result).isInstanceOf(docLinkTableClazz)
        assertThat(result.links).isEqualTo(listOf(docLink))
    }

    private fun createDocLinkTableDocument(): Document {
        val linkTableDoc = Document()
        linkTableDoc[CommonsConverter.classField] = docLinkTableClass
        linkTableDoc[DocLinkTableConverter.docLinkTableLinks] = listOf(documentLink)
        return linkTableDoc
    }

    companion object {
        val docLinkTableClazz = DocLinkTable::class.java
    }
}


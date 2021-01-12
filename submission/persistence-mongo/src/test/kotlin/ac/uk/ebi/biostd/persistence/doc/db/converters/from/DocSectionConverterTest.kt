package ac.uk.ebi.biostd.persistence.doc.db.converters.from

import ac.uk.ebi.biostd.persistence.doc.db.converters.to.CommonsConverter.classField
import ac.uk.ebi.biostd.persistence.doc.model.*
import arrow.core.Either
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.bson.Document
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class DocSectionConverterTest(
    @MockK val docAttributeConverter: DocAttributeConverter,
    @MockK val docAttribute1: DocAttribute,
    @MockK val docAttribute2: DocAttribute,
    @MockK val docAttribute3: DocAttribute,

    @MockK val attributeDocument1: Document,
    @MockK val attributeDocument2: Document,
    @MockK val attributeDocument3: Document,

    @MockK val docFileListConverter: DocFileListConverter,
    @MockK val docFileList1: DocFileList,
    @MockK val docFileList2: DocFileList,
    @MockK val fileListDocument1: Document,
    @MockK val fileListDocument2: Document,

    @MockK val docFileConverter: DocFileConverter,
    @MockK val docFile1: DocFile,
    @MockK val fileDocument1: Document,

    @MockK val docFileTableConverter: DocFileTableConverter,
    @MockK val docFileTable1: DocFileTable,
    @MockK val fileTableDocument1: Document,

    @MockK val docLinkConverter: DocLinkConverter,
    @MockK val docLink1: DocLink,
    @MockK val linkDocument1: Document,

    @MockK val docLinkTableConverter: DocLinkTableConverter,
    @MockK val docLinkTable1: DocLinkTable,
    @MockK val linkTableDocument1: Document
) {
    private val testInstance = DocSectionConverter(
        docAttributeConverter,
        docLinkConverter,
        docLinkTableConverter,
        docFileConverter,
        docFileTableConverter,
        docFileListConverter
    )

    @Test
    fun convert() {
        every { docAttributeConverter.convert(attributeDocument1) } returns docAttribute1
        every { docAttributeConverter.convert(attributeDocument2) } returns docAttribute2
        every { docAttributeConverter.convert(attributeDocument3) } returns docAttribute3

        every { docFileListConverter.convert(fileListDocument1) } returns docFileList1
        every { docFileListConverter.convert(fileListDocument2) } returns docFileList2

        every { docFileConverter.convert(fileDocument1) } returns docFile1
        every { fileDocument1.getString(classField) } returns docFileClass

        every { docFileTableConverter.convert(fileTableDocument1) } returns docFileTable1
        every { fileTableDocument1.getString(classField) } returns docFileTableClass

        every { docLinkConverter.convert(linkDocument1) } returns docLink1
        every { linkDocument1.getString(classField) } returns docLinkClass

        every { docLinkTableConverter.convert(linkTableDocument1) } returns docLinkTable1
        every { linkTableDocument1.getString(classField)} returns docLinkTableClass

        val result = testInstance.convert(createDocSectionDocument())

        assertThatBasics(result)
        assertThatSections(result)
        assertThatFiles(result)
        assertThatLinks(result)
    }

    private fun assertThatBasics(result: DocSection) {
        assertThat(result.accNo).isEqualTo(AccNo1)
        assertThat(result.type).isEqualTo(Type1)
        assertThat(result.attributes).isEqualTo(listOf(docAttribute1))
        assertThat(result.fileList).isEqualTo(docFileList1)
    }

    private fun assertThatLinks(result: DocSection) {
        val links1 = result.links[0] as Either.Left<DocLink>
        assertThat(links1.a).isEqualTo(docLink1)
        val links2 = result.links[1] as Either.Right<DocLinkTable>
        assertThat(links2.b).isEqualTo(docLinkTable1)
    }

    private fun assertThatFiles(result: DocSection) {
        val files1 = result.files[0] as Either.Left<DocFile>
        assertThat(files1.a).isEqualTo(docFile1)
        val files2 = result.files[1] as Either.Right<DocFileTable>
        assertThat(files2.b).isEqualTo(docFileTable1)
    }

    private fun assertThatSections(result: DocSection) {
        val section1 = result.sections[0] as Either.Left<DocSection>
        assertThat(section1.a.accNo).isEqualTo(AccNo2)
        assertThat(section1.a.type).isEqualTo(Type2)
        assertThat(section1.a.fileList).isEqualTo(docFileList2)
        assertThat(section1.a.attributes).isEqualTo(listOf(docAttribute2))

        val section2 = result.sections[1] as Either.Right<DocSectionTable>
        assertThat(section2.b.sections[0].accNo).isEqualTo(AccNo3)
        assertThat(section2.b.sections[0].type).isEqualTo(Type3)
        assertThat(section2.b.sections[0].attributes).isEqualTo(listOf(docAttribute3))
    }

    private fun createDocSectionDocument(): Document {
        val sectionDoc = Document()
        sectionDoc[classField] = docSectionClass
        sectionDoc[DocSectionConverter.secAccNo] = AccNo1
        sectionDoc[DocSectionConverter.secType] = Type1
        sectionDoc[DocSectionConverter.secAttributes] = listOf(attributeDocument1)
        sectionDoc[DocSectionConverter.secFileList] = fileListDocument1
        sectionDoc[DocSectionConverter.secSections] = listOf(
            createInternalDocSection(AccNo2, Type2, attributeDocument2, fileListDocument2),
            createDocSectionTable(AccNo3, Type3, attributeDocument3)
        )
        sectionDoc[DocSectionConverter.secFiles] = listOf(fileDocument1, fileTableDocument1)
        sectionDoc[DocSectionConverter.secLinks] = listOf(linkDocument1, linkTableDocument1)
        return sectionDoc
    }

    private fun basicDocSection(accNo: String, type: String, attributeDocument: Document): Document {
        val sectionDoc = Document()
        sectionDoc[classField] = docSectionClass
        sectionDoc[DocSectionConverter.secAccNo] = accNo
        sectionDoc[DocSectionConverter.secType] = type
        sectionDoc[DocSectionConverter.secAttributes] = listOf(attributeDocument)
        return sectionDoc
    }

    private fun createInternalDocSection(accNo: String, type: String, attributeDocument: Document, fileListDocument: Document): Document {
        val sectionDoc = basicDocSection(accNo, type, attributeDocument)
        sectionDoc[DocSectionConverter.secFileList] = fileListDocument
        sectionDoc[DocSectionConverter.secSections] = listOf<Document>()
        sectionDoc[DocSectionConverter.secFiles] = listOf<Document>()
        sectionDoc[DocSectionConverter.secLinks] = listOf<Document>()
        return sectionDoc
    }

    private fun createDocSectionTable(accNo: String, type: String, attributeDocument: Document): Document {
        val sectionTableDocument = Document()
        sectionTableDocument[classField] = docSectionTableClass
        sectionTableDocument[DocSectionConverter.secTableSections] = listOf(basicDocSection(accNo, type, attributeDocument))
        return sectionTableDocument
    }

    private companion object {
        const val AccNo1 = "accNo1"
        const val AccNo2 = "accNo2"
        const val AccNo3 = "accNo3"
        const val Type1 = "type1"
        const val Type2 = "type2"
        const val Type3 = "type3"
    }
}


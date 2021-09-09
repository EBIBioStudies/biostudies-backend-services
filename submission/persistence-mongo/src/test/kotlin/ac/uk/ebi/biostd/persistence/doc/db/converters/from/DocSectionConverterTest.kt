package ac.uk.ebi.biostd.persistence.doc.db.converters.from

import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSectionFields
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSectionFields.SEC_ACC_NO
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSectionFields.SEC_ATTRIBUTES
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSectionFields.SEC_FILES
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSectionFields.SEC_FILE_LIST
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSectionFields.SEC_ID
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSectionFields.SEC_LINKS
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSectionFields.SEC_SECTIONS
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSectionFields.SEC_TYPE
import ac.uk.ebi.biostd.persistence.doc.db.converters.to.CommonsConverter.classField
import ac.uk.ebi.biostd.persistence.doc.model.DocAttribute
import ac.uk.ebi.biostd.persistence.doc.model.DocFileList
import ac.uk.ebi.biostd.persistence.doc.model.DocFile
import ac.uk.ebi.biostd.persistence.doc.model.DocFileTable
import ac.uk.ebi.biostd.persistence.doc.model.DocLink
import ac.uk.ebi.biostd.persistence.doc.model.DocLinkTable
import ac.uk.ebi.biostd.persistence.doc.model.docFileTableClass
import ac.uk.ebi.biostd.persistence.doc.model.docLinkClass
import ac.uk.ebi.biostd.persistence.doc.model.docLinkTableClass
import ac.uk.ebi.biostd.persistence.doc.model.DocSection
import ac.uk.ebi.biostd.persistence.doc.model.DocSectionTable
import ac.uk.ebi.biostd.persistence.doc.model.docSectionClass
import ac.uk.ebi.biostd.persistence.doc.model.docSectionTableClass
import ac.uk.ebi.biostd.persistence.doc.model.nfsDocFileClass
import arrow.core.Either
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.bson.Document
import org.bson.types.ObjectId
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
        every { fileDocument1.getString(classField) } returns nfsDocFileClass

        every { docFileTableConverter.convert(fileTableDocument1) } returns docFileTable1
        every { fileTableDocument1.getString(classField) } returns docFileTableClass

        every { docLinkConverter.convert(linkDocument1) } returns docLink1
        every { linkDocument1.getString(classField) } returns docLinkClass

        every { docLinkTableConverter.convert(linkTableDocument1) } returns docLinkTable1
        every { linkTableDocument1.getString(classField) } returns docLinkTableClass

        val result = testInstance.convert(createDocSectionDocument())

        assertThatBasics(result)
        assertThatSections(result)
        assertThatFiles(result)
        assertThatLinks(result)
    }

    private fun assertThatBasics(result: DocSection) {
        assertThat(result.id).isEqualTo(secId)
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
        sectionDoc[SEC_ID] = secId
        sectionDoc[SEC_ACC_NO] = AccNo1
        sectionDoc[SEC_TYPE] = Type1
        sectionDoc[SEC_FILE_LIST] = fileListDocument1
        sectionDoc[SEC_ATTRIBUTES] = listOf(attributeDocument1)
        sectionDoc[SEC_SECTIONS] = listOf(
            createInternalDocSection(AccNo2, Type2, attributeDocument2, fileListDocument2),
            createDocSectionTable(AccNo3, Type3, attributeDocument3)
        )
        sectionDoc[SEC_FILES] = listOf(fileDocument1, fileTableDocument1)
        sectionDoc[SEC_LINKS] = listOf(linkDocument1, linkTableDocument1)
        return sectionDoc
    }

    private fun basicDocSection(accNo: String, type: String, attributeDocument: Document): Document {
        val sectionDoc = Document()
        sectionDoc[classField] = docSectionClass
        sectionDoc[SEC_ID] = secId
        sectionDoc[SEC_ACC_NO] = accNo
        sectionDoc[SEC_TYPE] = type
        sectionDoc[SEC_ATTRIBUTES] = listOf(attributeDocument)
        return sectionDoc
    }

    private fun createInternalDocSection(accNo: String, type: String, attributeDocument: Document, fileListDocument: Document): Document {
        val sectionDoc = basicDocSection(accNo, type, attributeDocument)
        sectionDoc[SEC_FILE_LIST] = fileListDocument
        sectionDoc[SEC_SECTIONS] = listOf<Document>()
        sectionDoc[SEC_FILES] = listOf<Document>()
        sectionDoc[SEC_LINKS] = listOf<Document>()
        return sectionDoc
    }

    private fun createDocSectionTable(accNo: String, type: String, attributeDocument: Document): Document {
        val sectionTableDocument = Document()
        sectionTableDocument[classField] = docSectionTableClass
        sectionTableDocument[DocSectionFields.SEC_TABLE_SECTIONS] = listOf(basicDocSection(accNo, type, attributeDocument))
        return sectionTableDocument
    }

    private companion object {
        val secId = ObjectId()
        const val AccNo1 = "accNo1"
        const val AccNo2 = "accNo2"
        const val AccNo3 = "accNo3"
        const val Type1 = "type1"
        const val Type2 = "type2"
        const val Type3 = "type3"
    }
}

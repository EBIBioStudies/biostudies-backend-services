package ac.uk.ebi.biostd.persistence.doc.db.converters.to

import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSectionFields
import ac.uk.ebi.biostd.persistence.doc.model.DocAttribute
import ac.uk.ebi.biostd.persistence.doc.model.DocFile
import ac.uk.ebi.biostd.persistence.doc.model.DocFileList
import ac.uk.ebi.biostd.persistence.doc.model.DocFileTable
import ac.uk.ebi.biostd.persistence.doc.model.DocLink
import ac.uk.ebi.biostd.persistence.doc.model.DocLinkTable
import ac.uk.ebi.biostd.persistence.doc.model.DocSection
import ac.uk.ebi.biostd.persistence.doc.model.DocSectionTable
import ac.uk.ebi.biostd.persistence.doc.model.DocSectionTableRow
import arrow.core.Either
import ebi.ac.uk.util.collections.second
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.bson.Document
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class SectionConverterTest(
    @MockK val attributeConverter: AttributeConverter,
    @MockK val docAttribute1: DocAttribute,
    @MockK val docAttribute2: DocAttribute,
    @MockK val docAttribute3: DocAttribute,
    @MockK val attributeDocument1: Document,
    @MockK val attributeDocument2: Document,
    @MockK val attributeDocument3: Document,
    @MockK val fileListConverter: FileListConverter,
    @MockK val docFileList1: DocFileList,
    @MockK val docFileList2: DocFileList,
    @MockK val docFileList3: DocFileList,
    @MockK val fileListDocument1: Document,
    @MockK val fileListDocument2: Document,
    @MockK val fileListDocument3: Document,
    @MockK val linkConverter: LinkConverter,
    @MockK val docLink1: DocLink,
    @MockK val linkDocument1: Document,
    @MockK val linkTableConverter: LinkTableConverter,
    @MockK val docLinkTable1: DocLinkTable,
    @MockK val linkTableDocument1: Document,
    @MockK val fileConverter: FileConverter,
    @MockK val docFile1: DocFile,
    @MockK val fileDocument1: Document,
    @MockK val fileTableConverter: FileTableConverter,
    @MockK val docFileTable1: DocFileTable,
    @MockK val fileTableDocument1: Document,
) {
    private val testInstance =
        SectionConverter(
            attributeConverter,
            linkConverter,
            linkTableConverter,
            fileConverter,
            fileTableConverter,
            fileListConverter,
        )

    @Test
    fun convert() {
        every { attributeConverter.convert(docAttribute1) } returns attributeDocument1
        every { attributeConverter.convert(docAttribute2) } returns attributeDocument2
        every { attributeConverter.convert(docAttribute3) } returns attributeDocument3

        every { fileListConverter.convert(docFileList1) } returns fileListDocument1
        every { fileListConverter.convert(docFileList2) } returns fileListDocument2
        every { fileListConverter.convert(docFileList3) } returns fileListDocument3

        every { fileConverter.convert(docFile1) } returns fileDocument1
        every { fileTableConverter.convert(docFileTable1) } returns fileTableDocument1

        every { linkConverter.convert(docLink1) } returns linkDocument1
        every { linkTableConverter.convert(docLinkTable1) } returns linkTableDocument1

        val docSection = createDocSection()

        val result = testInstance.convert(docSection)

        assertMainSection(result)
        assertFiles(result.getAs(DocSectionFields.SEC_FILES))
        assertLinks(result.getAs(DocSectionFields.SEC_LINKS))

        val sections = result.getAs<List<Document>>(DocSectionFields.SEC_SECTIONS)
        assertSubSection(sections.first())

        val sectionTable = sections.second()
        val sectionsOfSecTable = sectionTable.getAs<List<Document>>(DocSectionFields.SEC_SECTIONS)
        assertTableSection(sectionsOfSecTable.first())
    }

    private fun assertLinks(links: List<Document>) {
        val firstLink = links.first()
        val secondLink = links.second()
        assertThat(firstLink).isEqualTo(linkDocument1)
        assertThat(secondLink).isEqualTo(linkTableDocument1)
    }

    private fun assertFiles(files: List<Document>) {
        val firstFile = files.first()
        val secondFile = files.second()
        assertThat(firstFile).isEqualTo(fileDocument1)
        assertThat(secondFile).isEqualTo(fileTableDocument1)
    }

    private fun assertTableSection(uniqueSection: Document) {
        assertThat(uniqueSection[DocSectionFields.SEC_ACC_NO]).isEqualTo(DOC_SECTION_ACC_NO_3)
        assertThat(uniqueSection[DocSectionFields.SEC_TYPE]).isEqualTo(DOC_SECTION_TYPE_3)
        assertThat(uniqueSection[DocSectionFields.SEC_ATTRIBUTES]).isEqualTo(listOf(attributeDocument3))
    }

    private fun assertMainSection(result: Document) {
        assertThat(result[DocSectionFields.SEC_ID]).isEqualTo(sectionId)
        assertThat(result[DocSectionFields.SEC_ACC_NO]).isEqualTo(DOC_SECTION_ACC_NO_1)
        assertThat(result[DocSectionFields.SEC_TYPE]).isEqualTo(DOC_SECTION_TYPE_1)
        assertThat(result[DocSectionFields.SEC_FILE_LIST]).isEqualTo(fileListDocument1)
        assertThat(result[DocSectionFields.SEC_ATTRIBUTES]).isEqualTo(listOf(attributeDocument1))
    }

    private fun assertSubSection(section: Document) {
        assertThat(section[DocSectionFields.SEC_ACC_NO]).isEqualTo(DOC_SECTION_ACC_NO_2)
        assertThat(section[DocSectionFields.SEC_TYPE]).isEqualTo(DOC_SECTION_TYPE_2)
        assertThat(section[DocSectionFields.SEC_FILE_LIST]).isEqualTo(fileListDocument2)
        assertThat(section[DocSectionFields.SEC_ATTRIBUTES]).isEqualTo(listOf(attributeDocument2))
        assertThat(section[DocSectionFields.SEC_SECTIONS]).isEqualTo(listOf<Document>())
        assertThat(section[DocSectionFields.SEC_FILES]).isEqualTo(listOf<Document>())
        assertThat(section[DocSectionFields.SEC_LINKS]).isEqualTo(listOf<Document>())
    }

    private fun createDocSection(): DocSection {
        return DocSection(
            id = sectionId,
            accNo = DOC_SECTION_ACC_NO_1,
            type = DOC_SECTION_TYPE_1,
            fileList = docFileList1,
            attributes = listOf(docAttribute1),
            sections =
                listOf(
                    Either.left(
                        DocSection(
                            ObjectId(),
                            DOC_SECTION_ACC_NO_2,
                            DOC_SECTION_TYPE_2,
                            docFileList2,
                            listOf(docAttribute2),
                            listOf(),
                            listOf(),
                            listOf(),
                        ),
                    ),
                    Either.right(
                        DocSectionTable(
                            listOf(
                                DocSectionTableRow(
                                    DOC_SECTION_ACC_NO_3,
                                    DOC_SECTION_TYPE_3,
                                    listOf(docAttribute3),
                                ),
                            ),
                        ),
                    ),
                ),
            files = listOf(Either.left(docFile1), Either.right(docFileTable1)),
            links = listOf(Either.left(docLink1), Either.right(docLinkTable1)),
        )
    }

    private companion object {
        val sectionId = ObjectId()
        const val DOC_SECTION_ACC_NO_1 = "AccNo1"
        const val DOC_SECTION_ACC_NO_2 = "AccNo2"
        const val DOC_SECTION_ACC_NO_3 = "AccNo3"
        const val DOC_SECTION_TYPE_1 = "type1"
        const val DOC_SECTION_TYPE_2 = "type2"
        const val DOC_SECTION_TYPE_3 = "type3"
    }
}

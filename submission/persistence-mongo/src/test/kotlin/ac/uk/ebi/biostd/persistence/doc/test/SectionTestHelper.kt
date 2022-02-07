package ac.uk.ebi.biostd.persistence.doc.test

import ac.uk.ebi.biostd.persistence.doc.model.DocSection
import ac.uk.ebi.biostd.persistence.doc.model.DocSectionTable
import ac.uk.ebi.biostd.persistence.doc.model.DocSectionTableRow
import ac.uk.ebi.biostd.persistence.doc.test.AttributeTestHelper.assertBasicExtAttribute
import ac.uk.ebi.biostd.persistence.doc.test.AttributeTestHelper.assertFullExtAttribute
import ac.uk.ebi.biostd.persistence.doc.test.AttributeTestHelper.basicDocAttribute
import ac.uk.ebi.biostd.persistence.doc.test.AttributeTestHelper.fullDocAttribute
import ac.uk.ebi.biostd.persistence.doc.test.FileTestHelper.assertExtFile
import ac.uk.ebi.biostd.persistence.doc.test.FileTestHelper.assertExtFileList
import ac.uk.ebi.biostd.persistence.doc.test.FileTestHelper.assertNonEmptyExtFileList
import ac.uk.ebi.biostd.persistence.doc.test.FileTestHelper.nfsDocFile
import ac.uk.ebi.biostd.persistence.doc.test.FileTestHelper.docFileList
import ac.uk.ebi.biostd.persistence.doc.test.LinkTestHelper.assertExtLink
import ac.uk.ebi.biostd.persistence.doc.test.LinkTestHelper.docLink
import arrow.core.Either.Companion.left
import arrow.core.Either.Companion.right
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.util.collections.ifLeft
import ebi.ac.uk.util.collections.ifRight
import ebi.ac.uk.util.collections.second
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import java.io.File

private const val SECT_ACC_NO = "SECT-001"
private const val SECT_TYPE = "Study"
private const val SUB_SECT_ACC_NO = "Exp1"
private const val SUB_SECT_TYPE = "Study"

internal object SectionTestHelper {
    private val docSubSection = DocSection(
        id = ObjectId(),
        accNo = SUB_SECT_ACC_NO,
        type = SUB_SECT_TYPE,
        attributes = listOf(fullDocAttribute)
    )

    private val docTableSection = DocSectionTableRow(
        accNo = SUB_SECT_ACC_NO,
        type = SUB_SECT_TYPE,
        attributes = listOf(fullDocAttribute)
    )

    val docSection = DocSection(
        id = ObjectId(),
        accNo = SECT_ACC_NO,
        type = SECT_TYPE,
        fileList = docFileList,
        attributes = listOf(basicDocAttribute),
        sections = listOf(left(docSubSection), right(DocSectionTable(listOf(docTableSection)))),
        files = listOf(left(nfsDocFile)),
        links = listOf(left(docLink))
    )

    fun assertExtSection(extSection: ExtSection, file: File) {
        assertThat(extSection.accNo).isEqualTo(SECT_ACC_NO)
        assertThat(extSection.type).isEqualTo(SECT_TYPE)
        assertExtSectionAttributes(extSection)
        assertExtSubsections(extSection)
        assertExtFileList(extSection.fileList!!)
        assertExtSectionFiles(extSection, file)
        assertExtSectionLinks(extSection)
    }

    fun assertExtSectionWithFileListFiles(extSection: ExtSection, file: File) {
        assertThat(extSection.accNo).isEqualTo(SECT_ACC_NO)
        assertThat(extSection.type).isEqualTo(SECT_TYPE)
        assertExtSectionAttributes(extSection)
        assertExtSubsections(extSection)
        assertNonEmptyExtFileList(extSection.fileList!!)
        assertExtSectionFiles(extSection, file)
        assertExtSectionLinks(extSection)
    }

    private fun assertExtSubsections(extSection: ExtSection) {
        assertThat(extSection.sections).hasSize(2)
        extSection.sections.first().ifLeft { assertExtSubsection(it) }
        extSection.sections.second().ifRight {
            assertThat(it.sections).hasSize(1)
            assertExtSubsection(it.sections.first())
        }
    }

    private fun assertExtSubsection(extSection: ExtSection) {
        assertThat(extSection.accNo).isEqualTo(SUB_SECT_ACC_NO)
        assertThat(extSection.type).isEqualTo(SUB_SECT_TYPE)
        assertThat(extSection.attributes).hasSize(1)
        assertFullExtAttribute(extSection.attributes.first())
    }

    private fun assertExtSectionAttributes(extSection: ExtSection) {
        assertThat(extSection.attributes).hasSize(1)
        assertBasicExtAttribute(extSection.attributes.first())
    }

    private fun assertExtSectionFiles(extSection: ExtSection, file: File) {
        assertThat(extSection.files).hasSize(2)
        extSection.files.first().ifLeft { assertExtFile(it, file) }
        extSection.files.second().ifLeft { assertExtFile(it, file) }
    }

    private fun assertExtSectionLinks(extSection: ExtSection) {
        assertThat(extSection.links).hasSize(1)
        extSection.links.first().ifLeft { assertExtLink(it) }
    }
}

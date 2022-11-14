package ac.uk.ebi.biostd.persistence.doc.mapping.to

import ac.uk.ebi.biostd.persistence.doc.test.AttributeTestHelper
import ac.uk.ebi.biostd.persistence.doc.test.AttributeTestHelper.assertFullExtAttribute
import ac.uk.ebi.biostd.persistence.doc.test.FileTestHelper
import ac.uk.ebi.biostd.persistence.doc.test.FileTestHelper.docFileList
import ac.uk.ebi.biostd.persistence.doc.test.LinkTestHelper
import ac.uk.ebi.biostd.persistence.doc.test.SECT_ACC_NO
import ac.uk.ebi.biostd.persistence.doc.test.SECT_TYPE
import ac.uk.ebi.biostd.persistence.doc.test.SUB_SECT_ACC_NO
import ac.uk.ebi.biostd.persistence.doc.test.SUB_SECT_TYPE
import ac.uk.ebi.biostd.persistence.doc.test.SectionTestHelper.docSection
import ac.uk.ebi.biostd.persistence.doc.test.TEST_FILENAME
import arrow.core.Either
import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.io.ext.md5
import ebi.ac.uk.io.ext.size
import ebi.ac.uk.util.collections.ifLeft
import ebi.ac.uk.util.collections.ifRight
import ebi.ac.uk.util.collections.second
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.io.File

@ExtendWith(TemporaryFolderExtension::class, MockKExtension::class)
class ToExtSectionMapperTest(
    temporaryFolder: TemporaryFolder,
) {
    private val sectionFile =
        temporaryFolder.root.resolve("submissions/S-TEST/123/S-TEST123/$FILES_DIR/$TEST_FILENAME").apply { mkdirs() }
    private val extFileList = mockk<ExtFileList>()
    private val toExtFileListMapper: ToExtFileListMapper = mockk()
    private val testInstance = ToExtSectionMapper(toExtFileListMapper)
    private val testFireDocFile = FileTestHelper.fireDocFile
    private val testNfsDocFile = FileTestHelper.nfsDocFile.copy(
        fullPath = sectionFile.absolutePath,
        md5 = sectionFile.md5(),
        fileSize = sectionFile.size()
    )

    @Test
    fun `to ext Submission without FileListFiles`() {
        val section = docSection.copy(
            files = listOf(Either.left(testNfsDocFile), Either.left(testFireDocFile)),
            fileList = docFileList
        )

        every {
            toExtFileListMapper.toExtFileList(
                docFileList,
                "subAccNo",
                121,
                false,
                "subRelPath",
                false,
            )
        } returns extFileList
        val extSection =
            testInstance.toExtSection(section, "subAccNo", 121, false, "subRelPath", includeFileListFiles = false)

        assertExtSection(extSection, sectionFile)
        assertThat(extSection.fileList).isEqualTo(extFileList)
    }

    @Test
    fun `to ext Submission including FileListFiles`() {
        val section = docSection.copy(
            files = listOf(Either.left(testNfsDocFile), Either.left(testFireDocFile)),
            fileList = docFileList
        )

        every {
            toExtFileListMapper.toExtFileList(
                docFileList,
                "subAccNo",
                121,
                false,
                "subRelPath",
                true,
            )
        } returns extFileList
        val extSection =
            testInstance.toExtSection(section, "subAccNo", 121, false, "subRelPath", includeFileListFiles = true)

        assertExtSection(extSection, sectionFile)
        assertThat(extSection.fileList).isEqualTo(extFileList)
    }

    private fun assertExtSection(extSection: ExtSection, file: File) {
        assertThat(extSection.accNo).isEqualTo(SECT_ACC_NO)
        assertThat(extSection.type).isEqualTo(SECT_TYPE)

        assertThat(extSection.attributes).hasSize(1)
        AttributeTestHelper.assertBasicExtAttribute(extSection.attributes.first())

        assertExtSubsections(extSection)
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

    private fun assertExtSectionFiles(extSection: ExtSection, file: File) {
        assertThat(extSection.files).hasSize(2)
        extSection.files.first().ifLeft { FileTestHelper.assertExtFile(it, file) }
        extSection.files.second().ifLeft { FileTestHelper.assertExtFile(it, file) }
    }

    private fun assertExtSectionLinks(extSection: ExtSection) {
        assertThat(extSection.links).hasSize(1)
        extSection.links.first().ifLeft { LinkTestHelper.assertExtLink(it) }
    }
}

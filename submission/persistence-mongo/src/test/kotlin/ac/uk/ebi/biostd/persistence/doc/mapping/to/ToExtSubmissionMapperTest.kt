package ac.uk.ebi.biostd.persistence.doc.mapping.to

import ac.uk.ebi.biostd.persistence.doc.model.NfsDocFile
import ac.uk.ebi.biostd.persistence.doc.test.AttributeTestHelper
import ac.uk.ebi.biostd.persistence.doc.test.OWNER
import ac.uk.ebi.biostd.persistence.doc.test.PROJECT_ACC_NO
import ac.uk.ebi.biostd.persistence.doc.test.REL_PATH
import ac.uk.ebi.biostd.persistence.doc.test.ROOT_PATH
import ac.uk.ebi.biostd.persistence.doc.test.SECRET_KEY
import ac.uk.ebi.biostd.persistence.doc.test.SUBMITTER
import ac.uk.ebi.biostd.persistence.doc.test.SUB_ACC_NO
import ac.uk.ebi.biostd.persistence.doc.test.SUB_SCHEMA_VERSION
import ac.uk.ebi.biostd.persistence.doc.test.SUB_TITLE
import ac.uk.ebi.biostd.persistence.doc.test.SUB_VERSION
import ac.uk.ebi.biostd.persistence.doc.test.SectionTestHelper.docSection
import ac.uk.ebi.biostd.persistence.doc.test.SubmissionTestHelper
import ac.uk.ebi.biostd.persistence.doc.test.SubmissionTestHelper.docSubmission
import ac.uk.ebi.biostd.persistence.doc.test.TAG_NAME
import ac.uk.ebi.biostd.persistence.doc.test.TAG_VALUE
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.ExtSubmissionMethod
import ebi.ac.uk.extended.model.StorageMode
import ebi.ac.uk.extended.model.createNfsFile
import ebi.ac.uk.io.ext.md5
import ebi.ac.uk.io.ext.newFile
import ebi.ac.uk.io.ext.size
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.coEvery
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.io.File
import java.time.ZoneOffset

@ExtendWith(TemporaryFolderExtension::class, MockKExtension::class)
class ToExtSubmissionMapperTest(
    temporaryFolder: TemporaryFolder,
) {
    private val extSection = mockk<ExtSection>()
    private val toExtSectionMapper: ToExtSectionMapper = mockk()
    private val testInstance = ToExtSubmissionMapper(toExtSectionMapper)
    private val fileNfs = temporaryFolder.createDirectory("folder").newFile("nfsFileFile.txt")
    private val subNfsDocFile =
        NfsDocFile(
            fileNfs.name,
            "filePath",
            "relPath",
            fileNfs.absolutePath,
            listOf(),
            fileNfs.md5(),
            fileNfs.size(),
            "file",
        )

    @Test
    fun `to ext Submission including FileListFiles`() =
        runTest {
            coEvery {
                toExtSectionMapper.toExtSection(
                    docSection,
                    "S-TEST123",
                    1,
                    false,
                    REL_PATH,
                    true,
                )
            } returns extSection
            val submission =
                docSubmission.copy(
                    section = docSection,
                    pageTabFiles = listOf(subNfsDocFile),
                )

            val extSubmission = testInstance.toExtSubmission(submission, includeFileListFiles = true)

            assertExtSubmission(extSubmission, fileNfs)
            assertThat(extSubmission.section).isEqualTo(extSection)
        }

    @Test
    fun `to ext Submission without FileListFiles`() =
        runTest {
            coEvery {
                toExtSectionMapper.toExtSection(
                    docSection,
                    "S-TEST123",
                    1,
                    false,
                    REL_PATH,
                    false,
                )
            } returns extSection
            val submission =
                docSubmission.copy(
                    section = docSection,
                    pageTabFiles = listOf(subNfsDocFile),
                )

            val extSubmission = testInstance.toExtSubmission(submission, includeFileListFiles = false)

            assertExtSubmission(extSubmission, fileNfs)
            assertThat(extSubmission.section).isEqualTo(extSection)
        }

    private fun assertExtSubmission(
        extSubmission: ExtSubmission,
        nfsFileFile: File,
    ) {
        assertBasicProperties(extSubmission)
        assertAttributes(extSubmission)
        assertTags(extSubmission)
        assertProject(extSubmission)
        assertThat(extSubmission.pageTabFiles.first()).isEqualTo(createNfsFile("filePath", "relPath", nfsFileFile))
    }

    private fun assertBasicProperties(extSubmission: ExtSubmission) {
        assertThat(extSubmission.accNo).isEqualTo(SUB_ACC_NO)
        assertThat(extSubmission.version).isEqualTo(SUB_VERSION)
        assertThat(extSubmission.schemaVersion).isEqualTo(SUB_SCHEMA_VERSION)
        assertThat(extSubmission.owner).isEqualTo(OWNER)
        assertThat(extSubmission.submitter).isEqualTo(SUBMITTER)
        assertThat(extSubmission.title).isEqualTo(SUB_TITLE)
        assertThat(extSubmission.method).isEqualTo(ExtSubmissionMethod.PAGE_TAB)
        assertThat(extSubmission.relPath).isEqualTo(REL_PATH)
        assertThat(extSubmission.rootPath).isEqualTo(ROOT_PATH)
        assertThat(extSubmission.released).isFalse
        assertThat(extSubmission.secretKey).isEqualTo(SECRET_KEY)
        assertThat(extSubmission.releaseTime).isEqualTo(SubmissionTestHelper.time.atOffset(ZoneOffset.UTC))
        assertThat(extSubmission.modificationTime).isEqualTo(SubmissionTestHelper.time.atOffset(ZoneOffset.UTC))
        assertThat(extSubmission.creationTime).isEqualTo(SubmissionTestHelper.time.atOffset(ZoneOffset.UTC))
        assertThat(extSubmission.storageMode).isEqualTo(StorageMode.NFS)
    }

    private fun assertAttributes(extSubmission: ExtSubmission) {
        assertThat(extSubmission.attributes).hasSize(1)
        AttributeTestHelper.assertFullExtAttribute(extSubmission.attributes.first())
    }

    private fun assertTags(extSubmission: ExtSubmission) {
        assertThat(extSubmission.tags).hasSize(1)
        assertThat(extSubmission.tags.first().name).isEqualTo(TAG_NAME)
        assertThat(extSubmission.tags.first().value).isEqualTo(TAG_VALUE)
    }

    private fun assertProject(extSubmission: ExtSubmission) {
        assertThat(extSubmission.collections).hasSize(1)
        assertThat(extSubmission.collections.first().accNo).isEqualTo(PROJECT_ACC_NO)
    }
}

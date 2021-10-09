package ac.uk.ebi.biostd.persistence.doc.test

import ac.uk.ebi.biostd.persistence.doc.model.DocCollection
import ac.uk.ebi.biostd.persistence.doc.model.DocProcessingStatus.PROCESSED
import ac.uk.ebi.biostd.persistence.doc.model.DocStat
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionMethod.PAGE_TAB
import ac.uk.ebi.biostd.persistence.doc.model.DocTag
import ac.uk.ebi.biostd.persistence.doc.model.FireDocDirectory
import ac.uk.ebi.biostd.persistence.doc.model.FireDocFile
import ac.uk.ebi.biostd.persistence.doc.model.NfsDocFile
import ac.uk.ebi.biostd.persistence.doc.test.AttributeTestHelper.assertFullExtAttribute
import ac.uk.ebi.biostd.persistence.doc.test.AttributeTestHelper.fullDocAttribute
import ac.uk.ebi.biostd.persistence.doc.test.SectionTestHelper.assertExtSection
import ac.uk.ebi.biostd.persistence.doc.test.SectionTestHelper.docSection
import ebi.ac.uk.extended.model.ExtProcessingStatus
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.ExtSubmissionMethod
import ebi.ac.uk.extended.model.FireDirectory
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.util.collections.second
import ebi.ac.uk.util.collections.third
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import java.io.File
import java.time.Instant
import java.time.ZoneOffset.UTC

private const val SUB_ACC_NO = "S-TEST123"
private const val SUB_VERSION = 1
private const val OWNER = "owner@mail.org"
private const val SUBMITTER = "submitter@mail.org"
private const val SUB_TITLE = "Test Submission"
private const val ROOT_PATH = "/a/root/path"
internal const val SECRET_KEY = "a-secret-key"
private const val TAG_NAME = "component"
private const val TAG_VALUE = "web"
private const val PROJECT_ACC_NO = "BioImages"
private const val STAT_TYPE = "VIEWS"
private const val STAT_VALUE = 123L
internal const val REL_PATH = "S-TEST/123/S-TEST123"

val fireDocFile = FireDocFile("filename", "filePath", "relPath", "fireId", listOf(), "md5", 1L)
val fireDocDirectory = FireDocDirectory("filename", "filePath", "relPath", listOf(), "md5", 1L)
val nfsDocFile = NfsDocFile("filename", "filePath", "relPath", "fileAbsPath", listOf(), "md5", 1L, "fileType")

object SubmissionTestHelper {
    private val time = Instant.now()

    val docSubmission = DocSubmission(
        id = ObjectId(),
        accNo = SUB_ACC_NO,
        version = SUB_VERSION,
        owner = OWNER,
        submitter = SUBMITTER,
        title = SUB_TITLE,
        method = PAGE_TAB,
        relPath = REL_PATH,
        rootPath = ROOT_PATH,
        released = false,
        secretKey = SECRET_KEY,
        status = PROCESSED,
        releaseTime = time,
        modificationTime = time,
        creationTime = time,
        attributes = listOf(fullDocAttribute),
        tags = listOf(DocTag(TAG_NAME, TAG_VALUE)),
        stats = listOf(DocStat(STAT_TYPE, STAT_VALUE)),
        collections = listOf(DocCollection(PROJECT_ACC_NO)),
        section = docSection,
        pageTabFiles = listOf(fireDocFile, fireDocDirectory, nfsDocFile)
    )

    fun assertExtSubmission(extSubmission: ExtSubmission, testFile: File, nfsFileFile: File) {
        assertBasicProperties(extSubmission)
        assertExtSection(extSubmission.section, testFile)
        assertAttributes(extSubmission)
        assertTags(extSubmission)
        assertStats(extSubmission)
        assertProject(extSubmission)
        assertThat(extSubmission.pageTabFiles.first()).isEqualTo(
            FireFile(
                fireDocFile.fileName,
                fireDocFile.filePath,
                fireDocFile.relPath,
                fireDocFile.fireId,
                fireDocFile.md5,
                1,
                listOf()
            )
        )
        assertThat(extSubmission.pageTabFiles.second()).isEqualTo(
            FireDirectory(
                fireDocDirectory.fileName,
                fireDocDirectory.filePath,
                fireDocDirectory.relPath,
                fireDocDirectory.md5,
                fireDocDirectory.fileSize,
                listOf()
            )
        )
        assertThat(extSubmission.pageTabFiles.third()).isEqualTo(
            NfsFile(
                nfsFileFile.name,
                nfsDocFile.filePath,
                nfsDocFile.relPath,
                nfsDocFile.fullPath,
                nfsFileFile,
                listOf()
            )
        )
    }

    private fun assertBasicProperties(extSubmission: ExtSubmission) {
        assertThat(extSubmission.accNo).isEqualTo(SUB_ACC_NO)
        assertThat(extSubmission.version).isEqualTo(SUB_VERSION)
        assertThat(extSubmission.owner).isEqualTo(OWNER)
        assertThat(extSubmission.submitter).isEqualTo(SUBMITTER)
        assertThat(extSubmission.title).isEqualTo(SUB_TITLE)
        assertThat(extSubmission.method).isEqualTo(ExtSubmissionMethod.PAGE_TAB)
        assertThat(extSubmission.relPath).isEqualTo(REL_PATH)
        assertThat(extSubmission.rootPath).isEqualTo(ROOT_PATH)
        assertThat(extSubmission.secretKey).isEqualTo(SECRET_KEY)
        assertThat(extSubmission.status).isEqualTo(ExtProcessingStatus.PROCESSED)
        assertThat(extSubmission.releaseTime).isEqualTo(time.atOffset(UTC))
        assertThat(extSubmission.modificationTime).isEqualTo(time.atOffset(UTC))
        assertThat(extSubmission.creationTime).isEqualTo(time.atOffset(UTC))
        assertThat(extSubmission.released).isFalse
    }

    private fun assertAttributes(extSubmission: ExtSubmission) {
        assertThat(extSubmission.attributes).hasSize(1)
        assertFullExtAttribute(extSubmission.attributes.first())
    }

    private fun assertTags(extSubmission: ExtSubmission) {
        assertThat(extSubmission.tags).hasSize(1)
        assertThat(extSubmission.tags.first().name).isEqualTo(TAG_NAME)
        assertThat(extSubmission.tags.first().value).isEqualTo(TAG_VALUE)
    }

    private fun assertStats(extSubmission: ExtSubmission) {
        assertThat(extSubmission.stats).hasSize(1)
        assertThat(extSubmission.stats.first().name).isEqualTo(STAT_TYPE)
        assertThat(extSubmission.stats.first().value).isEqualTo(STAT_VALUE.toString())
    }

    private fun assertProject(extSubmission: ExtSubmission) {
        assertThat(extSubmission.collections).hasSize(1)
        assertThat(extSubmission.collections.first().accNo).isEqualTo(PROJECT_ACC_NO)
    }
}

package ac.uk.ebi.biostd.persistence.doc.test

import ac.uk.ebi.biostd.persistence.doc.model.DocCollection
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionMethod.PAGE_TAB
import ac.uk.ebi.biostd.persistence.doc.model.DocTag
import ac.uk.ebi.biostd.persistence.doc.model.FireDocFile
import ac.uk.ebi.biostd.persistence.doc.model.NfsDocFile
import ac.uk.ebi.biostd.persistence.doc.test.AttributeTestHelper.fullDocAttribute
import ac.uk.ebi.biostd.persistence.doc.test.SectionTestHelper.docSection
import ebi.ac.uk.extended.model.ExtFileType.DIR
import ebi.ac.uk.extended.model.ExtFileType.FILE
import ebi.ac.uk.extended.model.StorageMode
import org.bson.types.ObjectId
import java.time.Instant

internal const val SUB_ACC_NO = "S-TEST123"
internal const val SUB_VERSION = 1
internal const val SUB_SCHEMA_VERSION = "1.0"
internal const val OWNER = "owner@mail.org"
internal const val SUBMITTER = "submitter@mail.org"
internal const val SUB_TITLE = "Test Submission"
internal const val ROOT_PATH = "/a/root/path"
internal const val SECRET_KEY = "a-secret-key"
internal const val TAG_NAME = "component"
internal const val TAG_VALUE = "web"
internal const val PROJECT_ACC_NO = "BioImages"
internal const val STAT_TYPE = "VIEWS"
internal const val STAT_VALUE = 123L
internal const val REL_PATH = "S-TEST/123/S-TEST123"

val fireDocFile = FireDocFile("filename", "filePath", "relPath", "fireId", listOf(), "md5", 1L, FILE.value)
val fireDocDirectory = FireDocFile("filename", "filePath", "relPath", "dirFireId", listOf(), "md5", 1L, DIR.value)
val nfsDocFile = NfsDocFile("filename", "filePath", "relPath", "fileAbsPath", listOf(), "md5", 1L, "fileType")

object SubmissionTestHelper {
    internal val time = Instant.now()

    val docSubmission = DocSubmission(
        id = ObjectId(),
        accNo = SUB_ACC_NO,
        version = SUB_VERSION,
        schemaVersion = SUB_SCHEMA_VERSION,
        storageMode = StorageMode.NFS,
        owner = OWNER,
        submitter = SUBMITTER,
        title = SUB_TITLE,
        method = PAGE_TAB,
        relPath = REL_PATH,
        rootPath = ROOT_PATH,
        released = false,
        secretKey = SECRET_KEY,
        releaseTime = time,
        modificationTime = time,
        creationTime = time,
        attributes = listOf(fullDocAttribute),
        tags = listOf(DocTag(TAG_NAME, TAG_VALUE)),
        collections = listOf(DocCollection(PROJECT_ACC_NO)),
        section = docSection,
        pageTabFiles = listOf(fireDocFile, fireDocDirectory, nfsDocFile),
    )
}

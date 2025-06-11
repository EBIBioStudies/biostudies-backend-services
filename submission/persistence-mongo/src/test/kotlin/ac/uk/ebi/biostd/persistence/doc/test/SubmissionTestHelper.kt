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
internal const val REL_PATH = "S-TEST/123/S-TEST123"
internal const val DOI = "10.6019/S-TEST123"

val fireDocFile =
    FireDocFile(
        fileName = "filename",
        filePath = "filePath",
        relPath = "relPath",
        fireId = "fireId",
        attributes = listOf(),
        md5 = "md5",
        fileSize = 1L,
        fileType = FILE.value,
    )
val fireDocDirectory =
    FireDocFile(
        fileName = "filename",
        filePath = "filePath",
        relPath = "relPath",
        fireId = "dirFireId",
        attributes = listOf(),
        md5 = "md5",
        fileSize = 1L,
        fileType = DIR.value,
    )
val nfsDocFile =
    NfsDocFile(
        fileName = "filename",
        filePath = "filePath",
        relPath = "relPath",
        fullPath = "fileAbsPath",
        attributes = listOf(),
        md5Calculated = true,
        md5 = "md5",
        fileSize = 1L,
        fileType = "fileType",
    )

object SubmissionTestHelper {
    internal val time = Instant.now()

    val docSubmission =
        DocSubmission(
            id = ObjectId(),
            accNo = SUB_ACC_NO,
            version = SUB_VERSION,
            schemaVersion = SUB_SCHEMA_VERSION,
            storageMode = StorageMode.NFS,
            owner = OWNER,
            submitter = SUBMITTER,
            title = SUB_TITLE,
            doi = DOI,
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

package ac.uk.ebi.biostd.persistence.doc.test.doc.ext

import ebi.ac.uk.extended.model.ExtCollection
import ebi.ac.uk.extended.model.ExtFileType.DIR
import ebi.ac.uk.extended.model.ExtFileType.FILE
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.ExtSubmissionMethod.PAGE_TAB
import ebi.ac.uk.extended.model.ExtTag
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.StorageMode
import java.time.OffsetDateTime
import java.time.ZoneOffset

const val SUBMISSION_ACC_NO = "S-TEST1"
const val SUBMISSION_VERSION = 1
const val SUBMISSION_SCHEMA_VERSION = "1.0"
const val SUBMISSION_OWNER = "owner@mail.org"
const val SUBMISSION_SUBMITTER = "submitter@mail.org"
const val SUBMISSION_TITLE = "TestSubmission"
val SUBMISSION_METHOD = PAGE_TAB
const val SUBMISSION_REL_PATH = "/a/rel/path"
const val SUBMISSION_ROOT_PATH = "/a/root/path"
const val SUBMISSION_RELEASED = true
const val SUBMISSION_SECRET_KEY = "a-secret-key"
const val SUBMISSION_DOI = "10.6019/S-TEST1"

val RELEASE_TIME: OffsetDateTime = OffsetDateTime.of(2019, 9, 21, 10, 30, 34, 15, ZoneOffset.UTC)
val MODIFICATION_TIME: OffsetDateTime = OffsetDateTime.of(2020, 9, 21, 10, 30, 34, 15, ZoneOffset.UTC)
val CREATION_TIME: OffsetDateTime = OffsetDateTime.of(2018, 9, 21, 10, 30, 34, 15, ZoneOffset.UTC)

const val EXT_TAG_NAME = "tagName"
const val EXT_TAG_VALUE = "tagValue"
val extTag = ExtTag(EXT_TAG_NAME, EXT_TAG_VALUE)

const val COLLECTION_ACC_NO = "BioImages"
val extCollection = ExtCollection(COLLECTION_ACC_NO)

const val FIRE_FILE_FILEPATH = "fireFileFilePath/fireFileFileName"
const val FIRE_FILE_REL_PATH = "fireFileRelPath"
const val FIRE_FILE_FIRE_ID = "fireFileFireID"
const val FIRE_DIR_FIRE_ID = "fireDirFireID"
const val FIRE_DIR_FIRE_PATH = "submission/fireFileFilePath/fireFileFileName"
const val FIRE_FILE_MD5 = "fireFileMd5"
const val FIRE_FILE_SIZE = 1L
const val FIRE_FILE_PUBLISHED = true

val fireFile = FireFile(
    fireId = FIRE_FILE_FIRE_ID,
    firePath = FIRE_DIR_FIRE_PATH,
    published = FIRE_FILE_PUBLISHED,
    filePath = FIRE_FILE_FILEPATH,
    relPath = FIRE_FILE_REL_PATH,
    md5 = FIRE_FILE_MD5,
    size = FIRE_FILE_SIZE,
    type = FILE,
    attributes = listOf()
)

const val FIRE_DIRECTORY_FILEPATH = "fireDirectoryFilePath/fireDirectoryFileName"
const val FIRE_DIRECTORY_REL_PATH = "fireDirectoryRelPath"
const val FIRE_DIRECTORY_FIRE_PATH = "submission/fireDirectoryFilePath/fireDirectoryFileName"
const val FIRE_DIRECTORY_MD5 = "fireDirectoryMd5"
const val FIRE_DIRECTORY_SIZE = 2L
val fireDirectory = FireFile(
    fireId = FIRE_DIR_FIRE_ID,
    firePath = FIRE_DIRECTORY_FIRE_PATH,
    published = false,
    filePath = FIRE_DIRECTORY_FILEPATH,
    relPath = FIRE_DIRECTORY_REL_PATH,
    md5 = FIRE_DIRECTORY_MD5,
    size = FIRE_DIRECTORY_SIZE,
    type = DIR,
    attributes = listOf()
)

const val NFS_FILENAME = "nfsFileName"
const val NFS_FILEPATH = "nfsFileFolder/nfsFileName"
const val NFS_REL_PATH = "Files/nfsFileFolder/nfsFileName"

val fullExtSubmission = ExtSubmission(
    accNo = SUBMISSION_ACC_NO,
    version = SUBMISSION_VERSION,
    schemaVersion = SUBMISSION_SCHEMA_VERSION,
    owner = SUBMISSION_OWNER,
    submitter = SUBMISSION_SUBMITTER,
    title = SUBMISSION_TITLE,
    doi = SUBMISSION_DOI,
    method = SUBMISSION_METHOD,
    relPath = SUBMISSION_REL_PATH,
    rootPath = SUBMISSION_ROOT_PATH,
    released = SUBMISSION_RELEASED,
    secretKey = SUBMISSION_SECRET_KEY,
    releaseTime = RELEASE_TIME,
    modificationTime = MODIFICATION_TIME,
    creationTime = CREATION_TIME,
    attributes = listOf(submissionAttribute),
    tags = listOf(extTag),
    collections = listOf(extCollection),
    section = rootSection,
    pageTabFiles = listOf(fireFile, fireDirectory),
    storageMode = StorageMode.NFS
)

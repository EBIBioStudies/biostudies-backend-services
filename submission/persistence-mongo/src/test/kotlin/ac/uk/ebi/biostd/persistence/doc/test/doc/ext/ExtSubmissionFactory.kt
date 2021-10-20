package ac.uk.ebi.biostd.persistence.doc.test.doc.ext

import ebi.ac.uk.extended.model.ExtCollection
import ebi.ac.uk.extended.model.ExtProcessingStatus.PROCESSED
import ebi.ac.uk.extended.model.ExtStat
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.ExtSubmissionMethod.PAGE_TAB
import ebi.ac.uk.extended.model.ExtTag
import ebi.ac.uk.extended.model.FireDirectory
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.NfsFile
import java.io.File
import java.time.OffsetDateTime
import java.time.ZoneOffset

const val SUBMISSION_ACC_NO = "S-TEST1"
const val SUBMISSION_VERSION = 1
const val SUBMISSION_OWNER = "owner@mail.org"
const val SUBMISSION_SUBMITTER = "submitter@mail.org"
const val SUBMISSION_TITLE = "TestSubmission"
val SUBMISSION_METHOD = PAGE_TAB
const val SUBMISSION_REL_PATH = "/a/rel/path"
const val SUBMISSION_ROOT_PATH = "/a/root/path"
const val SUBMISSION_RELEASED = true
const val SUBMISSION_SECRET_KEY = "a-secret-key"
val SUBMISSION_STATUS = PROCESSED
val RELEASE_TIME: OffsetDateTime = OffsetDateTime.of(2019, 9, 21, 10, 30, 34, 15, ZoneOffset.UTC)
val MODIFICATION_TIME: OffsetDateTime = OffsetDateTime.of(2020, 9, 21, 10, 30, 34, 15, ZoneOffset.UTC)
val CREATION_TIME: OffsetDateTime = OffsetDateTime.of(2018, 9, 21, 10, 30, 34, 15, ZoneOffset.UTC)

const val EXT_TAG_NAME = "tagName"
const val EXT_TAG_VALUE = "tagValue"
val extTag = ExtTag(EXT_TAG_NAME, EXT_TAG_VALUE)

const val COLLECTION_ACC_NO = "BioImages"
val extCollection = ExtCollection(COLLECTION_ACC_NO)

const val EXT_STAT_NAME = "component"
const val EXT_STAT_VALUE = "1"
val extStat = ExtStat(EXT_STAT_NAME, EXT_STAT_VALUE)

const val FIRE_FILE_FILENAME = "fireFileFileName"
const val FIRE_FILE_FILEPATH = "fireFileFilePath/fireFileFileName"
const val FIRE_FILE_REL_PATH = "fireFileRelPath"
const val FIRE_FILE_FIRE_ID = "fireFileFireID"
const val FIRE_FILE_MD5 = "fireFileMd5"
const val FIRE_FILE_SIZE = 1L
val fireFile = FireFile(
    FIRE_FILE_FILEPATH,
    FIRE_FILE_REL_PATH,
    FIRE_FILE_FIRE_ID,
    FIRE_FILE_MD5,
    FIRE_FILE_SIZE,
    listOf()
)

const val FIRE_DIRECTORY_FILENAME = "fireDirectoryFileName"
const val FIRE_DIRECTORY_FILEPATH = "fireDirectoryFilePath/fireDirectoryFileName"
const val FIRE_DIRECTORY_REL_PATH = "fireDirectoryRelPath"
const val FIRE_DIRECTORY_MD5 = "fireDirectoryMd5"
const val FIRE_DIRECTORY_SIZE = 2L
val fireDirectory = FireDirectory(
    FIRE_DIRECTORY_FILEPATH,
    FIRE_DIRECTORY_REL_PATH,
    FIRE_DIRECTORY_MD5,
    FIRE_DIRECTORY_SIZE,
    listOf()
)

const val NFS_FILENAME = "nfsFileName"
const val NFS_FILEPATH = "nfsFilePath/nfsFileName"
const val NFS_REL_PATH = "nfsRelPath"
const val NFS_FULL_PATH = "nfsFullPath"
val NFS_FILE = File(NFS_FILENAME)
val nfsFile = NfsFile(NFS_FILEPATH, NFS_REL_PATH, NFS_FULL_PATH, NFS_FILE, listOf())

val fullExtSubmission = ExtSubmission(
    accNo = SUBMISSION_ACC_NO,
    version = SUBMISSION_VERSION,
    owner = SUBMISSION_OWNER,
    submitter = SUBMISSION_SUBMITTER,
    title = SUBMISSION_TITLE,
    method = SUBMISSION_METHOD,
    relPath = SUBMISSION_REL_PATH,
    rootPath = SUBMISSION_ROOT_PATH,
    released = SUBMISSION_RELEASED,
    secretKey = SUBMISSION_SECRET_KEY,
    status = SUBMISSION_STATUS,
    releaseTime = RELEASE_TIME,
    modificationTime = MODIFICATION_TIME,
    creationTime = CREATION_TIME,
    attributes = listOf(submissionAttribute),
    tags = listOf(extTag),
    collections = listOf(extCollection),
    section = rootSection,
    stats = listOf(extStat),
    pageTabFiles = listOf(fireFile, fireDirectory)
)

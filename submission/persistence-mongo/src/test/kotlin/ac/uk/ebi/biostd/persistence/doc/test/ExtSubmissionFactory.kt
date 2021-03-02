package ac.uk.ebi.biostd.persistence.doc.test

import arrow.core.Either
import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtAttributeDetail
import ebi.ac.uk.extended.model.ExtCollection
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.extended.model.ExtFileTable
import ebi.ac.uk.extended.model.ExtLink
import ebi.ac.uk.extended.model.ExtLinkTable
import ebi.ac.uk.extended.model.ExtProcessingStatus.PROCESSED
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSectionTable
import ebi.ac.uk.extended.model.ExtStat
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.ExtSubmissionMethod.PAGE_TAB
import ebi.ac.uk.extended.model.ExtTag
import java.io.File
import java.time.OffsetDateTime
import java.time.ZoneOffset

const val EXT_TAG_NAME = "tagName"
const val EXT_TAG_VALUE = "tagValue"
val extTag = ExtTag(EXT_TAG_NAME, EXT_TAG_VALUE)

const val COLLECTION_ACC_NO = "BioImages"
val extCollection = ExtCollection(COLLECTION_ACC_NO)

val extStat = ExtStat("component", "1")

const val SEC_ACC_NO = "accNo-123"
const val EXT_FILE_LIST_FILENAME1 = "fileName1"

const val EXT_FILE_FILENAME = "fileName2"
val EXT_FILE_FILE = File("somePath")
val extFile = ExtFile(fileName = EXT_FILE_FILENAME, file = EXT_FILE_FILE)

const val SEC_TYPE = "Study"

const val EXT_FILE_LIST_FILENAME2 = "fileName2"
val subSection = ExtSection(
    type = SEC_TYPE,
    fileList = ExtFileList(
        fileName = EXT_FILE_LIST_FILENAME2,
        files = listOf(extFile)
    )
)

val subTableSection = ExtSection(
    type = SEC_TYPE,
    fileList = ExtFileList(
        fileName = EXT_FILE_LIST_FILENAME2,
        files = listOf(extFile)
    )
)

const val ATTRIBUTE_NAME1 = "name"
const val ATTRIBUTE_VALUE = "value"
const val ATTRIBUTE_REFERENCE = false
val ATTRIBUTE_NAME_ATTRS = listOf<ExtAttributeDetail>()
val ATTRIBUTE_VALUE_ATTRS = listOf<ExtAttributeDetail>()
val extAttribute = ExtAttribute(
    name = ATTRIBUTE_NAME1,
    value = ATTRIBUTE_VALUE,
    reference = ATTRIBUTE_REFERENCE,
    nameAttrs = ATTRIBUTE_NAME_ATTRS,
    valueAttrs = ATTRIBUTE_VALUE_ATTRS
)

const val EXT_LINK_URL = "url"
val extLink = ExtLink(
    url = EXT_LINK_URL,
    attributes = listOf(extAttribute)
)

val mainSection = ExtSection(
    accNo = SEC_ACC_NO,
    type = SEC_TYPE,
    fileList = ExtFileList(
        fileName = EXT_FILE_LIST_FILENAME1,
        files = listOf(extFile)
    ),
    attributes = listOf(extAttribute),
    sections = listOf(
        Either.left(subSection),
        Either.right(ExtSectionTable(sections = listOf(subSection)))
    ),
    files = listOf(
        Either.left(extFile),
        Either.right(ExtFileTable(files = listOf(extFile)))
    ),
    links = listOf(
        Either.left(extLink),
        Either.right(ExtLinkTable(links = listOf(extLink)))
    )
)

val RELEASE_TIME: OffsetDateTime = OffsetDateTime.of(2019, 9, 21, 10, 30, 34, 15, ZoneOffset.UTC)
val MODIFICATION_TIME: OffsetDateTime = OffsetDateTime.of(2020, 9, 21, 10, 30, 34, 15, ZoneOffset.UTC)
val CREATION_TIME: OffsetDateTime = OffsetDateTime.of(2018, 9, 21, 10, 30, 34, 15, ZoneOffset.UTC)
const val ACC_NO = "S-TEST1"
const val VERSION = 1
const val OWNER1 = "owner@mail.org"
const val SUBMITTER1 = "submitter@mail.org"
const val TITLE = "TestSubmission"
const val REL_PATH1 = "/a/rel/path"
const val RELEASED = true
val METHOD = PAGE_TAB
val STATUS = PROCESSED
const val ROOT_PATH1 = "/a/root/path"

val fullExtSubmission = ExtSubmission(
    accNo = ACC_NO,
    version = VERSION,
    owner = OWNER1,
    submitter = SUBMITTER1,
    title = TITLE,
    method = METHOD,
    relPath = REL_PATH1,
    rootPath = ROOT_PATH1,
    released = RELEASED,
    secretKey = SECRET_KEY,
    status = STATUS,
    releaseTime = RELEASE_TIME,
    modificationTime = MODIFICATION_TIME,
    creationTime = CREATION_TIME,
    attributes = listOf(extAttribute),
    tags = listOf(extTag),
    collections = listOf(extCollection),
    section = mainSection,
    stats = listOf(extStat)
)

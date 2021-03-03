package ac.uk.ebi.biostd.persistence.doc.test.doc.ext

import ebi.ac.uk.extended.model.ExtCollection
import ebi.ac.uk.extended.model.ExtProcessingStatus.PROCESSED
import ebi.ac.uk.extended.model.ExtStat
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.ExtSubmissionMethod.PAGE_TAB
import ebi.ac.uk.extended.model.ExtTag
import java.time.OffsetDateTime
import java.time.ZoneOffset

const val ACC_NO1 = "S-TEST1"
const val VERSION = 1
const val OWNER1 = "owner@mail.org"
const val SUBMITTER1 = "submitter@mail.org"
const val TITLE = "TestSubmission"
val METHOD = PAGE_TAB
const val REL_PATH1 = "/a/rel/path"
const val ROOT_PATH1 = "/a/root/path"
const val RELEASED = true
const val SECRET_KEY1 = "a-secret-key"
val STATUS = PROCESSED
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

val fullExtSubmission = ExtSubmission(
    accNo = ACC_NO1,
    version = VERSION,
    owner = OWNER1,
    submitter = SUBMITTER1,
    title = TITLE,
    method = METHOD,
    relPath = REL_PATH1,
    rootPath = ROOT_PATH1,
    released = RELEASED,
    secretKey = SECRET_KEY1,
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

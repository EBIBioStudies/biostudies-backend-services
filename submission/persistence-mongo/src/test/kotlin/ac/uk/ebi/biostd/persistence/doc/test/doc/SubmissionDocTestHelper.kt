package ac.uk.ebi.biostd.persistence.doc.test.doc

import ac.uk.ebi.biostd.persistence.doc.model.DocAttribute
import ac.uk.ebi.biostd.persistence.doc.model.DocAttributeDetail
import ac.uk.ebi.biostd.persistence.doc.model.DocCollection
import ac.uk.ebi.biostd.persistence.doc.model.DocFilesChanges
import ac.uk.ebi.biostd.persistence.doc.model.DocRequestProcessing
import ac.uk.ebi.biostd.persistence.doc.model.DocSection
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionMethod.PAGE_TAB
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionRequest
import ac.uk.ebi.biostd.persistence.doc.model.DocTag
import com.mongodb.BasicDBObject
import ebi.ac.uk.dsl.json.jsonObj
import ebi.ac.uk.extended.model.StorageMode
import ebi.ac.uk.model.RequestStatus.REQUESTED
import org.bson.types.ObjectId
import java.time.Instant
import java.time.temporal.ChronoUnit

internal const val ATTR_NAME = "Test Attribute"
internal const val ATTR_VALUE = "Test Value"
internal const val NAME_ATTR_NAME = "Test Name Attribute"
internal const val NAME_ATTR_VALUE = "Test Name Value"
internal const val VALUE_ATTR_NAME = "Test Value Attribute"
internal const val VALUE_ATTR_VALUE = "Test Value Value"

val testDocAttribute: DocAttribute =
    DocAttribute(
        name = ATTR_NAME,
        value = ATTR_VALUE,
        nameAttrs = listOf(DocAttributeDetail(NAME_ATTR_NAME, NAME_ATTR_VALUE)),
        valueAttrs = listOf(DocAttributeDetail(VALUE_ATTR_NAME, VALUE_ATTR_VALUE)),
    )

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
internal const val COLLECTION_ACC_NO = "BioImages"
internal const val REL_PATH = "S-TEST/123/S-TEST123"
internal const val DOI = "10.6019/S-TEST123"
internal val CREATION_TIME = Instant.now()
internal val SUBMISSION_TIME = CREATION_TIME.plus(1, ChronoUnit.HOURS)
internal val MODIFICATION_TIME = CREATION_TIME.plus(2, ChronoUnit.HOURS)
internal val RELEASE_TIME = CREATION_TIME.plus(3, ChronoUnit.DAYS)

internal val testDocSection = DocSection(id = ObjectId(), type = "Study")

internal val testDocCollection = DocCollection(COLLECTION_ACC_NO)

internal val testDocSubmission: DocSubmission
    get() =
        DocSubmission(
            id = ObjectId(),
            accNo = SUB_ACC_NO,
            version = SUB_VERSION,
            schemaVersion = SUB_SCHEMA_VERSION,
            owner = OWNER,
            submitter = SUBMITTER,
            title = SUB_TITLE,
            doi = DOI,
            method = PAGE_TAB,
            relPath = REL_PATH,
            rootPath = ROOT_PATH,
            released = false,
            secretKey = SECRET_KEY,
            releaseTime = RELEASE_TIME.truncatedTo(ChronoUnit.MILLIS),
            submissionTime = SUBMISSION_TIME.truncatedTo(ChronoUnit.MILLIS),
            modificationTime = MODIFICATION_TIME.truncatedTo(ChronoUnit.MILLIS),
            creationTime = CREATION_TIME.truncatedTo(ChronoUnit.MILLIS),
            attributes = listOf(testDocAttribute),
            tags = listOf(DocTag(TAG_NAME, TAG_VALUE)),
            collections = listOf(testDocCollection),
            section = testDocSection,
            storageMode = StorageMode.NFS,
        )

internal val testDocSubmissionRequest: DocSubmissionRequest
    get() =
        DocSubmissionRequest(
            id = ObjectId(),
            accNo = SUB_ACC_NO,
            version = SUB_VERSION,
            owner = SUBMITTER,
            draft = "draft-content",
            status = REQUESTED,
            creationTime = Instant.now().truncatedTo(ChronoUnit.MILLIS),
            modificationTime = Instant.now().truncatedTo(ChronoUnit.MILLIS).plusSeconds(1),
            newSubmission = true,
            onBehalfUser = null,
            files = emptyList(),
            preferredSources = listOf("SUBMISSION"),
            errors = emptyList(),
            process =
                DocRequestProcessing(
                    silentMode = false,
                    notifyTo = "user-b@test.org",
                    submission = BasicDBObject.parse(jsonObj { "submission" to SUB_ACC_NO }.toString()),
                    totalFiles = 51,
                    fileChanges = DocFilesChanges(1, 3, 10, 8, 2),
                    currentIndex = 61,
                    statusChanges = emptyList(),
                    previousVersion = 1,
                    singleJobMode = true,
                ),
        )

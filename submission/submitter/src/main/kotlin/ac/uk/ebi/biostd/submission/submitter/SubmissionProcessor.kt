package ac.uk.ebi.biostd.submission.submitter

import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceService
import ac.uk.ebi.biostd.submission.model.SubmitRequest
import ac.uk.ebi.biostd.submission.service.AccNoService
import ac.uk.ebi.biostd.submission.service.AccNoServiceRequest
import ac.uk.ebi.biostd.submission.service.CollectionInfoService
import ac.uk.ebi.biostd.submission.service.CollectionRequest
import ac.uk.ebi.biostd.submission.service.CollectionResponse
import ac.uk.ebi.biostd.submission.service.ParentInfoService
import ac.uk.ebi.biostd.submission.service.TimesRequest
import ac.uk.ebi.biostd.submission.service.TimesService
import ebi.ac.uk.base.orFalse
import ebi.ac.uk.extended.mapping.from.ToExtSectionMapper
import ebi.ac.uk.extended.mapping.from.toExtAttributes
import ebi.ac.uk.extended.model.ExtCollection
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.ExtSubmissionMethod
import ebi.ac.uk.extended.model.ExtTag
import ebi.ac.uk.extended.model.StorageMode
import ebi.ac.uk.model.AccNumber
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.SubmissionMethod
import ebi.ac.uk.model.constants.SUBMISSION_RESERVED_ATTRIBUTES
import ebi.ac.uk.model.constants.SubFields
import ebi.ac.uk.model.extensions.accNoTemplate
import ebi.ac.uk.model.extensions.attachTo
import ebi.ac.uk.model.extensions.releaseDate
import ebi.ac.uk.model.extensions.rootPath
import ebi.ac.uk.model.extensions.title
import ebi.ac.uk.security.integration.model.api.SecurityUser
import ebi.ac.uk.util.date.isBeforeOrEqual
import java.time.OffsetDateTime
import java.util.UUID

private const val DEFAULT_SCHEMA_VERSION = "1.0"

/**
 * TODO: refactor this class to avoid so many individual validations and unnecessary queries. All information required
 * to process submission is already in request (previous version and parent collection).
 * https://www.pivotaltracker.com/story/show/183068218
 */
@Suppress("LongParameterList")
class SubmissionProcessor(
    private val persistenceService: SubmissionPersistenceService,
    private val timesService: TimesService,
    private val accNoService: AccNoService,
    private val parentInfoService: ParentInfoService,
    private val collectionInfoService: CollectionInfoService,
    private val properties: ApplicationProperties,
    private val toExtSectionMapper: ToExtSectionMapper,
) {

    fun processSubmission(rqt: SubmitRequest): ExtSubmission {
        val (submission, submitter, sources, method, onBehalfUser, _, previousVersion, storageMode) = rqt
        val isNew = previousVersion == null
        val (parentTags, parentReleaseTime, parentPattern) = parentInfoService.getParentInfo(submission.attachTo)
        val (createTime, modTime, releaseTime) = getTimes(submission, previousVersion?.creationTime, parentReleaseTime)
        val released = releaseTime?.isBeforeOrEqual(OffsetDateTime.now()).orFalse()
        val accNo = getAccNumber(submission, isNew, submitter, parentPattern)
        val accNoString = accNo.toString()
        val version = persistenceService.getNextVersion(accNoString)
        val collectionInfo = getCollectionInfo(submitter, submission, accNoString, isNew)
        val secretKey = previousVersion?.secretKey ?: UUID.randomUUID().toString()
        val relPath = accNoService.getRelPath(accNo)
        val tags = getTags(parentTags, collectionInfo)
        val ownerEmail = onBehalfUser?.email ?: previousVersion?.owner ?: submitter.email
        return ExtSubmission(
            accNo = accNoString,
            owner = ownerEmail,
            submitter = submitter.email,
            version = version,
            schemaVersion = DEFAULT_SCHEMA_VERSION,
            method = getMethod(method),
            title = submission.title,
            relPath = relPath,
            rootPath = submission.rootPath,
            released = released,
            secretKey = secretKey,
            releaseTime = releaseTime,
            modificationTime = modTime,
            creationTime = createTime,
            tags = submission.tags.map { ExtTag(it.first, it.second) },
            collections = tags.map { ExtCollection(it) },
            section = toExtSectionMapper.convert(submission.accNo, version, submission.section, sources),
            attributes = submission.attributes.toExtAttributes(SUBMISSION_RESERVED_ATTRIBUTES),
            storageMode = storageMode ?: if (properties.persistence.enableFire) StorageMode.FIRE else StorageMode.NFS
        )
    }

    private fun getMethod(method: SubmissionMethod): ExtSubmissionMethod {
        return when (method) {
            SubmissionMethod.FILE -> ExtSubmissionMethod.FILE
            SubmissionMethod.PAGE_TAB -> ExtSubmissionMethod.PAGE_TAB
            SubmissionMethod.UNKNOWN -> ExtSubmissionMethod.UNKNOWN
        }
    }

    private fun getTags(parentTags: List<String>, collection: CollectionResponse?): List<String> {
        val tags = parentTags.filter { it != SubFields.PUBLIC_ACCESS_TAG.value }.toMutableList()
        if (collection != null) tags.add(collection.accessTag)
        return tags
    }

    private fun getCollectionInfo(
        user: SecurityUser,
        sub: Submission,
        accNo: String,
        isNew: Boolean,
    ): CollectionResponse? {
        val request = CollectionRequest(user.email, sub.section.type, sub.accNoTemplate, accNo, isNew)
        return collectionInfoService.process(request)
    }

    private fun getAccNumber(sub: Submission, isNew: Boolean, user: SecurityUser, parentPattern: String?): AccNumber {
        val accNo = sub.accNo.ifBlank { null }
        val request = AccNoServiceRequest(user.email, accNo, isNew, sub.attachTo, parentPattern)
        return accNoService.calculateAccNo(request)
    }

    private fun getTimes(sub: Submission, creationTime: OffsetDateTime?, parentReleaseTime: OffsetDateTime?) =
        timesService.getTimes(TimesRequest(sub.accNo, sub.releaseDate, creationTime, parentReleaseTime))
}

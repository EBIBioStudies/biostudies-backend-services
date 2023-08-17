package ac.uk.ebi.biostd.submission.submitter

import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceService
import ac.uk.ebi.biostd.submission.model.SubmitRequest
import ac.uk.ebi.biostd.submission.service.AccNoService
import ac.uk.ebi.biostd.submission.service.CollectionProcessor
import ac.uk.ebi.biostd.submission.service.DoiService
import ac.uk.ebi.biostd.submission.service.TimesService
import ebi.ac.uk.extended.mapping.from.ToExtSectionMapper
import ebi.ac.uk.extended.mapping.from.toExtAttributes
import ebi.ac.uk.extended.model.ExtCollection
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.ExtSubmissionMethod
import ebi.ac.uk.extended.model.ExtTag
import ebi.ac.uk.extended.model.StorageMode
import ebi.ac.uk.model.SubmissionMethod
import ebi.ac.uk.model.constants.SUBMISSION_RESERVED_ATTRIBUTES
import ebi.ac.uk.model.extensions.isCollection
import ebi.ac.uk.model.extensions.rootPath
import ebi.ac.uk.model.extensions.title
import mu.KotlinLogging
import java.util.UUID

private const val DEFAULT_SCHEMA_VERSION = "1.0"

private val logger = KotlinLogging.logger {}

@Suppress("LongParameterList")
class SubmissionProcessor(
    private val doiService: DoiService,
    private val persistenceService: SubmissionPersistenceService,
    private val timesService: TimesService,
    private val accNoService: AccNoService,
    private val collectionProcessor: CollectionProcessor,
    private val properties: ApplicationProperties,
    private val toExtSectionMapper: ToExtSectionMapper,
) {
    fun processSubmission(rqt: SubmitRequest): ExtSubmission {
        val (submission, submitter, sources, method, onBehalfUser, _, _, previousVersion, storageMode) = rqt
        val (creationTime, modificationTime, releaseTime, released) = timesService.getTimes(rqt)
        val accNo = accNoService.calculateAccNo(rqt)
        val accNoString = accNo.toString()

        logger.info { "${rqt.accNo} ${rqt.owner} Assigned accNo '$accNoString' to draft with key '${rqt.draftKey}'" }

        val doi = doiService.calculateDoi(accNoString, rqt)
        val version = persistenceService.getNextVersion(accNoString)
        val secretKey = previousVersion?.secretKey ?: UUID.randomUUID().toString()
        val relPath = accNoService.getRelPath(accNo)
        val tags = getTags(rqt)
        val ownerEmail = onBehalfUser?.email ?: previousVersion?.owner ?: submitter.email
        return ExtSubmission(
            accNo = accNoString,
            owner = ownerEmail,
            submitter = submitter.email,
            version = version,
            schemaVersion = DEFAULT_SCHEMA_VERSION,
            method = getMethod(method),
            title = submission.title,
            doi = doi,
            relPath = relPath,
            rootPath = submission.rootPath,
            released = released,
            secretKey = secretKey,
            releaseTime = releaseTime,
            modificationTime = modificationTime,
            creationTime = creationTime,
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

    private fun getTags(rqt: SubmitRequest): List<String> {
        val collectionTags = rqt.collection?.collections.orEmpty()
        if (rqt.submission.isCollection) {
            val collectionTag = collectionProcessor.process(rqt)
            return collectionTags + collectionTag
        }

        return collectionTags
    }
}

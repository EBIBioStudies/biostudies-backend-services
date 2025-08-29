package ac.uk.ebi.biostd.submission.domain.submission

import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import ac.uk.ebi.biostd.submission.model.SubmitRequest
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
import ebi.ac.uk.extended.model.StorageMode.FIRE
import ebi.ac.uk.extended.model.StorageMode.NFS
import ebi.ac.uk.model.SubmissionMethod
import ebi.ac.uk.model.constants.SUBMISSION_RESERVED_ATTRIBUTES
import ebi.ac.uk.model.extensions.isCollection
import ebi.ac.uk.model.extensions.rootPath
import ebi.ac.uk.model.extensions.title
import java.util.UUID

private const val DEFAULT_SCHEMA_VERSION = "1.0"

@Suppress("LongParameterList", "DestructuringDeclarationWithTooManyEntries")
class SubmissionProcessor(
    private val doiService: DoiService,
    private val timesService: TimesService,
    private val collectionProcessor: CollectionProcessor,
    private val properties: ApplicationProperties,
    private val toExtSectionMapper: ToExtSectionMapper,
) {
    suspend fun processSubmission(rqt: SubmitRequest): ExtSubmission {
        val (creationTime, submissionTime, releaseTime, released) = timesService.getTimes(rqt)
        val accNoString = rqt.accNo
        val submission = rqt.submission
        val previousVersion = rqt.previousVersion
        val secretKey = previousVersion?.secretKey ?: UUID.randomUUID().toString()
        val tags = getTags(rqt)
        val ownerEmail = rqt.onBehalfUser?.email ?: previousVersion?.owner ?: rqt.submitter.email
        val rootSection = toExtSectionMapper.convert(accNoString, rqt.version, submission.section, rqt.sources)
        val doi = doiService.calculateDoi(accNoString, rqt)

        return ExtSubmission(
            accNo = accNoString,
            owner = ownerEmail,
            submitter = rqt.submitter.email,
            version = rqt.version,
            schemaVersion = DEFAULT_SCHEMA_VERSION,
            method = getMethod(rqt.method),
            title = submission.title,
            doi = doi,
            relPath = rqt.relPath,
            rootPath = submission.rootPath,
            released = released,
            secretKey = secretKey,
            releaseTime = releaseTime,
            submissionTime = submissionTime,
            modificationTime = submissionTime,
            creationTime = creationTime,
            tags = submission.tags.map { ExtTag(it.first, it.second) },
            collections = tags.map { ExtCollection(it) },
            section = rootSection,
            attributes = submission.attributes.toExtAttributes(SUBMISSION_RESERVED_ATTRIBUTES),
            storageMode = getStorageMode(rqt.storageMode, previousVersion),
        )
    }

    private fun getStorageMode(
        rqtMode: StorageMode?,
        previousVersion: ExtSubmission?,
    ): StorageMode = rqtMode ?: previousVersion?.storageMode ?: if (properties.persistence.enableFire) FIRE else NFS

    private fun getMethod(method: SubmissionMethod): ExtSubmissionMethod =
        when (method) {
            SubmissionMethod.FILE -> ExtSubmissionMethod.FILE
            SubmissionMethod.PAGE_TAB -> ExtSubmissionMethod.PAGE_TAB
            SubmissionMethod.UNKNOWN -> ExtSubmissionMethod.UNKNOWN
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

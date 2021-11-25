package ac.uk.ebi.biostd.submission.submitter

import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import ac.uk.ebi.biostd.json.exception.NoAttributeValueException
import ac.uk.ebi.biostd.persistence.common.request.SaveSubmissionRequest
import ac.uk.ebi.biostd.persistence.common.service.SubmissionQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestService
import ac.uk.ebi.biostd.submission.service.ParentInfoService
import ac.uk.ebi.biostd.submission.service.TimesRequest
import ac.uk.ebi.biostd.submission.service.TimesService
import ebi.ac.uk.base.isNotBlank
import ebi.ac.uk.base.orFalse
import ebi.ac.uk.extended.mapping.from.toExtAttribute
import ebi.ac.uk.extended.mapping.from.toExtSection
import ebi.ac.uk.extended.mapping.from.toExtTable
import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.extended.model.ExtProcessingStatus.PROCESSED
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.ExtTag
import ebi.ac.uk.extended.model.FileMode.MOVE
import ebi.ac.uk.extended.model.StorageMode.FIRE
import ebi.ac.uk.extended.model.StorageMode.NFS
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.model.Section
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.constants.SUBMISSION_RESERVED_ATTRIBUTES
import ebi.ac.uk.model.extensions.attachTo
import ebi.ac.uk.model.extensions.releaseDate
import ebi.ac.uk.model.extensions.title
import ebi.ac.uk.util.date.isBeforeOrEqual
import java.time.OffsetDateTime

class SubmissionMetadataHandler(
    private val timesService: TimesService,
    private val properties: ApplicationProperties,
    private val parentInfoService: ParentInfoService,
    private val extSubmissionQueryService: SubmissionQueryService,
    private val submissionRequestService: SubmissionRequestService
) {
    fun updateMetadata(
        source: FilesSource,
        submission: Submission,
        previousVersion: ExtSubmission
    ): ExtSubmission {
        val extSubmission = processSubmission(source, submission, previousVersion)
        val saveRequest = SaveSubmissionRequest(extSubmission, MOVE, submission.accNo)

        return submissionRequestService.saveAndProcessSubmissionRequest(saveRequest)
    }

    private fun processSubmission(
        source: FilesSource,
        submission: Submission,
        previousVersion: ExtSubmission
    ): ExtSubmission {
        val (_, parentReleaseTime, _) = parentInfoService.getParentInfo(submission.attachTo)
        val (createTime, modTime, releaseTime) = getTimes(submission, previousVersion.creationTime, parentReleaseTime)
        val released = releaseTime?.isBeforeOrEqual(OffsetDateTime.now()).orFalse()

        return previousVersion.copy(
            version = previousVersion.version + 1,
            title = submission.title,
            released = released,
            status = PROCESSED,
            releaseTime = releaseTime,
            modificationTime = modTime,
            creationTime = createTime,
            tags = submission.tags.map { ExtTag(it.first, it.second) },
            section = populateFileLists(submission.accNo, submission.section, source),
            attributes = getAttributes(submission),
            storageMode = if (properties.persistence.enableFire) FIRE else NFS
        )
    }

    private fun populateFileLists(accNo: String, section: Section, source: FilesSource): ExtSection {
        val fileListName = section.fileList?.name
        val fileList = if (fileListName.isNotBlank()) buildFileList(accNo, fileListName!!) else null

        return section.toExtSection(source)
            .copy(
                fileList = fileList,
                sections = section.sections.map {
                    either -> either.bimap({ populateFileLists(accNo, it, source) }, { it.toExtTable(source) })
                }
            )
    }

    private fun buildFileList(accNo: String, fileListName: String) =
        ExtFileList(fileListName, extSubmissionQueryService.getReferencedFiles(accNo, fileListName))

    private fun getAttributes(submission: Submission): List<ExtAttribute> {
        return submission.attributes
            .onEach { require(it.value.isNotEmpty()) { throw NoAttributeValueException(it.value) } }
            .filterNot { SUBMISSION_RESERVED_ATTRIBUTES.contains(it.name) }
            .map { it.toExtAttribute() }
    }

    private fun getTimes(sub: Submission, creationTime: OffsetDateTime?, parentReleaseTime: OffsetDateTime?) =
        timesService.getTimes(TimesRequest(sub.accNo, sub.releaseDate, creationTime, parentReleaseTime))
}

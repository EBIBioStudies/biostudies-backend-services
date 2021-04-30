package ac.uk.ebi.biostd.persistence.mapping.extended.from

import ac.uk.ebi.biostd.persistence.exception.UserNotFoundException
import ac.uk.ebi.biostd.persistence.model.DbSubmission
import ac.uk.ebi.biostd.persistence.model.DbSubmissionAttribute
import ac.uk.ebi.biostd.persistence.repositories.AccessTagDataRepo
import ac.uk.ebi.biostd.persistence.repositories.TagDataRepository
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtCollection
import ebi.ac.uk.extended.model.ExtProcessingStatus
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.ExtSubmissionMethod
import ebi.ac.uk.extended.model.ExtTag
import ebi.ac.uk.model.SubmissionMethod
import ebi.ac.uk.model.constants.ProcessingStatus

private const val ROOT_SECTION_ORDER = 0

internal class ToDbSubmissionMapper(
    private val tagsRepository: AccessTagDataRepo,
    private val tagsRefRepository: TagDataRepository,
    private var userRepository: UserDataRepository
) {

    fun toSubmissionDb(submission: ExtSubmission) = toSubmissionDb(submission, DbSubmission())

    fun toSubmissionDb(submission: ExtSubmission, dbSubmission: DbSubmission): DbSubmission {
        return dbSubmission.apply {
            accNo = submission.accNo
            title = submission.title
            status = getStatus(submission.status)
            method = getMethod(submission.method)
            version = submission.version
            relPath = submission.relPath
            rootPath = submission.rootPath
            secretKey = submission.secretKey
            creationTime = submission.creationTime
            modificationTime = submission.modificationTime
            releaseTime = submission.releaseTime
            owner = getUser(submission.owner)
            submitter = getUser(submission.submitter)
            accessTags = toAccessTag(submission.collections)
            tags = toTags(submission.tags)
            released = submission.released
            attributes = submission.attributes.mapIndexedTo(sortedSetOf(), ::toDbSubmissionAttribute)
            rootSection = submission.section.toDbSection(this, ROOT_SECTION_ORDER)
        }
    }

    private fun getStatus(status: ExtProcessingStatus): ProcessingStatus =
        when (status) {
            ExtProcessingStatus.PROCESSED -> ProcessingStatus.PROCESSED
            ExtProcessingStatus.PROCESSING -> ProcessingStatus.PROCESSING
            ExtProcessingStatus.REQUESTED -> ProcessingStatus.REQUESTED
        }

    private fun getMethod(method: ExtSubmissionMethod): SubmissionMethod =
        when (method) {
            ExtSubmissionMethod.FILE -> SubmissionMethod.FILE
            ExtSubmissionMethod.PAGE_TAB -> SubmissionMethod.PAGE_TAB
            ExtSubmissionMethod.UNKNOWN -> SubmissionMethod.UNKNOWN
        }

    private fun toDbSubmissionAttribute(idx: Int, attr: ExtAttribute) = DbSubmissionAttribute(attr.toDbAttribute(idx))

    private fun toAccessTag(accessTags: List<ExtCollection>) =
        accessTags.mapTo(mutableSetOf()) { tagsRepository.findByName(it.accNo) }

    private fun toTags(tags: List<ExtTag>) =
        tags.mapTo(mutableSetOf()) { tagsRefRepository.findByClassifierAndName(it.name, it.value) }

    private fun getUser(email: String) = userRepository.findByEmail(email).orElseThrow { UserNotFoundException(email) }
}

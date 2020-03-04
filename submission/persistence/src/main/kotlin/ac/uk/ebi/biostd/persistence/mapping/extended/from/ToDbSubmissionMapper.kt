package ac.uk.ebi.biostd.persistence.mapping.extended.from

import ac.uk.ebi.biostd.persistence.model.DbSubmissionAttribute
import ac.uk.ebi.biostd.persistence.repositories.AccessTagDataRepo
import ac.uk.ebi.biostd.persistence.repositories.TagDataRepository
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ebi.ac.uk.extended.model.ExtAccessTag
import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.ExtTag
import ac.uk.ebi.biostd.persistence.model.Submission as SubmissionDb

private const val ROOT_SECTION_ORDER = 0

class ToDbSubmissionMapper(
    private val tagsRepository: AccessTagDataRepo,
    private val tagsRefRepository: TagDataRepository,
    private var userRepository: UserDataRepository
) {
    fun toSubmissionDb(submission: ExtSubmission, submitter: String) = SubmissionDb().apply {
        accNo = submission.accNo
        title = submission.title
        status = submission.status
        method = submission.method
        version = submission.version
        relPath = submission.relPath
        rootPath = submission.rootPath
        secretKey = submission.secretKey
        creationTime = submission.creationTime
        modificationTime = submission.modificationTime
        releaseTime = submission.releaseTime
        owner = userRepository.getByEmail(submitter)
        accessTags = toAccessTag(submission.accessTags)
        tags = toTags(submission.tags)
        released = submission.released
        attributes = submission.attributes.mapIndexedTo(sortedSetOf(), ::toDbSubmissionAttribute)
        rootSection = submission.section.toDbSection(this, ROOT_SECTION_ORDER)
    }

    private fun toDbSubmissionAttribute(idx: Int, attr: ExtAttribute) = DbSubmissionAttribute(attr.toDbAttribute(idx))

    private fun toAccessTag(accessTags: List<ExtAccessTag>) =
        accessTags.mapTo(mutableSetOf()) { tagsRepository.findByName(it.name) }

    private fun toTags(tags: List<ExtTag>) =
        tags.mapTo(mutableSetOf()) { tagsRefRepository.findByClassifierAndName(it.name, it.value) }
}

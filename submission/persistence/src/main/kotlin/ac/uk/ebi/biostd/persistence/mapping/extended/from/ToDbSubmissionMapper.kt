package ac.uk.ebi.biostd.persistence.mapping.extended.from

import ac.uk.ebi.biostd.persistence.model.SubmissionAttribute
import ac.uk.ebi.biostd.persistence.repositories.AccessTagDataRepository
import ac.uk.ebi.biostd.persistence.repositories.TagDataRepository
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ebi.ac.uk.extended.model.ExtAccessTag
import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.ExtTag
import ac.uk.ebi.biostd.persistence.model.Submission as SubmissionDb

private const val ROOT_SECTION_ORDER = 0

class ToDbSubmissionMapper(
    private val tagsRepository: AccessTagDataRepository,
    private val tagsRefRepository: TagDataRepository,
    private var userRepository: UserDataRepository
) {
    fun toSubmissionDb(submission: ExtSubmission, submitter: String) = SubmissionDb().apply {
        accNo = submission.accNo
        title = submission.title
        secretKey = submission.secretKey
        relPath = submission.relPath
        rootPath = submission.rootPath
        creationTime = submission.creationTime.toEpochSecond()
        modificationTime = submission.modificationTime.toEpochSecond()
        releaseTime = submission.releaseTime.toEpochSecond()
        owner = userRepository.getByEmail(submitter)
        accessTags = toAccessTag(submission.accessTags)
        tags = toTags(submission.tags)
        released = submission.released
        attributes = submission.attributes.mapIndexedTo(sortedSetOf(), ::asSubmissionAttribute)
        rootSection = submission.section.toDbSection(this, ROOT_SECTION_ORDER)
    }

    private fun asSubmissionAttribute(index: Int, attr: ExtAttribute) = SubmissionAttribute(attr.toDbAttribute(index))

    private fun toAccessTag(accessTags: List<ExtAccessTag>) =
        accessTags.mapTo(mutableSetOf()) { tagsRepository.findByName(it.name) }

    private fun toTags(tags: List<ExtTag>) =
        tags.mapTo(mutableSetOf()) { tagsRefRepository.findByClassifierAndName(it.name, it.value) }
}

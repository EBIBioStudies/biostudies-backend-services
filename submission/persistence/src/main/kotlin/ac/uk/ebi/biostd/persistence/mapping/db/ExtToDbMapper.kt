package ac.uk.ebi.biostd.persistence.mapping.db

import ac.uk.ebi.biostd.persistence.mapping.db.extensions.toDbAttribute
import ac.uk.ebi.biostd.persistence.mapping.db.extensions.toRootDbSection
import ac.uk.ebi.biostd.persistence.model.SubmissionAttribute
import ac.uk.ebi.biostd.persistence.repositories.TagsDataRepository
import ac.uk.ebi.biostd.persistence.repositories.TagsRefRepository
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.model.User
import ac.uk.ebi.biostd.persistence.model.Attribute as AttributeDb
import ac.uk.ebi.biostd.persistence.model.AttributeDetail as AttributeDetailDb
import ac.uk.ebi.biostd.persistence.model.File as FileDb
import ac.uk.ebi.biostd.persistence.model.LibraryFile as LibraryFileDb
import ac.uk.ebi.biostd.persistence.model.Link as LinkDb
import ac.uk.ebi.biostd.persistence.model.ReferencedFile as ReferencedFileDb
import ac.uk.ebi.biostd.persistence.model.Section as SectionDb
import ac.uk.ebi.biostd.persistence.model.Submission as SubmissionDb

private const val ROOT_SECTION_ORDER = 0

internal class ExtToDbMapper(
    private val tagsRepository: TagsDataRepository,
    private val tagsRefRepository: TagsRefRepository,
    private var userRepository: UserDataRepository
) {
    fun toSubmissionDb(submission: ExtSubmission, user: User) = SubmissionDb().apply {
        accNo = submission.accNo
        title = submission.title
        secretKey = submission.secretKey
        relPath = submission.relPath
        rootPath = submission.rootPath
        creationTime = submission.creationTime.toEpochSecond()
        modificationTime = submission.modificationTime.toEpochSecond()
        releaseTime = submission.releaseTime.toEpochSecond()
        owner = userRepository.getByEmail(user.email)
        accessTags = toAccessTag(submission.accessTags)
        tags = toTags(submission.tags)
        attributes = submission.attributes.mapIndexedTo(sortedSetOf(), ::asSubmissionAttribute)
        rootSection = submission.section.toRootDbSection(this, ROOT_SECTION_ORDER)
    }

    private fun asSubmissionAttribute(index: Int, attr: ExtAttribute) = SubmissionAttribute(attr.toDbAttribute(index))

    private fun toAccessTag(accessTags: List<String>) = accessTags.mapTo(mutableSetOf()) { tagsRepository.findByName(it) }

    private fun toTags(tags: List<Pair<String, String>>) =
        tags.mapTo(mutableSetOf()) { (classifier, tag) -> tagsRefRepository.findByClassifierAndName(classifier, tag) }
}


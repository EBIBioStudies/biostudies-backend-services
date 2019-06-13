package ac.uk.ebi.biostd.persistence.mapping

import ac.uk.ebi.biostd.persistence.mapping.extensions.toDbAttribute
import ac.uk.ebi.biostd.persistence.mapping.extensions.toDbFiles
import ac.uk.ebi.biostd.persistence.mapping.extensions.toDbLibraryFile
import ac.uk.ebi.biostd.persistence.mapping.extensions.toDbLinks
import ac.uk.ebi.biostd.persistence.model.SectionAttribute
import ac.uk.ebi.biostd.persistence.model.SubmissionAttribute
import ac.uk.ebi.biostd.persistence.repositories.TagsDataRepository
import ac.uk.ebi.biostd.persistence.repositories.TagsRefRepository
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtSection
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

internal class SubmissionExtMapper(
    private val tagsRepository: TagsDataRepository,
    private val tagsRefRepository: TagsRefRepository,
    private var userRepository: UserDataRepository,
    private val sectionExtMapper: SectionExtMapper
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
        rootSection = sectionExtMapper.toSection(submission.section, this)
    }

    private fun asSubmissionAttribute(index: Int, attr: ExtAttribute) = SubmissionAttribute(attr.toDbAttribute(index))

    private fun toAccessTag(accessTags: List<String>) = accessTags.mapTo(mutableSetOf()) { tagsRepository.findByName(it) }

    private fun toTags(tags: List<Pair<String, String>>) = tags.mapTo(mutableSetOf()) { (classifier, tag) -> tagsRefRepository.findByClassifierAndName(classifier, tag) }
}

internal class SectionExtMapper {

    fun toSection(section: ExtSection, parent: SubmissionDb?, index: Int): SectionDb {
        SectionDb(section.accNo, section.type).apply {
            order = index
            submission = parent
            attributes = section.attributes.mapIndexedTo(sortedSetOf(), ::asSectionAttribute)
            libraryFile = section.libraryFile?.toDbLibraryFile()
            links = section.links.toDbLinks()
            files = section.files.toDbFiles()
        }
    }

    private fun asSectionAttribute(index: Int, attr: ExtAttribute) = SectionAttribute(attr.toDbAttribute(index))
}


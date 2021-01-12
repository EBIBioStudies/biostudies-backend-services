package ac.uk.ebi.biostd.persistence.doc.mapping.from

import ac.uk.ebi.biostd.persistence.doc.model.DocProcessingStatus
import ac.uk.ebi.biostd.persistence.doc.model.DocProject
import ac.uk.ebi.biostd.persistence.doc.model.DocSection
import ac.uk.ebi.biostd.persistence.doc.model.DocSectionTable
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionMethod
import ac.uk.ebi.biostd.persistence.doc.model.DocTag
import arrow.core.Either
import ebi.ac.uk.extended.model.ExtProcessingStatus
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSectionTable
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.ExtSubmissionMethod

@Suppress("TooManyFunctions")
fun ExtSubmission.toDocSubmission() = DocSubmission(
    id = null,
    accNo = accNo,
    title = title,
    status = getStatus(status),
    method = getMethod(method),
    version = version,
    relPath = relPath,
    rootPath = rootPath,
    released = released,
    secretKey = secretKey,
    creationTime = creationTime.toInstant(),
    modificationTime = modificationTime.toInstant(),
    releaseTime = releaseTime?.toInstant(),
    owner = owner,
    submitter = submitter,
    projects = projects.map { DocProject(it.accNo) },
    tags = tags.map { DocTag(it.name, it.value) },
    attributes = attributes.map { it.toDocAttribute() },
    section = section.toDocSection())

private fun ExtSection.toDocSection(): DocSection = DocSection(
    accNo = accNo,
    type = type,
    fileList = fileList?.toDocFileList(),
    attributes = attributes.map { it.toDocAttribute() },
    files = files.map { it.toDocFiles() },
    links = links.map { it.toDocLinks() },
    sections = sections.map { it.toDocSections() }
)

// Section Mapping
private fun Either<ExtSection, ExtSectionTable>.toDocSections() =
    bimap(ExtSection::toDocSection, ExtSectionTable::toDocSectionTable)

private fun ExtSectionTable.toDocSectionTable() = DocSectionTable(sections.map { it.toDocSection() })

private fun getStatus(status: ExtProcessingStatus) =
    when (status) {
        ExtProcessingStatus.PROCESSED -> DocProcessingStatus.PROCESSED
        ExtProcessingStatus.PROCESSING -> DocProcessingStatus.PROCESSING
        ExtProcessingStatus.REQUESTED -> DocProcessingStatus.REQUESTED
    }

private fun getMethod(method: ExtSubmissionMethod) =
    when (method) {
        ExtSubmissionMethod.FILE -> DocSubmissionMethod.FILE
        ExtSubmissionMethod.PAGE_TAB -> DocSubmissionMethod.PAGE_TAB
        ExtSubmissionMethod.UNKNOWN -> DocSubmissionMethod.UNKNOWN
    }

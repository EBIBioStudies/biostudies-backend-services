package ac.uk.ebi.biostd.persistence.doc.mapping.from

import ac.uk.ebi.biostd.persistence.doc.model.DocAttribute
import ac.uk.ebi.biostd.persistence.doc.model.DocAttributeDetail
import ac.uk.ebi.biostd.persistence.doc.model.DocFile
import ac.uk.ebi.biostd.persistence.doc.model.DocFileList
import ac.uk.ebi.biostd.persistence.doc.model.DocFileTable
import ac.uk.ebi.biostd.persistence.doc.model.DocLink
import ac.uk.ebi.biostd.persistence.doc.model.DocLinkTable
import ac.uk.ebi.biostd.persistence.doc.model.DocProcessingStatus
import ac.uk.ebi.biostd.persistence.doc.model.DocProject
import ac.uk.ebi.biostd.persistence.doc.model.DocSection
import ac.uk.ebi.biostd.persistence.doc.model.DocSectionTable
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionMethod
import ac.uk.ebi.biostd.persistence.doc.model.DocTag
import arrow.core.Either
import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtAttributeDetail
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.extended.model.ExtFileTable
import ebi.ac.uk.extended.model.ExtLink
import ebi.ac.uk.extended.model.ExtLinkTable
import ebi.ac.uk.extended.model.ExtProcessingStatus
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSectionTable
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.ExtSubmissionMethod

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

// File Mapping
private fun Either<ExtFile, ExtFileTable>.toDocFiles() = bimap(ExtFile::toDocFile, ExtFileTable::toDocFileTable)
private fun ExtFile.toDocFile(): DocFile = DocFile(fileName, attributes.map { it.toDocAttribute() }, md5)
private fun ExtFileTable.toDocFileTable() = DocFileTable(files.map { it.toDocFile() })
private fun ExtFileList.toDocFileList(): DocFileList = DocFileList(fileName, files.map { it.toDocFile() })

// Links Mapping
private fun Either<ExtLink, ExtLinkTable>.toDocLinks() = bimap(ExtLink::toDocLink, ExtLinkTable::toDocLinkTable)
private fun ExtLink.toDocLink(): DocLink = DocLink(url, attributes.map { it.toDocAttribute() })
private fun ExtLinkTable.toDocLinkTable() = DocLinkTable(links.map { it.toDocLink() })

// Section Mapping
private fun Either<ExtSection, ExtSectionTable>.toDocSections() = bimap(ExtSection::toDocSection, ExtSectionTable::toDocSectionTable)
private fun ExtSectionTable.toDocSectionTable() = DocSectionTable(sections.map { it.toDocSection() })

// Attributes Mapping
private fun List<ExtAttributeDetail>.toExtAttr() = map { DocAttributeDetail(it.name, it.value) }
private fun ExtAttribute.toDocAttribute() = DocAttribute(name, value, reference, nameAttrs.toExtAttr(), valueAttrs.toExtAttr())

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

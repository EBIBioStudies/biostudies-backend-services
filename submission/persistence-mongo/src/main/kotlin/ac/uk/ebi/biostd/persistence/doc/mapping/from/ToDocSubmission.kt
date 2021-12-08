package ac.uk.ebi.biostd.persistence.doc.mapping.from

import ac.uk.ebi.biostd.persistence.doc.model.DocCollection
import ac.uk.ebi.biostd.persistence.doc.model.DocProcessingStatus
import ac.uk.ebi.biostd.persistence.doc.model.DocSection
import ac.uk.ebi.biostd.persistence.doc.model.DocStat
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionMethod
import ac.uk.ebi.biostd.persistence.doc.model.DocTag
import ac.uk.ebi.biostd.persistence.doc.model.FileListDocFile
import ebi.ac.uk.extended.model.ExtProcessingStatus
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.ExtSubmissionMethod
import org.bson.types.ObjectId

fun ExtSubmission.toDocSubmission(): Pair<DocSubmission, List<FileListDocFile>> {
    val submissionId = ObjectId()
    val (docSection, fileList) = section.toDocSection(submissionId)
    val docSubmission = toDocSubmission(submissionId, docSection)
    return Pair(docSubmission, fileList)
}

private fun ExtSubmission.toDocSubmission(submissionId: ObjectId, docSection: DocSection): DocSubmission {
    return DocSubmission(
        id = submissionId,
        accNo = accNo,
        title = title,
        status = getStatus(status),
        method = getMethod(method),
        version = version,
        schemaVersion = schemaVersion,
        relPath = relPath,
        rootPath = rootPath,
        released = released,
        secretKey = secretKey,
        creationTime = creationTime.toInstant(),
        modificationTime = modificationTime.toInstant(),
        releaseTime = releaseTime?.toInstant(),
        owner = owner,
        submitter = submitter,
        collections = collections.map { DocCollection(it.accNo) },
        tags = tags.map { DocTag(it.name, it.value) },
        attributes = attributes.map { it.toDocAttribute() },
        section = docSection,
        stats = stats.map { DocStat(it.name, it.value.toLong()) },
        pageTabFiles = pageTabFiles.map { it.toDocFile() },
        storageMode = storageMode
    )
}

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

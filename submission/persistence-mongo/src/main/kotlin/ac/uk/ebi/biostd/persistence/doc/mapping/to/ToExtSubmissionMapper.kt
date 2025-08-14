package ac.uk.ebi.biostd.persistence.doc.mapping.to

import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionMethod
import ebi.ac.uk.extended.model.ExtCollection
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.ExtSubmissionMethod
import ebi.ac.uk.extended.model.ExtTag
import java.time.ZoneOffset.UTC

class ToExtSubmissionMapper(
    private val toExtSectionMapper: ToExtSectionMapper,
) {
    internal suspend fun toExtSubmission(
        sub: DocSubmission,
        includeFileListFiles: Boolean,
        includeLinkListLinks: Boolean,
    ): ExtSubmission =
        ExtSubmission(
            accNo = sub.accNo,
            owner = sub.owner,
            submitter = sub.submitter,
            title = sub.title,
            doi = sub.doi,
            version = sub.version,
            schemaVersion = sub.schemaVersion,
            method = getMethod(sub.method),
            relPath = sub.relPath,
            rootPath = sub.rootPath,
            released = sub.released,
            secretKey = sub.secretKey,
            releaseTime = sub.releaseTime?.atOffset(UTC),
            submissionTime = sub.submissionTime.atOffset(UTC),
            modificationTime = sub.modificationTime.atOffset(UTC),
            creationTime = sub.creationTime.atOffset(UTC),
            section =
                toExtSectionMapper.toExtSection(
                    sub.section,
                    sub.accNo,
                    sub.version,
                    sub.released,
                    sub.relPath,
                    includeFileListFiles,
                    includeLinkListLinks,
                ),
            attributes = sub.attributes.toExtAttributes(),
            collections = sub.collections.map { ExtCollection(it.accNo) },
            tags = sub.tags.map { ExtTag(it.name, it.value) },
            pageTabFiles = sub.pageTabFiles.map { it.toExtFile(sub.released, sub.relPath) },
            storageMode = sub.storageMode,
        )

    private fun getMethod(method: DocSubmissionMethod) =
        when (method) {
            DocSubmissionMethod.FILE -> ExtSubmissionMethod.FILE
            DocSubmissionMethod.PAGE_TAB -> ExtSubmissionMethod.PAGE_TAB
            DocSubmissionMethod.UNKNOWN -> ExtSubmissionMethod.UNKNOWN
        }
}

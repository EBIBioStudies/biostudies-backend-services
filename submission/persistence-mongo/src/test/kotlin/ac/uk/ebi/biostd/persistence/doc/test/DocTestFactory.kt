package ac.uk.ebi.biostd.persistence.doc.test

import ac.uk.ebi.biostd.persistence.doc.model.DocAttribute
import ac.uk.ebi.biostd.persistence.doc.model.DocLink
import ac.uk.ebi.biostd.persistence.doc.model.DocLinkTable
import ac.uk.ebi.biostd.persistence.doc.model.DocProcessingStatus
import ac.uk.ebi.biostd.persistence.doc.model.DocProject
import ac.uk.ebi.biostd.persistence.doc.model.DocSection
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionMethod
import ac.uk.ebi.biostd.persistence.doc.model.DocTag
import arrow.core.Either
import java.time.Instant

class DocTestFactory {

    companion object {
        val time = Instant.now()

        val docSubmission = DocSubmission(
            id = null,
            accNo = "S-TEST123",
            version = 1,
            owner = "owner@mail.org",
            submitter = "submitter@mail.org",
            title = "Test Submission",
            method = DocSubmissionMethod.PAGE_TAB,
            relPath = "/a/rel/path",
            rootPath = "/a/root/path",
            released = false,
            secretKey = "a-secret-key",
            status = DocProcessingStatus.PROCESSED,
            releaseTime = time,
            modificationTime = time,
            creationTime = time,
            attributes = listOf(DocAttribute("AttachTo", "BioImages")),
            tags = listOf(DocTag("component", "web")),
            projects = listOf(DocProject("BioImages")),
            section = DocSection(
                type = "Study",
                links = listOf(Either.left(DocLink("url")), Either.right(DocLinkTable(listOf(DocLink("url2")))))
            )
        )
    }
}

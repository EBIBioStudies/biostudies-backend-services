package ac.uk.ebi.biostd.persistence.doc.test

import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtProcessingStatus.PROCESSED
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtStat
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.ExtSubmissionMethod.PAGE_TAB
import ebi.ac.uk.extended.model.ExtTag
import ebi.ac.uk.extended.model.Project
import java.time.OffsetDateTime
import java.time.ZoneOffset

fun extSubmission(released: Boolean): ExtSubmission {
    val releaseTime = OffsetDateTime.of(2019, 9, 21, 10, 30, 34, 15, ZoneOffset.UTC)
    val modificationTime = OffsetDateTime.of(2020, 9, 21, 10, 30, 34, 15, ZoneOffset.UTC)
    val creationTime = OffsetDateTime.of(2018, 9, 21, 10, 30, 34, 15, ZoneOffset.UTC)

    return ExtSubmission(
        accNo = "S-TEST1",
        version = 1,
        owner = "owner@mail.org",
        submitter = "submitter@mail.org",
        title = "TestSubmission",
        method = PAGE_TAB,
        relPath = "/a/rel/path",
        rootPath = "/a/root/path",
        released = released,
        secretKey = "a-secret-key",
        status = PROCESSED,
        releaseTime = releaseTime,
        modificationTime = modificationTime,
        creationTime = creationTime,
        attributes = listOf(ExtAttribute("AttachTo", "BioImages")),
        tags = listOf(ExtTag("component", "web")),
        projects = listOf(Project("BioImages")),
        section = ExtSection(type = "Study"),
        stats = listOf(ExtStat("component", "web"))
    )
}

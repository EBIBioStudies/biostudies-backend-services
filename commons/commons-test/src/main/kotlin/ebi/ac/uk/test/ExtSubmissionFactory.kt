package ebi.ac.uk.test

import ebi.ac.uk.extended.model.ExtProcessingStatus.PROCESSED
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.ExtSubmissionMethod.PAGE_TAB
import java.time.OffsetDateTime
import java.time.ZoneOffset.UTC

val basicExtSubmission = ExtSubmission(
    accNo = "S-TEST123",
    version = 1,
    schemaVersion = "1.0",
    title = "Test Submission",
    owner = "owner@email.org",
    submitter = "submitter@email.org",
    method = PAGE_TAB,
    relPath = "S-TEST/123/S-TEST123",
    rootPath = null,
    released = false,
    secretKey = "a-secret-key",
    status = PROCESSED,
    releaseTime = null,
    modificationTime = OffsetDateTime.of(2018, 9, 21, 0, 0, 0, 0, UTC),
    creationTime = OffsetDateTime.of(2018, 9, 21, 0, 0, 0, 0, UTC),
    attributes = emptyList(),
    tags = emptyList(),
    collections = emptyList(),
    section = ExtSection(type = "Study")
)

package ac.uk.ebi.biostd.persistence.filesystem

import arrow.core.Either.Companion.left
import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.extended.model.ExtProcessingStatus
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.ExtSubmissionMethod
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.extended.model.StorageMode
import java.io.File
import java.time.OffsetDateTime
import java.time.ZoneOffset

fun extSubmissionWithFileList(files: List<File>, referencedFiles: List<File>) =
    ExtSubmission(
        accNo = "ABC-123",
        version = 1,
        schemaVersion = "1.0",
        storageMode = StorageMode.NFS,
        title = "A Test Submission",
        owner = "owner",
        submitter = "submitter",
        method = ExtSubmissionMethod.PAGE_TAB,
        relPath = "ABC/ABCxxx123/ABC-123",
        rootPath = null,
        released = false,
        secretKey = "a-secret-key",
        status = ExtProcessingStatus.PROCESSED,
        releaseTime = null,
        modificationTime = OffsetDateTime.of(2018, 10, 10, 0, 0, 0, 0, ZoneOffset.UTC),
        creationTime = OffsetDateTime.of(2018, 10, 10, 0, 0, 0, 0, ZoneOffset.UTC),
        attributes = emptyList(),
        tags = emptyList(),
        collections = emptyList(),
        section = extSectionWithFileList(files, referencedFiles)
    )

fun extSectionWithFileList(files: List<File>, referencedFiles: List<File>) =
    ExtSection(
        type = "Study",
        files = files.map { left(NfsFile(it.name, "relPath", it.absolutePath, it, emptyList())) },
        fileList = ExtFileList(
            "fileList",
            referencedFiles.map { NfsFile(it.name, "relPath", it.absolutePath, it, emptyList()) }
        )
    )

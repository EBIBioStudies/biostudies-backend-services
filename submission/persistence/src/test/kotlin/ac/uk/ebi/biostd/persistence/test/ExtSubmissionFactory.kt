package ac.uk.ebi.biostd.persistence.test

import arrow.core.Either.Companion.left
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.model.SubmissionMethod
import ebi.ac.uk.model.constants.ProcessingStatus
import java.io.File
import java.time.OffsetDateTime
import java.time.ZoneOffset

fun extSubmissionWithFileList(files: List<File>, fileList: File, referencedFiles: List<File>) =
    ExtSubmission(
        accNo = "ABC-123",
        version = 1,
        title = "A Test Submission",
        method = SubmissionMethod.PAGE_TAB,
        relPath = "ABC/ABCxxx123/ABC-123",
        rootPath = null,
        released = false,
        secretKey = "a-secret-key",
        status = ProcessingStatus.PROCESSED,
        releaseTime = null,
        modificationTime = OffsetDateTime.of(2018, 10, 10, 0, 0, 0, 0, ZoneOffset.UTC),
        creationTime = OffsetDateTime.of(2018, 10, 10, 0, 0, 0, 0, ZoneOffset.UTC),
        attributes = emptyList(),
        tags = emptyList(),
        accessTags = emptyList(),
        section = extSectionWithFileList(files, fileList, referencedFiles))

fun extSectionWithFileList(files: List<File>, fileList: File, referencedFiles: List<File>) =
    ExtSection(
        type = "Study",
        files = files.map { left(ExtFile(it.name, it, emptyList())) },
        fileList = ExtFileList("fileList", fileList, referencedFiles.map { ExtFile(it.name, it, emptyList()) }))

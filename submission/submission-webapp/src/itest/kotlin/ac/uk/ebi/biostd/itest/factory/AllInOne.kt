package ac.uk.ebi.biostd.itest.factory

import ebi.ac.uk.base.EMPTY
import ebi.ac.uk.test.createOrReplaceFile
import io.github.glytching.junit.extension.folder.TemporaryFolder
import java.io.File

data class SubmissionSpec(val submission: File, val fileList: File, val files: List<SubmissionFile> = emptyList())
data class SubmissionFile(val file: File, val folder: String = EMPTY)

fun submissionsFiles(tempFolder: TemporaryFolder): List<SubmissionFile> = listOf(
    SubmissionFile(tempFolder.createOrReplaceFile("DataFile1.txt")),
    SubmissionFile(tempFolder.createOrReplaceFile("DataFile2.txt")),
    SubmissionFile(tempFolder.createOrReplaceFile("DataFile3.txt"), "Folder1"),
    SubmissionFile(tempFolder.createOrReplaceFile("DataFile4.txt"), "Folder1/Folder2"),
    SubmissionFile(tempFolder.createOrReplaceFile("DataFile5.txt")),
    SubmissionFile(tempFolder.createOrReplaceFile("DataFile6.txt"), "Folder1"),
)
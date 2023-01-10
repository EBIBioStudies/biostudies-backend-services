package ac.uk.ebi.biostd.itest.factory

import ebi.ac.uk.base.EMPTY
import ebi.ac.uk.io.ext.createOrReplaceFile
import java.io.File

data class SubmissionSpec(
    val submission: File,
    val fileList: File,
    val files: List<SubmissionFile> = emptyList(),
    val subFileList: SubmissionFile? = null
)

data class SubmissionFile(val file: File, val folder: String = EMPTY)

fun submissionsFiles(tempFolder: File): List<SubmissionFile> = listOf(
    SubmissionFile(tempFolder.createOrReplaceFile("DataFile1.txt", "content 1")),
    SubmissionFile(tempFolder.createOrReplaceFile("DataFile2.txt", "content 2")),
    SubmissionFile(tempFolder.createOrReplaceFile("DataFile3.txt", "content 3"), "Folder1"),
    SubmissionFile(tempFolder.createOrReplaceFile("DataFile4.txt", "content 4"), "Folder1/Folder2"),
    SubmissionFile(tempFolder.createOrReplaceFile("DataFile5.txt", "content 5")),
    SubmissionFile(tempFolder.createOrReplaceFile("DataFile6.txt", "content 6"), "Folder1"),
    SubmissionFile(tempFolder.createOrReplaceFile("DataFile7.txt", "content 7")),
)

package ebi.ac.uk.extended.model

import arrow.core.Either.Companion.left
import ebi.ac.uk.io.ext.md5
import ebi.ac.uk.io.ext.size
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.OffsetDateTime
import java.time.ZoneOffset

@ExtendWith(TemporaryFolderExtension::class)
class ExtSubmissionExtensionsTest(
    tempFolder: TemporaryFolder
) {
    private val innerFile = tempFolder.createFile("file.txt")
    private val referencedFile = tempFolder.createFile("referenced.txt")

    @Test
    fun computedTitle() {
        val submissionTitle = testSubmission(subTitle = "submission title", secTitle = null)
        val submissionNoTitleSecTitle = testSubmission(subTitle = null, secTitle = "section title")
        val submissionNoTitleNoSecTitle = testSubmission(subTitle = null, secTitle = null)

        assertThat(submissionTitle.computedTitle).isEqualTo("submission title")
        assertThat(submissionNoTitleSecTitle.computedTitle).isEqualTo("section title")
        assertThat(submissionNoTitleNoSecTitle.computedTitle).isNull()
    }

    @Test
    fun `get all submission files`() {
        val innerExtFile = NfsFile(
            "my-folder/file.txt",
            "Files/my-folder/file.txt",
            innerFile,
            innerFile.absolutePath,
            innerFile.md5(),
            innerFile.size(),
            listOf()
        )
        val referencedExtFile = NfsFile(
            "my-folder/referenced.txt",
            "Files/my-folder/referenced.txt",
            referencedFile,
            referencedFile.absolutePath,
            referencedFile.md5(),
            referencedFile.size(),
            listOf()
        )
        val submission = testSubmission("Test Submission").copy(
            section = ExtSection(
                type = "Study",
                files = listOf(left(innerExtFile)),
                fileList = ExtFileList("a/file-list", listOf(referencedExtFile))
            )
        )

        val files = submission.allFiles().toList()

        assertThat(files).hasSize(2)
        assertThat(files).containsOnly(innerExtFile, referencedExtFile)
    }

    private fun testSubmission(subTitle: String? = null, secTitle: String? = null): ExtSubmission = ExtSubmission(
        accNo = "S-TEST1",
        version = 1,
        schemaVersion = "1.0",
        owner = "owner@mail.org",
        storageMode = StorageMode.FIRE,
        submitter = "submitter@mail.org",
        title = subTitle,
        method = ExtSubmissionMethod.PAGE_TAB,
        relPath = "/a/rel/path",
        rootPath = null,
        releaseTime = null,
        released = true,
        secretKey = "a-secret-key",
        status = ExtProcessingStatus.PROCESSED,
        modificationTime = OffsetDateTime.of(2020, 9, 21, 10, 30, 34, 15, ZoneOffset.UTC),
        creationTime = OffsetDateTime.of(2020, 9, 21, 10, 30, 34, 15, ZoneOffset.UTC),
        attributes = listOf(),
        section = ExtSection(
            type = "Study",
            attributes = secTitle?.let { listOf(ExtAttribute("Title", it)) } ?: listOf()
        )
    )
}

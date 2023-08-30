package ebi.ac.uk.extended.model

import arrow.core.Either.Companion.left
import ebi.ac.uk.util.collections.second
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.extended.serialization.service.createExtFileList
import java.time.OffsetDateTime
import java.time.ZoneOffset

@ExtendWith(TemporaryFolderExtension::class)
class ExtSubmissionExtensionsTest(
    private val tempFolder: TemporaryFolder
) {
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
        val innerFile = tempFolder.createFile("file.txt")
        val pagetabFile = tempFolder.createFile("S-TEST1.tsv")
        val refFile = tempFolder.createFile("referenced.txt")
        val pagetabExtFile = createNfsFile("S-TEST1.tsv", "S-TEST1.tsv", pagetabFile)
        val innerExtFile = createNfsFile("my-folder/file.txt", "Files/my-folder/file.txt", innerFile)
        val referencedExtFile = createNfsFile("my-folder/referenced.txt", "Files/my-folder/referenced.txt", refFile)
        val fileList = ExtFileList("a/file-list", createExtFileList(referencedExtFile))
        val submission = testSubmission("Test Submission").copy(
            pageTabFiles = listOf(pagetabExtFile),
            section = ExtSection(
                type = "Study",
                files = listOf(left(innerExtFile)),
                fileList = fileList,
                sections = listOf(left(ExtSection(type = "Exp")))
            )
        )

        val sectionFiles = submission.allSectionsFiles
        assertThat(sectionFiles).hasSize(1)
        assertThat(sectionFiles.first()).isEqualTo(innerExtFile)

        val fileLists = submission.allFileList
        assertThat(fileLists).hasSize(1)
        assertThat(fileLists.first()).isEqualTo(fileList)

        val allInnerSubmissionFiles = submission.allInnerSubmissionFiles
        assertThat(allInnerSubmissionFiles).hasSize(2)
        assertThat(allInnerSubmissionFiles.first()).isEqualTo(innerExtFile)
        assertThat(allInnerSubmissionFiles.second()).isEqualTo(pagetabExtFile)
    }

    private fun testSubmission(subTitle: String? = null, secTitle: String? = null): ExtSubmission = ExtSubmission(
        accNo = "S-TEST1",
        version = 1,
        schemaVersion = "1.0",
        owner = "owner@mail.org",
        storageMode = StorageMode.FIRE,
        submitter = "submitter@mail.org",
        title = subTitle,
        doi = "10.983/S-TEST1",
        method = ExtSubmissionMethod.PAGE_TAB,
        relPath = "/a/rel/path",
        rootPath = null,
        releaseTime = null,
        released = true,
        secretKey = "a-secret-key",
        modificationTime = OffsetDateTime.of(2020, 9, 21, 10, 30, 34, 15, ZoneOffset.UTC),
        creationTime = OffsetDateTime.of(2020, 9, 21, 10, 30, 34, 15, ZoneOffset.UTC),
        attributes = listOf(),
        section = ExtSection(
            type = "Study",
            attributes = secTitle?.let { listOf(ExtAttribute("Title", it)) } ?: listOf()
        )
    )
}

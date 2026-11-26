package uk.ac.ebi.biostd.extended.serialization.service

import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.biostd.base.Either.Companion.left
import uk.ac.ebi.biostd.base.Either.Companion.right
import uk.ac.ebi.biostd.extended.model.ExtAttribute
import uk.ac.ebi.biostd.extended.model.ExtCollection
import uk.ac.ebi.biostd.extended.model.ExtFileList
import uk.ac.ebi.biostd.extended.model.ExtFileTable
import uk.ac.ebi.biostd.extended.model.ExtFileType.FILE
import uk.ac.ebi.biostd.extended.model.ExtSection
import uk.ac.ebi.biostd.extended.model.ExtSubmission
import uk.ac.ebi.biostd.extended.model.ExtSubmissionMethod
import uk.ac.ebi.biostd.extended.model.ExtTag
import uk.ac.ebi.biostd.extended.model.FireFile
import uk.ac.ebi.biostd.extended.model.StorageMode
import java.io.File
import java.time.OffsetDateTime

@ExtendWith(TemporaryFolderExtension::class)
internal class ExtSerializationServiceExtTest(
    private val tmpFolder: TemporaryFolder,
) {
    private val testInstance: ExtSerializationService = ExtSerializationService()

    @Test
    fun fileFlow() =
        runTest {
            val fileList1 = tmpFolder.createFile("f1.json")
            val files = (4..1000).map { createFireFile(it) }

            val pageTabFile = createFireFile(1)
            val sectionFile = createFireFile(2)
            val sectionTableFile = createFireFile(3)

            val submission = createTestSubmission(fileList1, pageTabFile, sectionFile, sectionTableFile)
            val filesCount = testInstance.serialize(files.asSequence(), fileList1.outputStream())

            val result = testInstance.filesFlow(submission).toList()
            assertThat(filesCount).isEqualTo(files.size)
            assertThat(result).containsExactlyInAnyOrder(
                pageTabFile,
                sectionFile,
                sectionTableFile,
                *files.toTypedArray(),
            )
        }

    private fun createFireFile(idx: Int): FireFile =
        FireFile(
            "fireId-$idx",
            "firePath-$idx",
            false,
            "folder/myFile$idx",
            "Files/folder/myFile$idx",
            "md5-$idx",
            12,
            FILE,
            emptyList(),
        )

    private fun createTestSubmission(
        fileList: File,
        pageTabFile: FireFile,
        sectionFile: FireFile,
        sectionTableFile: FireFile,
    ): ExtSubmission =
        ExtSubmission(
            accNo = "S-TEST1",
            version = 1,
            schemaVersion = "1.0",
            storageMode = StorageMode.NFS,
            owner = "owner@mail.org",
            submitter = "submitter@mail.org",
            title = "TestSubmission",
            doi = "10.983/S-TEST1",
            method = ExtSubmissionMethod.PAGE_TAB,
            relPath = "/a/rel/path",
            rootPath = "/a/root/path",
            released = true,
            secretKey = "a-secret-key",
            releaseTime = OffsetDateTime.now(),
            submissionTime = OffsetDateTime.now(),
            modificationTime = OffsetDateTime.now(),
            creationTime = OffsetDateTime.now(),
            attributes = listOf(ExtAttribute("AttachTo", "BioImages")),
            tags = listOf(ExtTag("component", "web")),
            collections = listOf(ExtCollection("BioImages")),
            section =
                ExtSection(
                    type = "Study",
                    fileList = ExtFileList("path", file = fileList),
                    files = listOf(left(sectionFile), right(ExtFileTable(sectionTableFile))),
                ),
            pageTabFiles = listOf(pageTabFile),
        )
}

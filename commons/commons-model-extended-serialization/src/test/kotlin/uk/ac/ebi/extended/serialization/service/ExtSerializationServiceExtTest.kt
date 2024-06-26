package uk.ac.ebi.extended.serialization.service

import ebi.ac.uk.base.Either.Companion.left
import ebi.ac.uk.base.Either.Companion.right
import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtCollection
import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.extended.model.ExtFileTable
import ebi.ac.uk.extended.model.ExtFileType.FILE
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.ExtSubmissionMethod
import ebi.ac.uk.extended.model.ExtTag
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.StorageMode
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
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

    private fun createFireFile(idx: Int): FireFile {
        return FireFile(
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
    }

    private fun createTestSubmission(
        fileList: File,
        pageTabFile: FireFile,
        sectionFile: FireFile,
        sectionTableFile: FireFile,
    ): ExtSubmission {
        return ExtSubmission(
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
}

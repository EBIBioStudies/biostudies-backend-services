package uk.ac.ebi.extended.serialization.service

import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtCollection
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.ExtSubmissionMethod.PAGE_TAB
import ebi.ac.uk.extended.model.ExtTag
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.extended.model.StorageMode
import ebi.ac.uk.io.ext.md5
import ebi.ac.uk.io.ext.size
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.OffsetDateTime
import java.time.ZoneOffset

@ExtendWith(TemporaryFolderExtension::class)
class ExtSerializationServiceTest(private val tempFolder: TemporaryFolder) {
    private val testInstance = ExtSerializationService()
    private val testFile = tempFolder.createFile("results.txt")
    private val nfsFile = tempFolder.createFile("file.txt")

    @Test
    fun `serialize - deserialize`() {
        val time = OffsetDateTime.of(2019, 9, 21, 10, 30, 34, 15, ZoneOffset.UTC)
        val extSubmission =
            ExtSubmission(
                accNo = "S-TEST123",
                version = 1,
                schemaVersion = "1.0",
                storageMode = StorageMode.NFS,
                owner = "owner@mail.org",
                submitter = "submitter@mail.org",
                title = "Test Submission",
                doi = "10.983/S-TEST123",
                method = PAGE_TAB,
                relPath = "/a/rel/path",
                rootPath = "/a/root/path",
                released = false,
                secretKey = "a-secret-key",
                releaseTime = time,
                modificationTime = time,
                creationTime = time,
                attributes = listOf(ExtAttribute("AttachTo", "BioImages")),
                tags = listOf(ExtTag("component", "web")),
                collections = listOf(ExtCollection("BioImages")),
                section = ExtSection(type = "Study"),
            )

        val serialized = testInstance.serialize(extSubmission)
        val deserialized = testInstance.deserialize(serialized)

        assertThat(deserialized).isEqualTo(extSubmission)
    }

    @Test
    fun `serialize - deserialize fileList`() =
        runTest {
            val fileList = (1..20_000).map { createNfsFile(it) }.asSequence()
            val iterator = fileList.iterator()

            testFile.outputStream().use { testInstance.serialize(fileList, it) }

            val result = testFile.inputStream().use { testInstance.deserializeListAsFlow(it).toList() }
            result.onEach { assertThat(it).isEqualTo(iterator.next()) }
        }

    private fun createNfsFile(index: Int) =
        NfsFile(
            filePath = "folder$index/file.txt",
            relPath = "Files/folder$index/file.txt",
            fullPath = nfsFile.absolutePath,
            file = nfsFile,
            attributes = (1..4).map { ExtAttribute(name = "name$it-file$index", value = "value$it-file$index") },
            md5 = nfsFile.md5(),
            size = nfsFile.size(),
        )
}

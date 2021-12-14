package uk.ac.ebi.extended.serialization.service

import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtCollection
import ebi.ac.uk.extended.model.ExtProcessingStatus.PROCESSED
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.ExtSubmissionMethod.PAGE_TAB
import ebi.ac.uk.extended.model.ExtTag
import ebi.ac.uk.extended.model.StorageMode
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.extended.test.AttributeFactory.defaultAttribute
import uk.ac.ebi.extended.test.FireDirectoryFactory.defaultFireDirectory
import uk.ac.ebi.extended.test.FireFileFactory.defaultFireFile
import uk.ac.ebi.extended.test.NfsFileFactory.defaultNfsFile
import java.time.OffsetDateTime
import java.time.ZoneOffset

@ExtendWith(TemporaryFolderExtension::class)
class ExtSerializationServiceTest(private val tempFolder: TemporaryFolder) {
    private val testInstance = ExtSerializationService()
    private val testFile = tempFolder.createFile("results.txt")
    private val nfsFile = tempFolder.createFile("file.txt")
    private val attributes = listOf(
        defaultAttribute("name1"),
        defaultAttribute("name2"),
        defaultAttribute("name3"),
        defaultAttribute("name4")
    )
    private fun fireFile(fireId: String) = defaultFireFile(fireId = fireId, attributes = attributes)
    private fun fireDirectory(filePath: String) = defaultFireDirectory(filePath = filePath, attributes = attributes)
    private fun nfsFile(filePath: String) = defaultNfsFile(filePath = filePath, file = nfsFile, attributes = attributes)

    @Test
    fun `serialize - deserialize`() {
        val time = OffsetDateTime.of(2019, 9, 21, 10, 30, 34, 15, ZoneOffset.UTC)
        val extSubmission = ExtSubmission(
            accNo = "S-TEST123",
            version = 1,
            schemaVersion = "1.0",
            storageMode = StorageMode.NFS,
            owner = "owner@mail.org",
            submitter = "submitter@mail.org",
            title = "Test Submission",
            method = PAGE_TAB,
            relPath = "/a/rel/path",
            rootPath = "/a/root/path",
            released = false,
            secretKey = "a-secret-key",
            status = PROCESSED,
            releaseTime = time,
            modificationTime = time,
            creationTime = time,
            attributes = listOf(ExtAttribute("AttachTo", "BioImages")),
            tags = listOf(ExtTag("component", "web")),
            collections = listOf(ExtCollection("BioImages")),
            section = ExtSection(type = "Study")
        )

        val serialized = testInstance.serialize(extSubmission)
        val deserialized = testInstance.deserialize<ExtSubmission>(serialized)

        assertThat(deserialized).isEqualTo(extSubmission)
    }

    @Test
    fun `serialize - deserialize fileList`() {
        val fileCount = 50000
        val fileList = (1..fileCount).map { fireFile(fireId = "$it") }
            .plus((1..fileCount).map { fireDirectory(filePath = "folder$it/file.txt") })
            .plus((1..fileCount).map { defaultNfsFile(filePath = "folder$it/file.txt", file = nfsFile, attributes = attributes) })
            .asSequence()
        val iterator = fileList.iterator()

        testInstance.serializeFileList(fileList, testFile.outputStream())

        testInstance.deserializeFileList(testFile.inputStream()).forEach { file ->
            assertThat(file).isEqualTo(iterator.next())
        }

        testFile.outputStream().close()
        testFile.inputStream().close()
    }
}

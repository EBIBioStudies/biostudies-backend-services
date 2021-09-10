package ac.uk.ebi.biostd.persistence.pagetab

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat
import ac.uk.ebi.biostd.persistence.filesystem.pagetab.FirePageTabService
import arrow.core.Either.Companion.left
import arrow.core.Either.Companion.right
import ebi.ac.uk.asserts.assertThat
import ebi.ac.uk.extended.mapping.to.toFilesTable
import ebi.ac.uk.extended.mapping.to.toSimpleSubmission
import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSectionTable
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.io.RW_R_____
import ebi.ac.uk.test.basicExtSubmission
import ebi.ac.uk.util.collections.second
import ebi.ac.uk.util.collections.third
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.fire.client.integration.web.FireWebClient
import java.io.File
import java.nio.file.Files
import org.assertj.core.api.Assertions.assertThat as assertJThat
import uk.ac.ebi.fire.client.model.FireFile as FireWebFile

@ExtendWith(TemporaryFolderExtension::class, MockKExtension::class)
class FirePageTabServiceTest(
    tempFolder: TemporaryFolder,
    @MockK private val serializationService: SerializationService,
    @MockK private val fireWebClient: FireWebClient
) {
    private val fireFolder = tempFolder.root.resolve("fire-temp")
    private val testInstance: FirePageTabService = FirePageTabService(fireFolder, serializationService, fireWebClient)

    @Test
    fun `generate page tab`() {
        val submission = basicExtSubmission.copy(section = initialRootSection())
        setUpSerializer(submission.section.fileList!!.toFilesTable())
        setUpSerializer(submission.toSimpleSubmission())
        setUpFireWebClient()

        val result = testInstance.generatePageTab(submission)

        assertSubmission(result)
        assertRootSection(result.section)
        assertThat(result.section.sections.first()).hasLeftValueSatisfying { assertSubSection(it) }
        verifyFileLists(fireFolder)
        verifySubmissionFiles(fireFolder)
    }

    private fun setUpSerializer(element: Any) {
        every { serializationService.serializeElement(element, SubFormat.JSON_PRETTY) } returns "json"
        every { serializationService.serializeElement(element, SubFormat.XML) } returns "xml"
        every { serializationService.serializeElement(element, SubFormat.TSV) } returns "tsv"
    }

    private fun setUpFireWebClient() {
        every { fireWebClient.save(any(), any(), any()) } returns
            FireWebFile(1, "$FILE_LIST_JSON2-fireId", "md5", 1, "creationTime") andThen
            FireWebFile(2, "$FILE_LIST_XML2-fireId", "md5", 1, "creationTime") andThen
            FireWebFile(3, "$FILE_LIST_TSV2-fireId", "md5", 1, "creationTime") andThen
            FireWebFile(4, "$FILE_LIST_JSON1-fireId", "md5", 1, "creationTime") andThen
            FireWebFile(5, "$FILE_LIST_XML1-fireId", "md5", 1, "creationTime") andThen
            FireWebFile(6, "$FILE_LIST_TSV1-fireId", "md5", 1, "creationTime") andThen
            FireWebFile(7, "$SUB_JSON-fireId", "md5", 1, "creationTime") andThen
            FireWebFile(8, "$SUB_XML-fireId", "md5", 1, "creationTime") andThen
            FireWebFile(9, "$SUB_TSV-fireId", "md5", 1, "creationTime")
    }

    private fun initialRootSection() = ExtSection(
        type = "Study1",
        fileList = ExtFileList("data/file-list1"),
        sections = listOf(
            left(ExtSection(type = "Study2", fileList = ExtFileList("data/file-list2"))),
            right(ExtSectionTable(listOf(ExtSection(type = "Study3"))))
        )
    )

    private fun assertSubmission(submission: ExtSubmission) {
        val tabFiles = submission.tabFiles
        assertJThat(tabFiles.first()).isEqualTo(FireFile(SUB_JSON, "$SUB_JSON-fireId", "md5", 1, listOf()))
        assertJThat(tabFiles.second()).isEqualTo(FireFile(SUB_XML, "$SUB_XML-fireId", "md5", 1, listOf()))
        assertJThat(tabFiles.third()).isEqualTo(FireFile(SUB_TSV, "$SUB_TSV-fireId", "md5", 1, listOf()))
    }

    private fun assertRootSection(section: ExtSection) {
        val tabFiles = section.fileList!!.tabFiles
        assertJThat(tabFiles.first()).isEqualTo(FireFile(FILE_LIST_JSON1, "$FILE_LIST_JSON1-fireId", "md5", 1, listOf()))
        assertJThat(tabFiles.second()).isEqualTo(FireFile(FILE_LIST_XML1, "$FILE_LIST_XML1-fireId", "md5", 1, listOf()))
        assertJThat(tabFiles.third()).isEqualTo(FireFile(FILE_LIST_TSV1, "$FILE_LIST_TSV1-fireId", "md5", 1, listOf()))
    }

    private fun assertSubSection(section: ExtSection) {
        val tabFiles = section.fileList!!.tabFiles
        assertJThat(tabFiles.first()).isEqualTo(FireFile(FILE_LIST_JSON2, "$FILE_LIST_JSON2-fireId", "md5", 1, listOf()))
        assertJThat(tabFiles.second()).isEqualTo(FireFile(FILE_LIST_XML2, "$FILE_LIST_XML2-fireId", "md5", 1, listOf()))
        assertJThat(tabFiles.third()).isEqualTo(FireFile(FILE_LIST_TSV2, "$FILE_LIST_TSV2-fireId", "md5", 1, listOf()))
    }

    private fun verifyFileLists(fireFolder: File) {
        assertPageTabFile(fireFolder.resolve("data/$FILE_LIST_JSON1"))
        assertPageTabFile(fireFolder.resolve("data/$FILE_LIST_XML1"))
        assertPageTabFile(fireFolder.resolve("data/$FILE_LIST_TSV1"))

        assertPageTabFile(fireFolder.resolve("data/$FILE_LIST_JSON2"))
        assertPageTabFile(fireFolder.resolve("data/$FILE_LIST_XML2"))
        assertPageTabFile(fireFolder.resolve("data/$FILE_LIST_TSV2"))
    }

    private fun verifySubmissionFiles(fireFolder: File) {
        assertPageTabFile(fireFolder.resolve(SUB_JSON))
        assertPageTabFile(fireFolder.resolve(SUB_XML))
        assertPageTabFile(fireFolder.resolve(SUB_TSV))
    }

    private fun assertPageTabFile(file: File) {
        assertJThat(file).exists()
        assertJThat(Files.getPosixFilePermissions(file.toPath())).containsExactlyInAnyOrderElementsOf(RW_R_____)
    }

    companion object {
        const val SUB_JSON = "S-TEST123.json"
        const val SUB_XML = "S-TEST123.xml"
        const val SUB_TSV = "S-TEST123.pagetab.tsv"

        const val FILE_LIST_JSON1 = "file-list1.json"
        const val FILE_LIST_XML1 = "file-list1.xml"
        const val FILE_LIST_TSV1 = "file-list1.pagetab.tsv"

        const val FILE_LIST_JSON2 = "file-list2.json"
        const val FILE_LIST_XML2 = "file-list2.xml"
        const val FILE_LIST_TSV2 = "file-list2.pagetab.tsv"
    }
}

package ac.uk.ebi.biostd.persistence.pagetab

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat
import ac.uk.ebi.biostd.persistence.filesystem.pagetab.FirePageTabService
import arrow.core.Either.Companion.left
import arrow.core.Either.Companion.right
import ebi.ac.uk.extended.mapping.to.toFilesTable
import ebi.ac.uk.extended.mapping.to.toSimpleSubmission
import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSectionTable
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.io.RW_R_____
import uk.ac.ebi.fire.client.model.FireFile as FireWebFile
import ebi.ac.uk.test.basicExtSubmission
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.fire.client.integration.web.FireWebClient
import java.io.File
import java.nio.file.Files

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

        assertThat(result).isEqualTo(submission.copy(pageTabFiles = subPageTabFiles(), section = finalRootSection()))
        verifyFileLists(fireFolder)
        verifySubmissionFiles(fireFolder)
    }

    private fun setUpSerializer(element: Any) {
        every { serializationService.serializeElement(element, SubFormat.JSON_PRETTY) } returns "json"
        every { serializationService.serializeElement(element, SubFormat.XML) } returns "xml"
        every { serializationService.serializeElement(element, SubFormat.TSV) } returns "tsv"
    }

    private fun setUpFireWebClient() {
        every { fireWebClient.save(any(), any(), "S-TEST/123/S-TEST123") } returns
                FireWebFile(1, "$SUB_JSON-fireId", "md5", 1, "creationTime") andThen
                FireWebFile(2, "$SUB_XML-fireId", "md5", 1, "creationTime") andThen
                FireWebFile(3, "$SUB_TSV-fireId", "md5", 1, "creationTime") andThen
                FireWebFile(4, "$FILE_LIST_JSON1-fireId", "md5", 1, "creationTime") andThen
                FireWebFile(5, "$FILE_LIST_XML1-fireId", "md5", 1, "creationTime") andThen
                FireWebFile(6, "$FILE_LIST_TSV1-fireId", "md5", 1, "creationTime") andThen
                FireWebFile(7, "$FILE_LIST_JSON2-fireId", "md5", 1, "creationTime") andThen
                FireWebFile(8, "$FILE_LIST_XML2-fireId", "md5", 1, "creationTime") andThen
                FireWebFile(9, "$FILE_LIST_TSV2-fireId", "md5", 1, "creationTime") andThen
                FireWebFile(10, "$FILE_LIST_JSON3-fireId", "md5", 1, "creationTime") andThen
                FireWebFile(11, "$FILE_LIST_XML3-fireId", "md5", 1, "creationTime") andThen
                FireWebFile(12, "$FILE_LIST_TSV3-fireId", "md5", 1, "creationTime")
    }

    private fun subPageTabFiles() = listOf(
        FireFile(SUB_JSON, "$SUB_JSON-fireId", "md5", 1, listOf()),
        FireFile(SUB_XML, "$SUB_XML-fireId", "md5", 1, listOf()),
        FireFile(SUB_TSV, "$SUB_TSV-fireId", "md5", 1, listOf())
    )

    private val fileListRootSection = ExtFileList("data/file-list1")
    private val fileListSubSection = ExtFileList("data/file-list2")
    private val fileListSubSectionTable = ExtFileList("data/file-list3")

    private fun initialRootSection() = ExtSection(
        type = "Study1",
        fileList = fileListRootSection,
        sections = listOf(
            left(ExtSection(type = "Study2", fileList = fileListSubSection)),
            right(ExtSectionTable(listOf(ExtSection(type = "Study3", fileList = fileListSubSectionTable))))
        )
    )

    private fun finalRootSection() = ExtSection(
        type = "Study1",
        fileList = fileListRootSection.copy(pageTabFiles = filesRootSection()),
        sections = listOf(
            left(ExtSection(type = "Study2", fileList = fileListSubSection.copy(pageTabFiles = filesSubSection()))),
            right(
                ExtSectionTable(
                    listOf(
                        ExtSection(
                            type = "Study3",
                            fileList = fileListSubSectionTable.copy(pageTabFiles = filesSubSectionTable())
                        )
                    )
                )
            )
        )
    )

    private fun filesRootSection() = listOf(
        FireFile(FILE_LIST_JSON1, "$FILE_LIST_JSON1-fireId", "md5", 1, listOf()),
        FireFile(FILE_LIST_XML1, "$FILE_LIST_XML1-fireId", "md5", 1, listOf()),
        FireFile(FILE_LIST_TSV1, "$FILE_LIST_TSV1-fireId", "md5", 1, listOf())
    )

    private fun filesSubSection() = listOf(
        FireFile(FILE_LIST_JSON2, "$FILE_LIST_JSON2-fireId", "md5", 1, listOf()),
        FireFile(FILE_LIST_XML2, "$FILE_LIST_XML2-fireId", "md5", 1, listOf()),
        FireFile(FILE_LIST_TSV2, "$FILE_LIST_TSV2-fireId", "md5", 1, listOf())
    )

    private fun filesSubSectionTable() = listOf(
        FireFile(FILE_LIST_JSON3, "$FILE_LIST_JSON3-fireId", "md5", 1, listOf()),
        FireFile(FILE_LIST_XML3, "$FILE_LIST_XML3-fireId", "md5", 1, listOf()),
        FireFile(FILE_LIST_TSV3, "$FILE_LIST_TSV3-fireId", "md5", 1, listOf())
    )

    private fun verifyFileLists(fireFolder: File) {
        assertPageTabFile(fireFolder.resolve("data/${FILE_LIST_JSON1}"))
        assertPageTabFile(fireFolder.resolve("data/${FILE_LIST_XML1}"))
        assertPageTabFile(fireFolder.resolve("data/${FILE_LIST_TSV1}"))

        assertPageTabFile(fireFolder.resolve("data/${FILE_LIST_JSON2}"))
        assertPageTabFile(fireFolder.resolve("data/${FILE_LIST_XML2}"))
        assertPageTabFile(fireFolder.resolve("data/${FILE_LIST_TSV2}"))

        assertPageTabFile(fireFolder.resolve("data/${FILE_LIST_JSON3}"))
        assertPageTabFile(fireFolder.resolve("data/${FILE_LIST_XML3}"))
        assertPageTabFile(fireFolder.resolve("data/${FILE_LIST_TSV3}"))
    }

    private fun verifySubmissionFiles(fireFolder: File) {
        assertPageTabFile(fireFolder.resolve(SUB_JSON))
        assertPageTabFile(fireFolder.resolve(SUB_XML))
        assertPageTabFile(fireFolder.resolve(SUB_TSV))
    }

    private fun assertPageTabFile(file: File) {
        assertThat(file).exists()
        assertThat(Files.getPosixFilePermissions(file.toPath())).containsExactlyInAnyOrderElementsOf(RW_R_____)
    }
}

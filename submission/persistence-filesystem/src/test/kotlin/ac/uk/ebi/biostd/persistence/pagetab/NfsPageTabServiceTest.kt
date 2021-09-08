package ac.uk.ebi.biostd.persistence.pagetab

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat.Companion.JSON_PRETTY
import ac.uk.ebi.biostd.integration.SubFormat.Companion.TSV
import ac.uk.ebi.biostd.integration.SubFormat.Companion.XML
import ac.uk.ebi.biostd.persistence.filesystem.pagetab.NfsPageTabService
import arrow.core.Either
import arrow.core.Either.Companion.left
import ebi.ac.uk.extended.mapping.to.toFilesTable
import ebi.ac.uk.extended.mapping.to.toSimpleSubmission
import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSectionTable
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.io.RW_R_____
import ebi.ac.uk.paths.SubmissionFolderResolver
import ebi.ac.uk.test.basicExtSubmission
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

@ExtendWith(MockKExtension::class, TemporaryFolderExtension::class)
class NfsPageTabServiceTest(
    tempFolder: TemporaryFolder,
    @MockK private val serializationService: SerializationService
) {
    private val rootPath = tempFolder.root
    private val folderResolver = SubmissionFolderResolver(Paths.get("$rootPath/submission"), Paths.get("$rootPath/ftp"))
    private val testInstance = NfsPageTabService(folderResolver, serializationService)
    private val subFolder = tempFolder.root.resolve("submission/S-TEST/123/S-TEST123")

    @Test
    fun `generate page tab`() {
        val submission = basicExtSubmission.copy(section = initialRootSection())

        setUpSerializer(submission.section.fileList!!.toFilesTable())
        setUpSerializer(submission.toSimpleSubmission())

        assertThat(testInstance.generatePageTab(submission))
            .isEqualTo(submission.copy(tabFiles = pageTabFiles(), section = finalRootSection()))

        verifyFileLists(subFolder)
        verifySubmissionFiles(subFolder)
    }

    private fun setUpSerializer(element: Any) {
        every { serializationService.serializeElement(element, JSON_PRETTY) } returns "json"
        every { serializationService.serializeElement(element, XML) } returns "xml"
        every { serializationService.serializeElement(element, TSV) } returns "tsv"
    }

    private val fileListRootSection = ExtFileList("data/file-list1")
    private val fileListSubSection = ExtFileList("data/file-list2")
    private val fileListSubSectionTable = ExtFileList("data/file-list3")

    private fun initialRootSection() = ExtSection(
        type = "Study1",
        fileList = fileListRootSection,
        sections = listOf(
            left(ExtSection(type = "Study2", fileList = fileListSubSection)),
            Either.right(ExtSectionTable(listOf(ExtSection(type = "Study3", fileList = fileListSubSectionTable))))
        )
    )

    private fun finalRootSection() = ExtSection(
        type = "Study1",
        fileList = fileListRootSection.copy(tabFiles = filesRootSection()),
        sections = listOf(
            left(ExtSection(type = "Study2", fileList = fileListSubSection.copy(tabFiles = filesSubSection()))),
            Either.right(ExtSectionTable(listOf(ExtSection(type = "Study3", fileList = fileListSubSectionTable))))
        )
    )

    private fun pageTabFiles() = listOf(
        NfsFile(SUB_JSON, subFolder.resolve(SUB_JSON)),
        NfsFile(SUB_XML, subFolder.resolve(SUB_XML)),
        NfsFile(SUB_TSV, subFolder.resolve(SUB_TSV))
    )

    private fun filesRootSection() = listOf(
        NfsFile(FILE_LIST_JSON1, subFolder.resolve("data/${FILE_LIST_JSON1}")),
        NfsFile(FILE_LIST_XML1, subFolder.resolve("data/${FILE_LIST_XML1}")),
        NfsFile(FILE_LIST_TSV1, subFolder.resolve("data/${FILE_LIST_TSV1}"))
    )

    private fun filesSubSection() = listOf(
        NfsFile(FILE_LIST_JSON2, subFolder.resolve("data/${FILE_LIST_JSON2}")),
        NfsFile(FILE_LIST_XML2, subFolder.resolve("data/${FILE_LIST_XML2}")),
        NfsFile(FILE_LIST_TSV2, subFolder.resolve("data/${FILE_LIST_TSV2}"))
    )

    private fun verifyFileLists(submissionFolder: File) {
        assertPageTabFile(submissionFolder.resolve("data/${FILE_LIST_JSON1}"))
        assertPageTabFile(submissionFolder.resolve("data/${FILE_LIST_XML1}"))
        assertPageTabFile(submissionFolder.resolve("data/${FILE_LIST_TSV1}"))

        assertPageTabFile(submissionFolder.resolve("data/${FILE_LIST_JSON2}"))
        assertPageTabFile(submissionFolder.resolve("data/${FILE_LIST_XML2}"))
        assertPageTabFile(submissionFolder.resolve("data/${FILE_LIST_TSV2}"))

        assertPageTabFile(submissionFolder.resolve("data/${FILE_LIST_JSON3}"))
        assertPageTabFile(submissionFolder.resolve("data/${FILE_LIST_XML3}"))
        assertPageTabFile(submissionFolder.resolve("data/${FILE_LIST_TSV3}"))
    }

    private fun verifySubmissionFiles(subFolder: File) {
        assertPageTabFile(subFolder.resolve(SUB_JSON))
        assertPageTabFile(subFolder.resolve(SUB_XML))
        assertPageTabFile(subFolder.resolve(SUB_TSV))
    }

    private fun assertPageTabFile(file: File) {
        assertThat(file).exists()
        assertThat(Files.getPosixFilePermissions(file.toPath())).containsExactlyInAnyOrderElementsOf(RW_R_____)
    }
}

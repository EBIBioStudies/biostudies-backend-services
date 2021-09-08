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
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.io.RW_R_____
import ebi.ac.uk.paths.SubmissionFolderResolver
import ebi.ac.uk.test.basicExtSubmission
import ebi.ac.uk.util.collections.second
import ebi.ac.uk.util.collections.third
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat as assertJThat
import ebi.ac.uk.asserts.assertThat
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

        val result = testInstance.generatePageTab(submission)

        assertSubmission(result)
        assertRootSection(result.section)
        assertThat(result.section.sections.first()).hasLeftValueSatisfying { assertSubSection(it) }
        verifyFileLists(subFolder)
        verifySubmissionFiles(subFolder)
    }

    private fun setUpSerializer(element: Any) {
        every { serializationService.serializeElement(element, JSON_PRETTY) } returns "json"
        every { serializationService.serializeElement(element, XML) } returns "xml"
        every { serializationService.serializeElement(element, TSV) } returns "tsv"
    }

    private fun initialRootSection() = ExtSection(
        type = "Study1",
        fileList = ExtFileList("data/file-list1"),
        sections = listOf(
            left(ExtSection(type = "Study2", fileList = ExtFileList("data/file-list2"))),
            Either.right(ExtSectionTable(listOf(ExtSection(type = "Study3"))))
        )
    )

    private fun assertSubmission(submission: ExtSubmission) {
        val tabFiles = submission.tabFiles
        assertJThat(tabFiles.first()).isEqualTo(NfsFile(SUB_JSON, subFolder.resolve(SUB_JSON)))
        assertJThat(tabFiles.second()).isEqualTo(NfsFile(SUB_XML, subFolder.resolve(SUB_XML)))
        assertJThat(tabFiles.third()).isEqualTo(NfsFile(SUB_TSV, subFolder.resolve(SUB_TSV)))
    }

    private fun assertRootSection(section: ExtSection) {
        val tabFiles = section.fileList!!.tabFiles
        assertJThat(tabFiles.first()).isEqualTo(NfsFile(FILE_LIST_JSON1, subFolder.resolve("data/${FILE_LIST_JSON1}")))
        assertJThat(tabFiles.second()).isEqualTo(NfsFile(FILE_LIST_XML1, subFolder.resolve("data/${FILE_LIST_XML1}")))
        assertJThat(tabFiles.third()).isEqualTo(NfsFile(FILE_LIST_TSV1, subFolder.resolve("data/${FILE_LIST_TSV1}")))
    }

    private fun assertSubSection(section: ExtSection?) {
        val tabFiles = section!!.fileList!!.tabFiles
        assertJThat(tabFiles.first()).isEqualTo(NfsFile(FILE_LIST_JSON2, subFolder.resolve("data/${FILE_LIST_JSON2}")))
        assertJThat(tabFiles.second()).isEqualTo(NfsFile(FILE_LIST_XML2, subFolder.resolve("data/${FILE_LIST_XML2}")))
        assertJThat(tabFiles.third()).isEqualTo(NfsFile(FILE_LIST_TSV2, subFolder.resolve("data/${FILE_LIST_TSV2}")))
    }

    private fun verifyFileLists(submissionFolder: File) {
        assertPageTabFile(submissionFolder.resolve("data/${FILE_LIST_JSON1}"))
        assertPageTabFile(submissionFolder.resolve("data/${FILE_LIST_XML1}"))
        assertPageTabFile(submissionFolder.resolve("data/${FILE_LIST_TSV1}"))

        assertPageTabFile(submissionFolder.resolve("data/${FILE_LIST_JSON2}"))
        assertPageTabFile(submissionFolder.resolve("data/${FILE_LIST_XML2}"))
        assertPageTabFile(submissionFolder.resolve("data/${FILE_LIST_TSV2}"))
    }

    private fun verifySubmissionFiles(subFolder: File) {
        assertPageTabFile(subFolder.resolve(SUB_JSON))
        assertPageTabFile(subFolder.resolve(SUB_XML))
        assertPageTabFile(subFolder.resolve(SUB_TSV))
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

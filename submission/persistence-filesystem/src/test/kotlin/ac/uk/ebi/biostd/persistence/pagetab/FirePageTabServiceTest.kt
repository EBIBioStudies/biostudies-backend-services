package ac.uk.ebi.biostd.persistence.pagetab

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.persistence.filesystem.pagetab.FirePageTabService
import ac.uk.ebi.biostd.persistence.filesystem.pagetab.PageTabFiles
import ac.uk.ebi.biostd.persistence.filesystem.pagetab.generateFileListPageTab
import ac.uk.ebi.biostd.persistence.filesystem.pagetab.generateSubPageTab
import arrow.core.Either.Companion.left
import arrow.core.Either.Companion.right
import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSectionTable
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.test.basicExtSubmission
import ebi.ac.uk.util.collections.second
import ebi.ac.uk.util.collections.third
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkStatic
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.fire.client.integration.web.FireWebClient
import ebi.ac.uk.asserts.assertThat as assertThatEither
import uk.ac.ebi.fire.client.model.FireFile as FireFileWeb

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
        val subWithoutTabFiles = basicExtSubmission.copy(section = sectionWithoutTabFiles())
        setUpGeneratePageTab(subWithoutTabFiles)
        setUpFireWebClient()

        val result = testInstance.generatePageTab(subWithoutTabFiles)

        assertSubmissionTabFiles(result)
        assertSectionTabFiles(result.section)
        assertThatEither(result.section.sections.first()).hasLeftValueSatisfying { assertSubSectionTabFiles(it) }
    }

    private fun setUpGeneratePageTab(submission: ExtSubmission) {
        mockkStatic("ac.uk.ebi.biostd.persistence.filesystem.pagetab.PageTabUtilKt")

        every { serializationService.generateSubPageTab(submission, fireFolder) } returns
            PageTabFiles(
                fireFolder.resolve("S-TEST123.json"),
                fireFolder.resolve("S-TEST123.xml"),
                fireFolder.resolve("S-TEST123.pagetab.tsv")
            )
        every { serializationService.generateFileListPageTab(submission, fireFolder) } returns mapOf(
            "data/file-list2" to PageTabFiles(
                fireFolder.resolve("file-list2.json"),
                fireFolder.resolve("file-list2.xml"),
                fireFolder.resolve("file-list2.pagetab.tsv")
            ),
            "data/file-list1" to PageTabFiles(
                fireFolder.resolve("file-list1.json"),
                fireFolder.resolve("file-list1.xml"),
                fireFolder.resolve("file-list1.pagetab.tsv")
            )
        )
    }

    private fun setUpFireWebClient() {
        every { fireWebClient.save(any(), any(), any()) } returns
            FireFileWeb(1, "$FILE_LIST_JSON2-fireId", "md5", 1, "creationTime") andThen
            FireFileWeb(2, "$FILE_LIST_XML2-fireId", "md5", 1, "creationTime") andThen
            FireFileWeb(3, "$FILE_LIST_TSV2-fireId", "md5", 1, "creationTime") andThen
            FireFileWeb(4, "$FILE_LIST_JSON1-fireId", "md5", 1, "creationTime") andThen
            FireFileWeb(5, "$FILE_LIST_XML1-fireId", "md5", 1, "creationTime") andThen
            FireFileWeb(6, "$FILE_LIST_TSV1-fireId", "md5", 1, "creationTime") andThen
            FireFileWeb(7, "$SUB_JSON-fireId", "md5", 1, "creationTime") andThen
            FireFileWeb(8, "$SUB_XML-fireId", "md5", 1, "creationTime") andThen
            FireFileWeb(9, "$SUB_TSV-fireId", "md5", 1, "creationTime")
    }

    private fun sectionWithoutTabFiles() = ExtSection(
        type = "Study1",
        fileList = ExtFileList("data/file-list1"),
        sections = listOf(
            left(ExtSection(type = "Study2", fileList = ExtFileList("data/file-list2"))),
            right(ExtSectionTable(listOf(ExtSection(type = "Study3"))))
        )
    )

    private fun assertSubmissionTabFiles(submission: ExtSubmission) {
        val tabFiles = submission.pageTabFiles
        assertThat(tabFiles.first()).isEqualTo(
            FireFile(
                SUB_JSON,
                SUB_JSON,
                fireFolder.resolve(SUB_JSON).path,
                "$SUB_JSON-fireId",
                "md5",
                1,
                listOf()
            )
        )
        assertThat(tabFiles.second()).isEqualTo(
            FireFile(
                SUB_XML,
                SUB_XML,
                fireFolder.resolve(SUB_XML).path,
                "$SUB_XML-fireId",
                "md5",
                1,
                listOf()
            )
        )
        assertThat(tabFiles.third()).isEqualTo(
            FireFile(
                SUB_TSV,
                SUB_TSV,
                fireFolder.resolve(SUB_TSV).path,
                "$SUB_TSV-fireId",
                "md5",
                1,
                listOf()
            )
        )
    }

    private fun assertSectionTabFiles(section: ExtSection) {
        val tabFiles = section.fileList!!.pageTabFiles
        assertThat(tabFiles.first()).isEqualTo(
            FireFile(
                FILE_LIST_JSON1,
                FILE_LIST_JSON1,
                fireFolder.resolve(FILE_LIST_JSON1).path,
                "$FILE_LIST_JSON1-fireId",
                "md5",
                1,
                listOf()
            )
        )
        assertThat(tabFiles.second()).isEqualTo(
            FireFile(
                FILE_LIST_XML1,
                FILE_LIST_XML1,
                fireFolder.resolve(FILE_LIST_XML1).path,
                "$FILE_LIST_XML1-fireId",
                "md5",
                1,
                listOf()
            )
        )
        assertThat(tabFiles.third()).isEqualTo(
            FireFile(
                FILE_LIST_TSV1,
                FILE_LIST_TSV1,
                fireFolder.resolve(FILE_LIST_TSV1).path,
                "$FILE_LIST_TSV1-fireId",
                "md5",
                1,
                listOf()
            )
        )
    }

    private fun assertSubSectionTabFiles(section: ExtSection) {
        val tabFiles = section.fileList!!.pageTabFiles
        assertThat(tabFiles.first()).isEqualTo(
            FireFile(
                FILE_LIST_JSON2,
                FILE_LIST_JSON2,
                fireFolder.resolve(FILE_LIST_JSON2).path,
                "$FILE_LIST_JSON2-fireId",
                "md5",
                1,
                listOf()
            )
        )
        assertThat(tabFiles.second()).isEqualTo(
            FireFile(
                FILE_LIST_XML2,
                FILE_LIST_XML2,
                fireFolder.resolve(FILE_LIST_XML2).path,
                "$FILE_LIST_XML2-fireId",
                "md5",
                1,
                listOf()
            )
        )
        assertThat(tabFiles.third()).isEqualTo(
            FireFile(
                FILE_LIST_TSV2,
                FILE_LIST_TSV2,
                fireFolder.resolve(FILE_LIST_TSV2).path,
                "$FILE_LIST_TSV2-fireId",
                "md5",
                1,
                listOf()
            )
        )
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

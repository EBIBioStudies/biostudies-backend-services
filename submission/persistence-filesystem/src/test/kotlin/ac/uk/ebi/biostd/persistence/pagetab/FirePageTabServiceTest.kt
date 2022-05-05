package ac.uk.ebi.biostd.persistence.pagetab

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.persistence.filesystem.pagetab.FirePageTabService
import ac.uk.ebi.biostd.persistence.filesystem.pagetab.PageTabFiles
import ac.uk.ebi.biostd.persistence.filesystem.pagetab.PageTabUtil
import ac.uk.ebi.biostd.persistence.filesystem.service.FileProcessingService
import arrow.core.Either.Companion.left
import arrow.core.Either.Companion.right
import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.extended.model.ExtFileType.FILE
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
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.extended.serialization.service.createExtFileList
import uk.ac.ebi.fire.client.integration.web.FireWebClient
import uk.ac.ebi.serialization.common.FilesResolver
import ebi.ac.uk.asserts.assertThat as assertThatEither
import uk.ac.ebi.fire.client.model.FireApiFile as FireFileWeb

@ExtendWith(TemporaryFolderExtension::class, MockKExtension::class)
class FirePageTabServiceTest(
    tempFolder: TemporaryFolder,
    @MockK private val serializationService: SerializationService,
    @MockK private val fireWebClient: FireWebClient,
    @MockK private val pageTabUtil: PageTabUtil,
) {
    private val fireFolder = tempFolder.root.resolve("fire-temp")
    private val fileProcessingService =
        FileProcessingService(ExtSerializationService(), FilesResolver(tempFolder.createDirectory("ext-files")))
    private val testInstance =
        FirePageTabService(
            fireFolder,
            fireWebClient,
            pageTabUtil,
            fileProcessingService
        )

    @Test
    fun `generate page tab`() {
        val subWithoutTabFiles = basicExtSubmission.copy(section = sectionWithoutTabFiles())
        setUpGeneratePageTab(subWithoutTabFiles)
        setUpFireWebClient()

        val result = testInstance.generatePageTab(subWithoutTabFiles)

        assertSubmissionTabFiles(result)
        assertSectionTabFiles(result.section)
        assertThatEither(result.section.sections.first()).hasLeftValueSatisfying { assertSubSectionTabFiles(it) }
        verifySetFirePath()
        verifySetBioMetadata()
    }

    private fun setUpGeneratePageTab(submission: ExtSubmission) {
        every { pageTabUtil.generateSubPageTab(submission, fireFolder) } returns PageTabFiles(
            fireFolder.resolve("S-TEST123.json"),
            fireFolder.resolve("S-TEST123.xml"),
            fireFolder.resolve("S-TEST123.pagetab.tsv")
        )
        every {
            pageTabUtil.generateFileListPageTab(
                submission,
                fireFolder
            )
        } returns mapOf(
            "data/file-list2" to PageTabFiles(
                fireFolder.resolve("data/file-list2.json"),
                fireFolder.resolve("data/file-list2.xml"),
                fireFolder.resolve("data/file-list2.pagetab.tsv")
            ),
            "data/file-list1" to PageTabFiles(
                fireFolder.resolve("data/file-list1.json"),
                fireFolder.resolve("data/file-list1.xml"),
                fireFolder.resolve("data/file-list1.pagetab.tsv")
            )
        )
    }

    private fun setUpFireWebClient() {
        every { fireWebClient.save(any(), any()) } returns
            FireFileWeb(1, "$FILE_LIST_JSON2-fireId", "md5", 1, "creationTime") andThen
            FireFileWeb(2, "$FILE_LIST_XML2-fireId", "md5", 1, "creationTime") andThen
            FireFileWeb(3, "$FILE_LIST_TSV2-fireId", "md5", 1, "creationTime") andThen
            FireFileWeb(4, "$FILE_LIST_JSON1-fireId", "md5", 1, "creationTime") andThen
            FireFileWeb(5, "$FILE_LIST_XML1-fireId", "md5", 1, "creationTime") andThen
            FireFileWeb(6, "$FILE_LIST_TSV1-fireId", "md5", 1, "creationTime") andThen
            FireFileWeb(7, "$SUB_JSON-fireId", "md5", 1, "creationTime") andThen
            FireFileWeb(8, "$SUB_XML-fireId", "md5", 1, "creationTime") andThen
            FireFileWeb(9, "$SUB_TSV-fireId", "md5", 1, "creationTime")

        every { fireWebClient.setBioMetadata("$SUB_TSV-fireId", "S-TEST123", "file", false) } answers { nothing }
        every { fireWebClient.setBioMetadata("$SUB_XML-fireId", "S-TEST123", "file", false) } answers { nothing }
        every { fireWebClient.setBioMetadata("$SUB_JSON-fireId", "S-TEST123", "file", false) } answers { nothing }
        every { fireWebClient.setBioMetadata("$FILE_LIST_TSV1-fireId", "S-TEST123", "file", false) } answers { nothing }
        every { fireWebClient.setBioMetadata("$FILE_LIST_TSV2-fireId", "S-TEST123", "file", false) } answers { nothing }
        every { fireWebClient.setBioMetadata("$FILE_LIST_XML1-fireId", "S-TEST123", "file", false) } answers { nothing }
        every { fireWebClient.setBioMetadata("$FILE_LIST_XML2-fireId", "S-TEST123", "file", false) } answers { nothing }
        every {
            fireWebClient.setBioMetadata("$FILE_LIST_JSON1-fireId", "S-TEST123", "file", false)
        } answers { nothing }
        every {
            fireWebClient.setBioMetadata("$FILE_LIST_JSON2-fireId", "S-TEST123", "file", false)
        } answers { nothing }

        every { fireWebClient.setPath("$SUB_TSV-fireId", "S-TEST/123/S-TEST123/$SUB_TSV") } answers { nothing }
        every { fireWebClient.setPath("$SUB_XML-fireId", "S-TEST/123/S-TEST123/$SUB_XML") } answers { nothing }
        every { fireWebClient.setPath("$SUB_JSON-fireId", "S-TEST/123/S-TEST123/$SUB_JSON") } answers { nothing }
        every {
            fireWebClient.setPath("$FILE_LIST_TSV1-fireId", "S-TEST/123/S-TEST123/Files/data/$FILE_LIST_TSV1")
        } answers { nothing }
        every {
            fireWebClient.setPath("$FILE_LIST_TSV2-fireId", "S-TEST/123/S-TEST123/Files/data/$FILE_LIST_TSV2")
        } answers { nothing }
        every {
            fireWebClient.setPath("$FILE_LIST_XML1-fireId", "S-TEST/123/S-TEST123/Files/data/$FILE_LIST_XML1")
        } answers { nothing }
        every {
            fireWebClient.setPath("$FILE_LIST_XML2-fireId", "S-TEST/123/S-TEST123/Files/data/$FILE_LIST_XML2")
        } answers { nothing }
        every {
            fireWebClient.setPath("$FILE_LIST_JSON1-fireId", "S-TEST/123/S-TEST123/Files/data/$FILE_LIST_JSON1")
        } answers { nothing }
        every {
            fireWebClient.setPath("$FILE_LIST_JSON2-fireId", "S-TEST/123/S-TEST123/Files/data/$FILE_LIST_JSON2")
        } answers { nothing }
    }

    private fun sectionWithoutTabFiles() = ExtSection(
        type = "Study1",
        fileList = ExtFileList(
            "data/file-list1",
            file = createExtFileList()
        ),
        sections = listOf(
            left(
                ExtSection(
                    type = "Study2",
                    fileList = ExtFileList(
                        "data/file-list2",
                        file = createExtFileList()
                    )
                )
            ),
            right(ExtSectionTable(listOf(ExtSection(type = "Study3"))))
        )
    )

    private fun assertSubmissionTabFiles(submission: ExtSubmission) {
        val files = submission.pageTabFiles
        assertThat(files.first()).isEqualTo(FireFile(SUB_JSON, SUB_JSON, "$SUB_JSON-fireId", "md5", 1, FILE, listOf()))
        assertThat(files.second()).isEqualTo(FireFile(SUB_XML, SUB_XML, "$SUB_XML-fireId", "md5", 1, FILE, listOf()))
        assertThat(files.third()).isEqualTo(FireFile(SUB_TSV, SUB_TSV, "$SUB_TSV-fireId", "md5", 1, FILE, listOf()))
    }

    private fun assertSectionTabFiles(section: ExtSection) {
        val tabFiles = section.fileList!!.pageTabFiles
        assertThat(tabFiles.first()).isEqualTo(
            FireFile(
                "data/$FILE_LIST_JSON1",
                "Files/data/$FILE_LIST_JSON1",
                "$FILE_LIST_JSON1-fireId",
                "md5",
                1,
                FILE,
                listOf()
            )
        )
        assertThat(tabFiles.second()).isEqualTo(
            FireFile(
                "data/$FILE_LIST_XML1",
                "Files/data/$FILE_LIST_XML1",
                "$FILE_LIST_XML1-fireId",
                "md5",
                1,
                FILE,
                listOf()
            )
        )
        assertThat(tabFiles.third()).isEqualTo(
            FireFile(
                "data/$FILE_LIST_TSV1",
                "Files/data/$FILE_LIST_TSV1",
                "$FILE_LIST_TSV1-fireId",
                "md5",
                1,
                FILE,
                listOf()
            )
        )
    }

    private fun assertSubSectionTabFiles(section: ExtSection) {
        val tabFiles = section.fileList!!.pageTabFiles
        assertThat(tabFiles.first()).isEqualTo(
            FireFile(
                "data/$FILE_LIST_JSON2",
                "Files/data/$FILE_LIST_JSON2",
                "$FILE_LIST_JSON2-fireId",
                "md5",
                1,
                FILE,
                listOf()
            )
        )
        assertThat(tabFiles.second()).isEqualTo(
            FireFile(
                "data/$FILE_LIST_XML2",
                "Files/data/$FILE_LIST_XML2",
                "$FILE_LIST_XML2-fireId",
                "md5",
                1,
                FILE,
                listOf()
            )
        )
        assertThat(tabFiles.third()).isEqualTo(
            FireFile(
                "data/$FILE_LIST_TSV2",
                "Files/data/$FILE_LIST_TSV2",
                "$FILE_LIST_TSV2-fireId",
                "md5",
                1,
                FILE,
                listOf()
            )
        )
    }

    private fun verifySetFirePath() = verify(exactly = 1) {
        fireWebClient.setPath("$SUB_TSV-fireId", "S-TEST/123/S-TEST123/$SUB_TSV")
        fireWebClient.setPath("$SUB_XML-fireId", "S-TEST/123/S-TEST123/$SUB_XML")
        fireWebClient.setPath("$SUB_JSON-fireId", "S-TEST/123/S-TEST123/$SUB_JSON")
        fireWebClient.setPath("$FILE_LIST_TSV1-fireId", "S-TEST/123/S-TEST123/Files/data/$FILE_LIST_TSV1")
        fireWebClient.setPath("$FILE_LIST_TSV2-fireId", "S-TEST/123/S-TEST123/Files/data/$FILE_LIST_TSV2")
        fireWebClient.setPath("$FILE_LIST_XML1-fireId", "S-TEST/123/S-TEST123/Files/data/$FILE_LIST_XML1")
        fireWebClient.setPath("$FILE_LIST_XML2-fireId", "S-TEST/123/S-TEST123/Files/data/$FILE_LIST_XML2")
        fireWebClient.setPath("$FILE_LIST_JSON1-fireId", "S-TEST/123/S-TEST123/Files/data/$FILE_LIST_JSON1")
        fireWebClient.setPath("$FILE_LIST_JSON2-fireId", "S-TEST/123/S-TEST123/Files/data/$FILE_LIST_JSON2")
    }

    private fun verifySetBioMetadata() = verify(exactly = 1) {
        fireWebClient.setBioMetadata("$SUB_TSV-fireId", "S-TEST123", "file", false)
        fireWebClient.setBioMetadata("$SUB_XML-fireId", "S-TEST123", "file", false)
        fireWebClient.setBioMetadata("$SUB_JSON-fireId", "S-TEST123", "file", false)
        fireWebClient.setBioMetadata("$FILE_LIST_TSV1-fireId", "S-TEST123", "file", false)
        fireWebClient.setBioMetadata("$FILE_LIST_TSV2-fireId", "S-TEST123", "file", false)
        fireWebClient.setBioMetadata("$FILE_LIST_XML1-fireId", "S-TEST123", "file", false)
        fireWebClient.setBioMetadata("$FILE_LIST_XML2-fireId", "S-TEST123", "file", false)
        fireWebClient.setBioMetadata("$FILE_LIST_JSON1-fireId", "S-TEST123", "file", false)
        fireWebClient.setBioMetadata("$FILE_LIST_JSON2-fireId", "S-TEST123", "file", false)
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

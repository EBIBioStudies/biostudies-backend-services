package ac.uk.ebi.biostd.persistence.pagetab

import DefaultFileList.Companion.defaultFileList
import DefaultNfsFile.Companion.defaultNfsFile
import DefaultSection.Companion.defaultSection
import DefaultSectionTable.Companion.defaultSectionTable
import DefaultSubmission.Companion.defaultSubmission
import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.persistence.filesystem.pagetab.NfsPageTabService
import ac.uk.ebi.biostd.persistence.filesystem.pagetab.PageTabFiles
import ac.uk.ebi.biostd.persistence.filesystem.pagetab.generateFileListPageTab
import ac.uk.ebi.biostd.persistence.filesystem.pagetab.generateSubPageTab
import arrow.core.Either
import arrow.core.Either.Companion.left
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.paths.SubmissionFolderResolver
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
import java.nio.file.Paths
import ebi.ac.uk.asserts.assertThat as assertThatEither

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
        val submission = defaultSubmission(section = sectionWithoutTabFiles())
        setUpGeneratePageTab(submission)

        val result = testInstance.generatePageTab(submission)

        assertSubmissionTabFiles(result)
        assertSectionTabFiles(result.section)
        assertThatEither(result.section.sections.first()).hasLeftValueSatisfying { assertSubSectionTabFiles(it) }
    }

    private fun setUpGeneratePageTab(submission: ExtSubmission) {
        mockkStatic("ac.uk.ebi.biostd.persistence.filesystem.pagetab.PageTabUtilKt")

        every { serializationService.generateSubPageTab(submission, subFolder) } returns PageTabFiles(
            subFolder.resolve("S-TEST123.json"),
            subFolder.resolve("S-TEST123.xml"),
            subFolder.resolve("S-TEST123.pagetab.tsv")
        )
        every { serializationService.generateFileListPageTab(submission, subFolder.resolve("Files")) } returns mapOf(
            "data/file-list2" to PageTabFiles(
                subFolder.resolve("Files/data/file-list2.json"),
                subFolder.resolve("Files/data/file-list2.xml"),
                subFolder.resolve("Files/data/file-list2.pagetab.tsv")
            ),
            "data/file-list1" to PageTabFiles(
                subFolder.resolve("Files/data/file-list1.json"),
                subFolder.resolve("Files/data/file-list1.xml"),
                subFolder.resolve("Files/data/file-list1.pagetab.tsv")
            )
        )
    }

    private fun sectionWithoutTabFiles() = defaultSection(
        type = "Study1",
        fileList = defaultFileList("data/file-list1"),
        sections = listOf(
            left(defaultSection(type = "Study2", fileList = defaultFileList("data/file-list2"))),
            Either.right(defaultSectionTable(listOf(defaultSection(type = "Study3"))))
        )
    )

    private fun assertSubmissionTabFiles(submission: ExtSubmission) {
        val tabFiles = submission.pageTabFiles
        assertThat(tabFiles.first()).isEqualTo(
            defaultNfsFile(SUB_JSON, SUB_JSON, "$subFolder/$SUB_JSON", subFolder.resolve(SUB_JSON))
        )
        assertThat(tabFiles.second()).isEqualTo(
            defaultNfsFile(SUB_XML, SUB_XML, "$subFolder/$SUB_XML", subFolder.resolve(SUB_XML))
        )
        assertThat(tabFiles.third()).isEqualTo(
            defaultNfsFile(SUB_TSV, SUB_TSV, "$subFolder/$SUB_TSV", subFolder.resolve(SUB_TSV))
        )
    }

    private fun assertSectionTabFiles(section: ExtSection) {
        val tabFiles = section.fileList!!.pageTabFiles
        assertThat(tabFiles.first()).isEqualTo(
            defaultNfsFile(
                "data/$FILE_LIST_JSON1",
                "Files/data/$FILE_LIST_JSON1",
                "$subFolder/Files/data/$FILE_LIST_JSON1",
                subFolder.resolve("Files/data/$FILE_LIST_JSON1")
            )
        )
        assertThat(tabFiles.second()).isEqualTo(
            defaultNfsFile(
                "data/$FILE_LIST_XML1",
                "Files/data/$FILE_LIST_XML1",
                "$subFolder/Files/data/$FILE_LIST_XML1",
                subFolder.resolve("Files/data/$FILE_LIST_XML1")
            )
        )
        assertThat(tabFiles.third()).isEqualTo(
            defaultNfsFile(
                "data/$FILE_LIST_TSV1",
                "Files/data/$FILE_LIST_TSV1",
                "$subFolder/Files/data/$FILE_LIST_TSV1",
                subFolder.resolve("Files/data/$FILE_LIST_TSV1")
            )
        )
    }

    private fun assertSubSectionTabFiles(section: ExtSection?) {
        val tabFiles = section!!.fileList!!.pageTabFiles
        assertThat(tabFiles.first()).isEqualTo(
            defaultNfsFile(
                "data/$FILE_LIST_JSON2",
                "Files/data/$FILE_LIST_JSON2",
                "$subFolder/Files/data/$FILE_LIST_JSON2",
                subFolder.resolve("Files/data/$FILE_LIST_JSON2")
            )
        )
        assertThat(tabFiles.second()).isEqualTo(
            defaultNfsFile(
                "data/$FILE_LIST_XML2",
                "Files/data/$FILE_LIST_XML2",
                "$subFolder/Files/data/$FILE_LIST_XML2",
                subFolder.resolve("Files/data/$FILE_LIST_XML2")
            )
        )
        assertThat(tabFiles.third()).isEqualTo(
            defaultNfsFile(
                "data/$FILE_LIST_TSV2",
                "Files/data/$FILE_LIST_TSV2",
                "$subFolder/Files/data/$FILE_LIST_TSV2",
                subFolder.resolve("Files/data/$FILE_LIST_TSV2")
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

package ac.uk.ebi.biostd.itest.test.submission.submit

import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.TSV
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.client.integration.web.SubmissionFilesConfig
import ac.uk.ebi.biostd.common.config.FilePersistenceConfig
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.enableFire
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.storageMode
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.submissionPath
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.tempFolder
import ac.uk.ebi.biostd.itest.itest.getWebClient
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.filesystem.fire.ZipUtil
import ebi.ac.uk.asserts.assertThat
import ebi.ac.uk.dsl.file
import ebi.ac.uk.dsl.section
import ebi.ac.uk.dsl.submission
import ebi.ac.uk.dsl.tsv.line
import ebi.ac.uk.dsl.tsv.tsv
import ebi.ac.uk.extended.mapping.to.ToSubmissionMapper
import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtFileType
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.allSectionsFiles
import ebi.ac.uk.io.ext.allSubFiles
import ebi.ac.uk.io.ext.createDirectory
import ebi.ac.uk.io.ext.createFile
import ebi.ac.uk.io.ext.md5
import ebi.ac.uk.io.sources.PreferredSource.FIRE
import ebi.ac.uk.io.sources.PreferredSource.SUBMISSION
import ebi.ac.uk.io.sources.PreferredSource.USER_SPACE
import ebi.ac.uk.model.extensions.title
import ebi.ac.uk.util.collections.second
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfSystemProperty
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.transaction.annotation.Transactional
import uk.ac.ebi.fire.client.integration.web.FireClient
import java.io.File
import java.nio.file.Paths

@Import(FilePersistenceConfig::class)
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
class SubmissionFileSourceTest(
    @Autowired private val submissionRepository: SubmissionPersistenceQueryService,
    @Autowired private val securityTestService: SecurityTestService,
    @Autowired val toSubmissionMapper: ToSubmissionMapper,
    @Autowired private val fireClient: FireClient,
    @LocalServerPort val serverPort: Int,
) {
    private lateinit var webClient: BioWebClient

    @BeforeAll
    fun init() {
        securityTestService.ensureUserRegistration(SuperUser)
        webClient = getWebClient(serverPort, SuperUser)
    }

    @Test
    fun `6-1 resubmission with SUBMISSION file source as priority over USER_SPACE`() {
        fun submission(fileList: String) = tsv {
            line("Submission", "S-FSTST1")
            line("Title", "Preferred Source Submission")
            line()

            line("Study", "SECT-001")
            line("Title", "Root Section")
            line("File List", fileList)
            line()

            line("File", "File1.txt")

            line()
        }.toString()

        val fileList = tempFolder.createFile(
            "FileList.tsv",
            tsv {
                line("Files", "GEN")
                line("File2.txt", "ABC")
            }.toString()
        )

        val file1 = tempFolder.createFile("File1.txt", "content file 1")
        val file2 = tempFolder.createFile("File2.txt", "content file 2")
        webClient.uploadFiles(listOf(file1, file2))
        val filesConfig = SubmissionFilesConfig(listOf(fileList), storageMode)
        assertThat(webClient.submitSingle(submission("FileList.tsv"), TSV, filesConfig)).isSuccessful()

        val firstVersion = submissionRepository.getExtByAccNo("S-FSTST1")
        val firstVersionReferencedFiles = submissionRepository.getReferencedFiles(firstVersion, "FileList")
        val subFilesPath = "$submissionPath/${firstVersion.relPath}/Files"
        val innerFile = Paths.get("$subFilesPath/File1.txt")
        val referencedFile = Paths.get("$subFilesPath/File2.txt")

        assertThat(innerFile).exists()
        assertThat(innerFile.toFile().readText()).isEqualTo("content file 1")
        assertThat(referencedFile).exists()
        assertThat(referencedFile.toFile().readText()).isEqualTo("content file 2")

        file1.delete()
        file2.delete()
        fileList.delete()
        webClient.uploadFiles(listOf(tempFolder.createFile("File1.txt", "content file 1 updated")))

        assertThat(
            webClient.submitSingle(
                submission("FileList.json"),
                TSV,
                SubmissionFilesConfig(emptyList(), storageMode, preferredSources = listOf(SUBMISSION, USER_SPACE)),
            )
        ).isSuccessful()
        assertThat(innerFile.toFile().readText()).isEqualTo("content file 1")
        assertThat(referencedFile.toFile().readText()).isEqualTo("content file 2")

        if (enableFire) {
            val secondVersion = submissionRepository.getExtByAccNo("S-FSTST1")
            val secondVersionReferencedFiles = submissionRepository.getReferencedFiles(secondVersion, "FileList")

            val firstVersionFireId = (firstVersion.allSectionsFiles.first() as FireFile).fireId
            val secondVersionFireId = (secondVersion.allSectionsFiles.first() as FireFile).fireId
            assertThat(firstVersionFireId).isEqualTo(secondVersionFireId)

            val firstVersionReferencedFireId = (firstVersionReferencedFiles.first() as FireFile).fireId
            val secondVersionReferencedFireId = (secondVersionReferencedFiles.first() as FireFile).fireId
            assertThat(firstVersionReferencedFireId).isEqualTo(secondVersionReferencedFireId)
        }
    }

    @Test
    @EnabledIfSystemProperty(named = "enableFire", matches = "true")
    fun `6-2 submission with FIRE source only`() {
        val file3 = tempFolder.createFile("File3.txt", "content file 3")
        val file4 = tempFolder.createFile("File4.txt", "content file 4")
        val file3Md5 = file3.md5()
        val file4Md5 = file4.md5()

        val fireFile3 = fireClient.save(file3, file3Md5, 55L)
        val fireFile4 = fireClient.save(file4, file4Md5, 55L)

        val submission = tsv {
            line("Submission", "S-FSTST2")
            line("Title", "FIRE Only File Source Submission")
            line()

            line("Study", "SECT-001")
            line("Title", "Root Section")
            line("File List", "FileList.tsv")
            line()

            line("File", "File4.txt")
            line("dbMd5", file4Md5)
            line()

            line()
        }.toString()

        val fileList = tempFolder.createFile(
            "FileList.tsv",
            tsv {
                line("Files", "GEN", "dbMd5")
                line("File3.txt", "ABC", file3Md5)
            }.toString()
        )

        val filesConfig =
            SubmissionFilesConfig(listOf(fileList), storageMode, preferredSources = listOf(FIRE))
        assertThat(webClient.submitSingle(submission, TSV, filesConfig)).isSuccessful()

        val persistedSubmission = submissionRepository.getExtByAccNo("S-FSTST2")
        val firstVersionReferencedFiles = submissionRepository.getReferencedFiles(persistedSubmission, "FileList")
        val subFilesPath = "$submissionPath/${persistedSubmission.relPath}/Files"
        val innerFile = Paths.get("$subFilesPath/File4.txt")
        val referencedFile = Paths.get("$subFilesPath/File3.txt")

        assertThat(innerFile).exists()
        assertThat(innerFile.toFile().readText()).isEqualTo("content file 4")
        assertThat(referencedFile).exists()
        assertThat(referencedFile.toFile().readText()).isEqualTo("content file 3")

        val innerFileFireId = (persistedSubmission.allSectionsFiles.first() as FireFile).fireId
        assertThat(innerFileFireId).isEqualTo(fireFile4.fireOid)

        val referencedFileFireId = (firstVersionReferencedFiles.first() as FireFile).fireId
        assertThat(referencedFileFireId).isEqualTo(fireFile3.fireOid)
    }


    @Nested
    inner class SubmissionsWithFolders {
        @Test
        @EnabledIfSystemProperty(named = "enableFire", matches = "true")
        fun `6-3-1 submission with directory with files on FIRE`() {
            val submission = tsv {
                line("Submission", "S-FSTST3")
                line("Title", "Simple Submission With directory")
                line()

                line("Study")
                line()

                line("File", "directory")
                line("Type", "test")
                line()
            }.toString()

            val file1 = tempFolder.createFile("file1.txt", "content-1")
            val file2 = tempFolder.createFile(".file2.txt", "content-2")

            webClient.uploadFiles(listOf(file1), "directory")
            webClient.uploadFiles(listOf(file2), "directory/subdirectory")

            assertThat(webClient.submitSingle(submission, TSV)).isSuccessful()

            val submitted = submissionRepository.getExtByAccNo("S-FSTST3")
            assertThat(submitted.section.files).hasSize(1)
            assertThat(submitted.section.files.first()).hasLeftValueSatisfying {
                assertThat(it.type).isEqualTo(ExtFileType.DIR)
                assertThat(it.size).isEqualTo(328L)
                assertThat(it.md5).isEqualTo("18CF763D0BBA08E1AE232C191A3B58CF")

                val files = getZipFiles("$submissionPath/${submitted.relPath}/Files/directory.zip")
                assertThat(files).containsExactly(
                    "file1.txt" to file1.readText(),
                    "subdirectory/.file2.txt" to file2.readText()
                )
            }
        }

        private fun getZipFiles(filePath: String): List<Pair<String, String>> {
            val subZip = tempFolder.createDirectory("target")
            ZipUtil.unpack(File(filePath), subZip)
            val files = subZip.allSubFiles()
                .filter { file -> file.isDirectory.not() }
                .map { file -> file.toRelativeString(subZip) to file.readText() }
            subZip.delete()
            return files
        }
    }


    @Test
    fun `6-4 multiple file references`() {
        val firstVersionFileList = tsv {
            line("Files", "Type")
            line("MultipleReferences.txt", "Ref 1")
            line("MultipleReferences.txt", "Ref 2")
            line()
        }.toString()
        val firstVersionPagetab = tsv {
            line("Submission", "S-FSTST4")
            line("Title", "Simple Submission With Files")
            line("ReleaseDate", "2020-01-25")
            line()

            line("Study")
            line("Type", "Experiment")
            line("File List", "FirstVersionFileList.tsv")
            line()
        }.toString()

        webClient.uploadFiles(
            listOf(
                tempFolder.createFile("MultipleReferences.txt"),
                tempFolder.createFile("FirstVersionFileList.tsv", firstVersionFileList)
            )
        )

        assertThat(webClient.submitSingle(firstVersionPagetab, TSV)).isSuccessful()

        val firstVersion = submissionRepository.getExtByAccNo("S-FSTST4")
        assertThat(firstVersion.version).isEqualTo(1)
        assertThat(File("$submissionPath/${firstVersion.relPath}/Files/MultipleReferences.txt")).exists()

        val firstVersionReferencedFiles = submissionRepository.getReferencedFiles(firstVersion, "FirstVersionFileList")
        assertThat(firstVersionReferencedFiles).hasSize(2)
        assertThat(firstVersionReferencedFiles.first().attributes).containsExactly(ExtAttribute("Type", "Ref 1"))
        assertThat(firstVersionReferencedFiles.second().attributes).containsExactly(ExtAttribute("Type", "Ref 2"))

        webClient.deleteFile("MultipleReferences.txt")

        val secondVersionFileList = tsv {
            line("Files", "Type")
            line("MultipleReferences.txt", "A reference")
            line()
        }.toString()
        val secondVersionPagetab = tsv {
            line("Submission", "S-FSTST4")
            line("Title", "Multiple References")
            line("ReleaseDate", "2020-01-25")
            line()

            line("Study")
            line("Type", "Experiment")
            line("File List", "SecondVersionFileList.tsv")
            line()

            line("File", "MultipleReferences.txt")
            line("Type", "Another reference")
            line()
        }.toString()

        webClient.uploadFiles(listOf(tempFolder.createFile("SecondVersionFileList.tsv", secondVersionFileList)))
        assertThat(webClient.submitSingle(secondVersionPagetab, TSV)).isSuccessful()

        val secondVersion = submissionRepository.getExtByAccNo("S-FSTST4")
        assertThat(secondVersion.version).isEqualTo(2)
        assertThat(File("$submissionPath/${secondVersion.relPath}/Files/MultipleReferences.txt")).exists()

        val secondVersionFiles = secondVersion.allSectionsFiles
        assertThat(secondVersionFiles).hasSize(1)
        assertThat(secondVersionFiles.first().attributes).containsExactly(ExtAttribute("Type", "Another reference"))

        val secondVersionRefFiles = submissionRepository.getReferencedFiles(secondVersion, "SecondVersionFileList")
        assertThat(secondVersionRefFiles).hasSize(1)
        assertThat(secondVersionRefFiles.first().attributes).containsExactly(ExtAttribute("Type", "A reference"))
    }

    @Test
    fun `6-5 submission with group file`() {
        val groupName = "The-Group"
        val submission = tsv {
            line("Submission", "S-FSTST5")
            line("Title", "Sample Submission")
            line()

            line("Study")
            line()

            line("File", "groups/$groupName/GroupFile1.txt")
            line()

            line("File", "groups/$groupName/folder/GroupFile2.txt")
            line()
        }.toString()

        webClient.addUserInGroup(webClient.createGroup(groupName, "group-desc").name, SuperUser.email)
        webClient.uploadGroupFiles(groupName, listOf(tempFolder.createFile("GroupFile1.txt")))
        webClient.uploadGroupFiles(groupName, listOf(tempFolder.createFile("GroupFile2.txt")), "folder")

        assertThat(webClient.submitSingle(submission, TSV)).isSuccessful()
        val submitted = toSubmissionMapper.toSimpleSubmission(submissionRepository.getExtByAccNo("S-FSTST5"))
        assertThat(submitted).isEqualTo(
            submission("S-FSTST5") {
                title = "Sample Submission"
                section("Study") {
                    file("groups/$groupName/GroupFile1.txt")
                    file("groups/$groupName/folder/GroupFile2.txt")
                }
            }
        )
    }

    @Test
    @EnabledIfSystemProperty(named = "enableFire", matches = "true")
    fun `6-6 Submission bypassing fire`() {
        val submission = tsv {
            line("Submission", "S-FSTST6")
            line("Title", "Sample Submission")
            line("ReleaseDate", "2015-02-20")
            line()

            line("Study")
            line()

            line("File", "DataFile555.txt")
            line("dbMd5", "abc-123")
            line("dbId", "unique-id")
            line("dbPath", "/S-FSTST/006/S-FSTST6/Files/DataFile555.txt")
            line("dbPublished", true)
            line("dbSize", 145)
            line()
        }.toString()

        assertThat(webClient.submitSingle(submission, TSV)).isSuccessful()
        val submitted = submissionRepository.getExtByAccNo("S-FSTST6")

        assertThat(submitted.section.files[0]).hasLeftValueSatisfying {
            assertThat(it).isInstanceOf(FireFile::class.java)
            val fireFile = it as FireFile
            assertThat(fireFile.md5).isEqualTo("abc-123")
            assertThat(fireFile.fireId).isEqualTo("unique-id")
            assertThat(fireFile.size).isEqualTo(145)
            assertThat(fireFile.firePath).isEqualTo("/S-FSTST/006/S-FSTST6/Files/DataFile555.txt")
            assertThat(fireFile.relPath).isEqualTo("Files/DataFile555.txt")
            assertThat(fireFile.filePath).isEqualTo("DataFile555.txt")
            assertThat(fireFile.attributes).isEmpty()
        }
    }

    @Test
    fun `6-7 resubmission with SUBMISSION source ONLY`() {
        val submission = tsv {
            line("Submission", "S-FSTST7")
            line("Title", "Submission Source Only")
            line()

            line("Study", "SECT-001")
            line("Title", "Root Section")
            line()

            line("File", "test.txt")

            line()
        }.toString()

        val file = tempFolder.createFile("test.txt", "test content")
        webClient.uploadFiles(listOf(file))
        val filesConfig = SubmissionFilesConfig(emptyList(), storageMode)
        assertThat(webClient.submitSingle(submission, TSV, filesConfig)).isSuccessful()

        val firstVersion = submissionRepository.getExtByAccNo("S-FSTST7")
        val subFilesPath = "$submissionPath/${firstVersion.relPath}/Files"
        val innerFile = Paths.get("$subFilesPath/test.txt")

        assertThat(innerFile).exists()
        assertThat(innerFile.toFile().readText()).isEqualTo("test content")

        webClient.uploadFiles(listOf(tempFolder.createFile("test.txt", "updated test content")))

        assertThat(
            webClient.submitSingle(
                submission,
                TSV,
                SubmissionFilesConfig(emptyList(), storageMode, preferredSources = listOf(SUBMISSION)),
            )
        ).isSuccessful()
        assertThat(innerFile.toFile().readText()).isEqualTo("test content")

        if (enableFire) {
            val secondVersion = submissionRepository.getExtByAccNo("S-FSTST7")

            val firstVersionFireId = (firstVersion.allSectionsFiles.first() as FireFile).fireId
            val secondVersionFireId = (secondVersion.allSectionsFiles.first() as FireFile).fireId
            assertThat(firstVersionFireId).isEqualTo(secondVersionFireId)
        }
    }
}

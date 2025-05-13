package ac.uk.ebi.biostd.persistence.filesystem.pagetab

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat.Companion.JSON_PRETTY
import ac.uk.ebi.biostd.integration.SubFormat.Companion.TSV
import ac.uk.ebi.biostd.persistence.filesystem.extensions.permissions
import ebi.ac.uk.extended.mapping.to.ToFileListMapper
import ebi.ac.uk.extended.mapping.to.ToLinkListMapper
import ebi.ac.uk.extended.mapping.to.ToSubmissionMapper
import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.extended.model.ExtLinkList
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.allFileList
import ebi.ac.uk.extended.model.allLinkList
import ebi.ac.uk.io.FileUtils
import ebi.ac.uk.io.Permissions
import ebi.ac.uk.io.ext.newFile
import java.io.File

class PageTabUtil(
    private val serializationService: SerializationService,
    private val toSubmissionMapper: ToSubmissionMapper,
    private val fileListMapper: ToFileListMapper,
    private val linkListMapper: ToLinkListMapper,
) {
    suspend fun generateSubPageTab(
        extSub: ExtSubmission,
        target: File,
    ): PageTabFiles {
        val sub = toSubmissionMapper.toSimpleSubmission(extSub, filterBlindReview = true)
        val permissions = extSub.permissions()

        return PageTabFiles(
            json =
                saveTabFile(
                    target.resolve("${extSub.accNo}.json"),
                    serializationService.serializeSubmission(sub, JSON_PRETTY),
                    permissions.asPermissions(),
                ),
            tsv =
                saveTabFile(
                    target.resolve("${extSub.accNo}.tsv"),
                    serializationService.serializeSubmission(sub, TSV),
                    permissions.asPermissions(),
                ),
        )
    }

    suspend fun generateFileListPageTab(
        submission: ExtSubmission,
        filesFolder: File,
    ): Map<String, PageTabFiles> = submission.allFileList.associate { it.filePath to saveFileListTab(filesFolder, it) }

    suspend fun generateLinkListPageTab(
        submission: ExtSubmission,
        filesFolder: File,
    ): Map<String, PageTabFiles> = submission.allLinkList.associate { it.filePath to saveLinkListTab(filesFolder, it) }

    private suspend fun saveFileListTab(
        filesDir: File,
        fileList: ExtFileList,
    ): PageTabFiles {
        createFolderStructure(filesDir, fileList.filePath)
        val path = fileList.filePath
        return PageTabFiles(
            json = fileListMapper.serialize(fileList, JSON_PRETTY, filesDir.newFile("$path.json")),
            tsv = fileListMapper.serialize(fileList, TSV, filesDir.newFile("$path.tsv")),
        )
    }

    private suspend fun saveLinkListTab(
        filesDir: File,
        linkList: ExtLinkList,
    ): PageTabFiles {
        createFolderStructure(filesDir, linkList.filePath)
        val path = linkList.filePath
        return PageTabFiles(
            json = linkListMapper.serialize(linkList, JSON_PRETTY, filesDir.newFile("$path.json")),
            tsv = linkListMapper.serialize(linkList, TSV, filesDir.newFile("$path.tsv")),
        )
    }

    private fun createFolderStructure(
        folder: File,
        fileListPath: String,
    ) {
        val file = folder.resolve(fileListPath).parentFile
        file.mkdirs()
    }

    private fun saveTabFile(
        file: File,
        content: String,
        permissions: Permissions,
    ): File {
        FileUtils.writeContent(file, content, permissions)
        return file
    }
}

data class PageTabFiles(
    val json: File,
    val tsv: File,
)

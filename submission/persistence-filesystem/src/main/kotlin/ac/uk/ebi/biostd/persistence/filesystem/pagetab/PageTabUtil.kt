package ac.uk.ebi.biostd.persistence.filesystem.pagetab

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat.Companion.JSON_PRETTY
import ac.uk.ebi.biostd.integration.SubFormat.Companion.TSV
import ac.uk.ebi.biostd.persistence.filesystem.extensions.permissions
import ebi.ac.uk.base.Either
import ebi.ac.uk.extended.mapping.to.ToFileListMapper
import ebi.ac.uk.extended.mapping.to.ToSubmissionMapper
import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.allFileList
import ebi.ac.uk.io.FileUtils
import ebi.ac.uk.io.Permissions
import ebi.ac.uk.io.ext.newFile
import ebi.ac.uk.model.Section
import ebi.ac.uk.model.SectionsTable
import ebi.ac.uk.model.extensions.isAuthor
import ebi.ac.uk.model.extensions.isOrganization
import ebi.ac.uk.model.extensions.reviewType
import java.io.File

class PageTabUtil(
    private val serializationService: SerializationService,
    private val toSubmissionMapper: ToSubmissionMapper,
    private val fileListMapper: ToFileListMapper,
) {
    suspend fun generateSubPageTab(
        extSub: ExtSubmission,
        target: File,
    ): PageTabFiles {
        val sub = toSubmissionMapper.toSimpleSubmission(extSub)

        if (extSub.released.not() && sub.reviewType == DOUBLE_BLIND) {
            sub.section = filterBlindReview(sub.section) ?: Section()
        }

        val permissions = extSub.permissions()

        return PageTabFiles(
            json =
                saveTabFile(
                    target.resolve("${extSub.accNo}.json"),
                    serializationService.serializeSubmission(sub, JSON_PRETTY),
                    permissions,
                ),
            tsv =
                saveTabFile(
                    target.resolve("${extSub.accNo}.tsv"),
                    serializationService.serializeSubmission(sub, TSV),
                    permissions,
                ),
        )
    }

    private fun filterBlindReview(section: Section): Section? {
        if (section.isAuthor() || section.isOrganization()) return null

        val sections =
            section.sections.mapNotNull { either ->
                either.fold(
                    { section -> filterBlindReview(section)?.let { Either.Left(it) } },
                    { table -> Either.Right(SectionsTable(table.elements.mapNotNull { filterBlindReview(it) })) },
                )
            }

        section.sections = sections.toMutableList()

        return section
    }

    suspend fun generateFileListPageTab(
        submission: ExtSubmission,
        filesFolder: File,
    ): Map<String, PageTabFiles> = submission.allFileList.associate { it.filePath to saveTabFiles(filesFolder, it) }

    private suspend fun saveTabFiles(
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

    companion object {
        const val DOUBLE_BLIND = "DoubleBlind"
    }
}

data class PageTabFiles(
    val json: File,
    val tsv: File,
)

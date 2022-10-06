package ac.uk.ebi.biostd.persistence.filesystem.fire

import ac.uk.ebi.biostd.persistence.filesystem.api.FilesService
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileType.DIR
import ebi.ac.uk.extended.model.ExtFileType.FILE
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.io.ext.md5
import ebi.ac.uk.io.ext.size
import mu.KotlinLogging
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.extended.serialization.service.forEachFile
import uk.ac.ebi.fire.client.integration.web.FireClient
import java.io.File
import java.nio.file.Files

private val logger = KotlinLogging.logger {}

class FireFilesService(
    private val client: FireClient,
    private val fireTempDirPath: File,
    private val serializationService: ExtSerializationService,
) : FilesService {
    /**
     * Get or persist the given ext file from FIRE. Note that this method assumes that all the fire files belonging to
     * previous submission versions have been unpublished and any file with a path is assumed to be already used.
     *
     * For both FIRE and NFS, the file is searched by md5, and the system checks that it does not belong to another
     * submission. The method also ensures that the file has no path (i.e. it was submitted in the same submission in a
     * different path) and if so, even if the file exists in FIRE, it gets duplicated to ensure consistency.
     */
    override fun persistSubmissionFile(sub: ExtSubmission, file: ExtFile): ExtFile {
        return when (file) {
            is FireFile -> {
                val downloadFile = { client.downloadByFireId(file.fireId, file.fileName) }
                reuseOrPersistFireFile(file, sub.relPath, downloadFile)
            }
            is NfsFile -> {
                val nfsFile = if (file.type == FILE) file else asCompressedFile(sub.accNo, sub.version, file)
                val downloadFile = { nfsFile.file }
                return reuseOrPersistFireFile(nfsFile, sub.relPath, downloadFile)
            }
        }
    }

    private fun asCompressedFile(accNo: String, version: Int, directory: NfsFile): NfsFile {
        fun compress(file: File): File {
            val tempFolder = fireTempDirPath.resolve("$accNo/$version")
            tempFolder.mkdirs()

            val target = tempFolder.resolve("${file.name}.zip")
            Files.deleteIfExists(target.toPath())
            ZipUtil.pack(file, target)
            return target
        }

        val compressed = compress(directory.file)
        return directory.copy(
            filePath = "${directory.filePath}.zip",
            relPath = "${directory.relPath}.zip",
            file = compressed,
            fullPath = "${directory.fullPath}.zip",
            md5 = compressed.md5(),
            size = compressed.size(),
            type = DIR
        )
    }

    @Suppress("ReturnCount")
    private fun reuseOrPersistFireFile(
        file: ExtFile,
        subRelPath: String,
        fallbackFile: () -> File,
    ): ExtFile {
        val expectedPath = "/$subRelPath/${file.relPath}"
        val files = client.findByMd5(file.md5)

        val byPath = files.firstOrNull { it.filesystemEntry?.path == expectedPath }
        if (byPath != null) return asFireFile(file, byPath.fireOid)

        val noPath = files.firstOrNull { it.filesystemEntry?.path == null }
        if (noPath != null) return setMetadata(noPath.fireOid, file, expectedPath)

        val saved = client.save(fallbackFile(), file.md5, file.size)
        return setMetadata(saved.fireOid, file, expectedPath)
    }

    private fun setMetadata(fireOid: String, file: ExtFile, expectedPath: String): FireFile {
        client.setPath(fireOid, expectedPath)
        return asFireFile(file, fireOid)
    }

    private fun asFireFile(file: ExtFile, fireId: String): FireFile = FireFile(
        filePath = file.filePath,
        relPath = file.relPath,
        fireId = fireId,
        md5 = file.md5,
        size = file.size,
        type = file.type,
        attributes = file.attributes
    )

    override fun postProcessSubmissionFiles(sub: ExtSubmission) {
        // No need of post-processing on FIRE
    }

    override fun cleanSubmissionFiles(sub: ExtSubmission) {
        fun cleanFile(file: FireFile, index: Int) {
            logger.debug { "${sub.accNo}, ${sub.version} Cleaning file $index, path='${file.filePath}'" }
            client.unsetPath(file.fireId)
            client.unpublish(file.fireId)
            logger.debug { "${sub.accNo}, ${sub.version} Cleaning file $index, path='${file.filePath}'" }
        }

        logger.info { "${sub.accNo} ${sub.owner} Cleaning Current submission Folder for ${sub.accNo}" }
        serializationService.forEachFile(sub) { file, index -> if (file is FireFile) cleanFile(file, index) }
        logger.info { "${sub.accNo} ${sub.owner} Cleaning Ftp Folder for ${sub.accNo}" }
    }
}

package ac.uk.ebi.biostd.persistence.filesystem.fire

import ac.uk.ebi.biostd.persistence.filesystem.api.FilesService
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileType.DIR
import ebi.ac.uk.extended.model.ExtFileType.FILE
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.extended.model.expectedPath
import ebi.ac.uk.io.ext.md5
import ebi.ac.uk.io.ext.size
import mu.KotlinLogging
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.extended.serialization.service.fileSequence
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
     * different path) and if so, even if the file exists in FIRE, it gets duplicated to ensure consistency. TODO:
     * handle scenario when the same file appear two times in the same submission and it was already in fire.
     */
    override fun persistSubmissionFile(sub: ExtSubmission, file: ExtFile): FireFile {
        return when (file) {
            is FireFile -> getOrCreate(file, sub.expectedPath(file))
            is NfsFile -> {
                val nfsFile = if (file.type == FILE) file else asCompressedFile(sub.accNo, sub.version, file)
                return getOrCreate(nfsFile, sub.expectedPath(nfsFile))
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

    private fun getOrCreate(
        file: FireFile,
        expectedPath: String,
    ): FireFile {
        return when (file.firePath) {
            expectedPath -> file
            null -> setMetadata(file.fireId, file, expectedPath, file.published)
            else -> {
                val downloaded = client.downloadByFireId(file.fireId, file.fileName)
                val saved = client.save(downloaded, file.md5, file.size)
                setMetadata(saved.fireOid, file, expectedPath, false)
            }
        }
    }

    private fun getOrCreate(file: NfsFile, expectedPath: String): FireFile {
        val matches = client.findByMd5(file.md5)
        val apiFile = matches.find { it.path == expectedPath }
            ?: matches.find { it.path == null }
            ?: client.save(file.file, file.md5, file.size)
        val fireFile = asFireFile(file, apiFile.fireOid, apiFile.path, apiFile.published)
        return getOrCreate(fireFile, expectedPath)
    }

    private fun setMetadata(fireOid: String, file: ExtFile, expectedPath: String, published: Boolean): FireFile {
        client.setPath(fireOid, expectedPath)
        return asFireFile(file, fireOid, expectedPath, published)
    }

    private fun asFireFile(file: ExtFile, fireId: String, firePath: String?, published: Boolean): FireFile = FireFile(
        fireId = fireId,
        firePath = firePath,
        published = published,
        filePath = file.filePath,
        relPath = file.relPath,
        md5 = file.md5,
        size = file.size,
        type = file.type,
        attributes = file.attributes
    )

    override fun postProcessSubmissionFiles(sub: ExtSubmission) {
        // No need of post-processing on FIRE
    }

    override fun cleanSubmissionFiles(sub: ExtSubmission) {
        logger.info { "${sub.accNo} ${sub.owner} Started cleaning submission files for ${sub.accNo}" }
        serializationService
            .fileSequence(sub)
            .filterIsInstance(FireFile::class.java)
            .forEachIndexed { index, file -> cleanFile(sub.accNo, sub.version, index, file) }
        logger.info { "${sub.accNo} ${sub.owner} Finished cleaning submission files for ${sub.accNo}" }
    }

    override fun cleanCommonFiles(new: ExtSubmission, current: ExtSubmission) {
        val newFiles = createFileEntrySet(new)
        val previousFiles = createFileEntrySet(current)

        cleanFiles(current, newFiles.intersect(previousFiles))
    }

    override fun cleanRemainingFiles(new: ExtSubmission, current: ExtSubmission) {
        val newFiles = createFileEntrySet(new)
        val previousFiles = createFileEntrySet(current)

        cleanFiles(current, previousFiles.subtract(newFiles))
    }

    private fun cleanFiles(sub: ExtSubmission, filesToClean: Set<FileEntry>) {
        logger.info { "${sub.accNo} ${sub.owner} Started cleaning submission files for ${sub.accNo}" }
        serializationService.fileSequence(sub)
            .filterIsInstance(FireFile::class.java)
            .filter { filesToClean.contains(FileEntry(it.md5, it.firePath!!)) }
            .forEachIndexed { index, file -> cleanFile(sub.accNo, sub.version, index, file) }
        logger.info { "${sub.accNo} ${sub.owner} Finished cleaning Ftp Folder for ${sub.accNo}" }
    }

    private fun cleanFile(accNo: String, version: Int, index: Int, file: FireFile) {
        logger.debug { "$accNo, $version Cleaning file $index, path='${file.filePath}'" }
        client.unsetPath(file.fireId)
        client.unpublish(file.fireId)
        logger.debug { "$accNo, $version Cleaning file $index, path='${file.filePath}'" }
    }

    private fun createFileEntrySet(sub: ExtSubmission): Set<FileEntry> =
        serializationService.fileSequence(sub)
            .filterIsInstance(FireFile::class.java)
            .map { FileEntry(it.md5, sub.expectedPath(it)) }
            .toSet()

    data class FileEntry(val md5: String, val path: String)
}

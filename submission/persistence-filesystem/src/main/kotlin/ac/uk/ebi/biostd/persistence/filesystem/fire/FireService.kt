package ac.uk.ebi.biostd.persistence.filesystem.fire

import ac.uk.ebi.biostd.persistence.filesystem.extensions.fireType
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileType.DIR
import ebi.ac.uk.extended.model.ExtFileType.FILE
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.NfsFile
import mu.KotlinLogging
import org.zeroturnaround.zip.ZipUtil
import uk.ac.ebi.fire.client.integration.web.FireClient
import uk.ac.ebi.fire.client.model.isAvailable
import uk.ac.ebi.fire.client.model.path
import java.io.File
import java.nio.file.Files

private val logger = KotlinLogging.logger {}

class FireService(
    private val client: FireClient,
    private val fireTempDirPath: File,
) {

    fun cleanFtp(sub: ExtSubmission) {
        logger.info { "Started cleaning FTP files (unset path) of files in submission ${sub.accNo}" }
        client
            .findByAccNo(sub.accNo)
            .apply { logger.info { "Found $size files in submission ${sub.accNo}" } }
            .forEach { client.unsetPath(it.fireOid) }
        logger.info { "Finished cleaning FTP files (unset path) of files in submission ${sub.accNo}" }
    }

    /**
     * Get or persist the given ext file from FIRE. Note that this method assumes that all the fire files belonging to
     * previous submission versions have been unpublished and any file with a path is assumed to be already used.
     *
     * For both FIRE and NFS, the file is searched by md5, and the system checks that it does not belong to another
     * submission. The method also ensures that the file has no path (i.e. it was submitted in the same submission in a
     * different path) and if so, even if the file exists in FIRE, it gets duplicated to ensure consistency.
     */
    fun getOrPersist(sub: ExtSubmission, file: ExtFile): FireFile = when (file) {
        is FireFile -> fromFireFile(sub, file, "${sub.relPath}/${file.relPath}")
        is NfsFile -> when (file.type) {
            FILE -> fromNfsFile(sub, file, "${sub.relPath}/${file.relPath}")
            DIR -> fromNfsFile(sub, file.copy(file = compress(sub, file.file)), "${sub.relPath}/${file.relPath}.zip")
        }
    }

    private fun fromNfsFile(sub: ExtSubmission, file: NfsFile, expectedPath: String): FireFile =
        reuseOrPersistFireFile(sub, file, expectedPath) { file.file }

    private fun fromFireFile(sub: ExtSubmission, file: FireFile, expectedPath: String): FireFile =
        reuseOrPersistFireFile(sub, file, expectedPath) { client.downloadByFireId(file.fireId, file.fileName) }

    @Suppress("ReturnCount")
    private fun reuseOrPersistFireFile(
        sub: ExtSubmission,
        file: ExtFile,
        expectedPath: String,
        fallbackFile: () -> File
    ): FireFile {
        val fireFile = client.findByMd5(file.md5).firstOrNull { it.isAvailable(sub.accNo) }
        if (fireFile != null) {
            if (fireFile.path == null) return setMetadata(sub, fireFile.fireOid, file, expectedPath)
            if (fireFile.path == expectedPath) return asFireFile(file, fireFile.fireOid)
        }

        val saved = client.save(fallbackFile(), file.md5)
        return setMetadata(sub, saved.fireOid, file, expectedPath)
    }

    private fun setMetadata(sub: ExtSubmission, fireOid: String, file: ExtFile, path: String): FireFile {
        client.setBioMetadata(fireOid, sub.accNo, file.fireType, published = false)
        client.setPath(fireOid, path)
        return asFireFile(file, fireOid)
    }

    private fun compress(sub: ExtSubmission, file: File): File {
        val tempFolder = fireTempDirPath.resolve("${sub.accNo}/${sub.version}")
        tempFolder.mkdirs()

        val target = tempFolder.resolve(file.name)
        Files.deleteIfExists(target.toPath())
        ZipUtil.pack(file, target)

        return target
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
}

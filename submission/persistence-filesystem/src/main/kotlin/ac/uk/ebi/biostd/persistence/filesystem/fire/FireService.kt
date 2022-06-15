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
        logger.info { "Clean FTP files (unset path) of files in submission ${sub.accNo}" }
        client
            .findByAccNo(sub.accNo)
            .apply { logger.info { "Found $size files in submission ${sub.accNo}" } }
            .forEach { client.unsetPath(it.fireOid) }
        logger.info { "Finish clean FTP files (unset path) of files in submission ${sub.accNo}" }
    }

    /**
     * Get or persist the given ext file. Note that this method assumes all previous submission versions  fire files has
     * been unpublished and any file with path is assumed to be already used.
     *
     * For both fire and nfs file, fire file is search by md5 and system checks that it does not bellow to another
     * accNo (another submission), we also ensure file has no path (submitted in the same submission in a different
     * path). If so even is file exists in fire we duplicated.
     */
    fun getOrPersist(sub: ExtSubmission, file: ExtFile): FireFile = when (file) {
        is FireFile -> fromFireFile(sub, file, "${sub.relPath}/${file.relPath}")
        is NfsFile -> when (file.type) {
            FILE -> fromNfsFile(sub, file, "${sub.relPath}/${file.relPath}")
            DIR -> fromNfsFile(sub, file.copy(file = compress(sub, file.file)), "${sub.relPath}/${file.relPath}.zip")
        }
    }

    @Suppress("ReturnCount")
    private fun fromNfsFile(sub: ExtSubmission, file: NfsFile, expectedPath: String): FireFile {
        val fireFile = client.findByMd5(file.md5).firstOrNull { it.isAvailable(sub.accNo) }
        if (fireFile != null) {
            if (fireFile.path == null) return saveFile(sub, fireFile.fireOid, file, expectedPath)
            if (fireFile.path == expectedPath) return asFireFile(file, fireFile.fireOid)
        }

        val newFile = client.save(file.file, file.md5)
        return saveFile(sub, newFile.fireOid, file, expectedPath)
    }

    @Suppress("ReturnCount")
    private fun fromFireFile(sub: ExtSubmission, file: FireFile, expectedPath: String): FireFile {
        val fireFile = client.findByMd5(file.md5).first()
        if (fireFile.isAvailable(sub.accNo)) {
            if (fireFile.path == null) return saveFile(sub, fireFile.fireOid, file, expectedPath)
            if (fireFile.path == expectedPath) return asFireFile(file, fireFile.fireOid)
        }

        val downloadFile = client.downloadByFireId(fireFile.fireOid, file.fileName)
        val saved = client.save(downloadFile, file.md5)
        return saveFile(sub, saved.fireOid, file, expectedPath)
    }

    private fun saveFile(sub: ExtSubmission, fireOid: String, file: ExtFile, path: String): FireFile {
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

package ac.uk.ebi.biostd.persistence.filesystem.fire

import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileType
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.io.ext.md5
import mu.KotlinLogging
import org.zeroturnaround.zip.ZipUtil
import uk.ac.ebi.fire.client.integration.web.FireClient
import uk.ac.ebi.fire.client.model.FireApiFile
import uk.ac.ebi.fire.client.model.hasNoPath
import uk.ac.ebi.fire.client.model.isAvailable
import java.io.File
import java.nio.file.Files

private val logger = KotlinLogging.logger {}

class FireService(
    private val fireClient: FireClient,
    private val fireTempDirPath: File,
) {
    fun getOrPersist(sub: ExtSubmission, file: ExtFile): FireFile {
        return when (file) {
            is FireFile -> reuseFireFile(sub, file)
            is NfsFile -> getOrPersist(sub, file)
        }
    }

    fun cleanFtp(sub: ExtSubmission) {
        fireClient
            .findByAccNo(sub.accNo)
            .forEach { fireClient.unsetPath(it.fireOid) }
    }

    private fun reuseFireFile(sub: ExtSubmission, fireFile: FireFile): FireFile {
        val newPath = "${sub.relPath}/${fireFile.relPath}"
        fireClient.setPath(fireFile.fireId, newPath)
        return fireFile
    }

    private fun getOrPersist(sub: ExtSubmission, file: NfsFile): FireFile =
        when (val record = findFile(sub, file)) {
            null -> if (file.file.isDirectory) saveDirectory(sub, file) else saveFile(sub, file)
            else -> asFireFile(file, record, file.type)
        }

    /**
     * Try to find the fire file for the current nfs file. Some considerations:
     *
     * 1. We search by md5 and check that does not bellow to another accNo (another submission), we also ensure file
     * has no path (submitted in the same submission in a different path).
     * 2. We search by path in the case it has been already submitted by the current submission.
     */
    private fun findFile(sub: ExtSubmission, file: NfsFile): FireApiFile? {
        val fireFile = fireClient.findByMd5(file.md5).firstOrNull { it.isAvailable(sub.accNo) && it.hasNoPath() }
        return fireFile ?: fireClient.findByPath("${sub.relPath}/${file.relPath}")
    }

    private fun saveFile(sub: ExtSubmission, nfsFile: NfsFile): FireFile {
        logger.info { "${sub.accNo} ${sub.owner} Persisting file ${nfsFile.fileName} on FIRE" }
        val fireFile = fireClient.save(nfsFile.file, nfsFile.md5)
        fireClient.setBioMetadata(fireFile.fireOid, sub.accNo, nfsFile.type.value, published = false)
        fireClient.setPath(fireFile.fireOid, "${sub.relPath}/${nfsFile.relPath}")
        return asFireFile(nfsFile, fireFile, ExtFileType.FILE)
    }

    private fun saveDirectory(sub: ExtSubmission, nfsFile: NfsFile): FireFile {
        logger.info { "${sub.accNo} ${sub.owner} Persisting ${nfsFile.fileName}.zip on FIRE" }
        val directory = compress(sub, nfsFile.file)
        val fireFile = fireClient.save(directory, directory.md5())
        fireClient.setBioMetadata(fireFile.fireOid, sub.accNo, nfsFile.type.value, published = false)
        fireClient.setPath(fireFile.fireOid, "${sub.relPath}/${nfsFile.relPath}.zip")
        return asFireFile(nfsFile, fireFile, ExtFileType.DIR)
    }

    private fun compress(sub: ExtSubmission, file: File): File {
        val tempFolder = fireTempDirPath.resolve("${sub.accNo}/${sub.version}")
        tempFolder.mkdirs()

        val target = tempFolder.resolve(file.name)
        Files.deleteIfExists(target.toPath())
        ZipUtil.pack(file, target)
        return target
    }

    private fun asFireFile(nfsFile: NfsFile, fireFile: FireApiFile, type: ExtFileType): FireFile = FireFile(
        filePath = nfsFile.filePath,
        relPath = nfsFile.relPath,
        fireId = fireFile.fireOid,
        md5 = fireFile.objectMd5,
        size = fireFile.objectSize.toLong(),
        type = type,
        attributes = nfsFile.attributes
    )
}

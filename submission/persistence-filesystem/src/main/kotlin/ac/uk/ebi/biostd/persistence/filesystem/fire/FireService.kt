package ac.uk.ebi.biostd.persistence.filesystem.fire

import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileType
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.io.ext.md5
import org.zeroturnaround.zip.ZipUtil
import uk.ac.ebi.fire.client.integration.web.FireWebClient
import uk.ac.ebi.fire.client.model.FireApiFile
import java.io.File
import java.nio.file.Files

class FireService(
    private val fireWebClient: FireWebClient,
    private val fireTempDirPath: File,
) {

    fun getOrPersist(sub: ExtSubmission, file: ExtFile): FireFile {
        return when (file) {
            is FireFile -> reuseFireFile(sub, file)
            is NfsFile -> getOrPersist(sub, file)
        }
    }

    fun cleanFtp(sub: ExtSubmission) {
        fireWebClient
            .findByAccNo(sub.accNo)
            .forEach { fireWebClient.unsetPath(it.fireOid) }
    }

    private fun reuseFireFile(sub: ExtSubmission, fireFile: FireFile): FireFile {
        val newPath = "${sub.relPath}/${fireFile.relPath}"
        fireWebClient.setPath(fireFile.fireId, newPath)
        return fireFile
    }

    private fun getOrPersist(sub: ExtSubmission, nfsFile: NfsFile): FireFile {
        return when (val record = fireWebClient.findByPath("${sub.relPath}/${nfsFile.relPath}")) {
            null -> if (nfsFile.file.isDirectory) saveDirectory(sub, nfsFile) else saveFile(sub, nfsFile)
            else -> asFireFile(nfsFile, record, nfsFile.type)
        }
    }

    private fun saveFile(sub: ExtSubmission, nfsFile: NfsFile): FireFile {
        val fireFile = fireWebClient.save(nfsFile.file, nfsFile.md5)
        fireWebClient.setBioMetadata(fireFile.fireOid, sub.accNo, nfsFile.type.value, published = false)
        fireWebClient.setPath(fireFile.fireOid, "${sub.relPath}/${nfsFile.relPath}")
        return asFireFile(nfsFile, fireFile, ExtFileType.FILE)
    }

    private fun saveDirectory(sub: ExtSubmission, nfsFile: NfsFile): FireFile {
        val directory = compress(sub, nfsFile.file)
        val fireFile = fireWebClient.save(directory, directory.md5())
        fireWebClient.setBioMetadata(fireFile.fireOid, sub.accNo, nfsFile.type.value, published = false)
        fireWebClient.setPath(fireFile.fireOid, "${sub.relPath}/${nfsFile.relPath}.zip")
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

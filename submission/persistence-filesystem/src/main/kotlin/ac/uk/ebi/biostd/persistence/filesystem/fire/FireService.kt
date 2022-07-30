package ac.uk.ebi.biostd.persistence.filesystem.fire

import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileType.DIR
import ebi.ac.uk.extended.model.ExtFileType.FILE
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.io.ext.md5
import ebi.ac.uk.io.ext.size
import uk.ac.ebi.fire.client.integration.web.FireClient
import java.io.File
import java.nio.file.Files

class FireService(
    private val client: FireClient,
    private val fireTempDirPath: File,
) {
    fun cleanFile(file: FireFile) {
        client.unsetPath(file.fireId)
        client.unpublish(file.fireId)
    }

    /**
     * Get or persist the given ext file from FIRE. Note that this method assumes that all the fire files belonging to
     * previous submission versions have been unpublished and any file with a path is assumed to be already used.
     *
     * For both FIRE and NFS, the file is searched by md5, and the system checks that it does not belong to another
     * submission. The method also ensures that the file has no path (i.e. it was submitted in the same submission in a
     * different path) and if so, even if the file exists in FIRE, it gets duplicated to ensure consistency.
     */
    fun getOrPersist(sub: ExtSubmission, file: ExtFile): FirePersistResult {
        return when (file) {
            is FireFile -> {
                val downloadFile = { client.downloadByFireId(file.fireId, file.fileName) }
                reuseOrPersistFireFile(file, sub.relPath, downloadFile)
            }
            is NfsFile -> {
                val nfsFile = if (file.type == FILE) file else asCompressedFile(sub, file)
                val downloadFile = { nfsFile.file }
                return reuseOrPersistFireFile(nfsFile, sub.relPath, downloadFile)
            }
        }
    }

    private fun asCompressedFile(sub: ExtSubmission, directory: NfsFile): NfsFile {
        fun compress(file: File): File {
            val tempFolder = fireTempDirPath.resolve("${sub.accNo}/${sub.version}")
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
        subRelpath: String,
        fallbackFile: () -> File,
    ): FirePersistResult {
        val expectedPath = "/$subRelpath/${file.relPath}"
        val files = client.findByMd5(file.md5)

        val byPath = files.firstOrNull { it.filesystemEntry?.path == expectedPath }
        if (byPath != null) return FirePersistResult(asFireFile(file, byPath.fireOid), false)

        val noPath = files.firstOrNull { it.filesystemEntry?.path == null }
        if (noPath != null) return FirePersistResult(setMetadata(noPath.fireOid, file, expectedPath), false)

        val saved = client.save(fallbackFile(), file.md5, file.size)
        return FirePersistResult(setMetadata(saved.fireOid, file, expectedPath), true)
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
}

data class FirePersistResult(val file: FireFile, val created: Boolean)

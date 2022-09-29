package ac.uk.ebi.biostd.persistence.filesystem.nfs

import ac.uk.ebi.biostd.persistence.filesystem.api.FilePersistenceRequest
import ac.uk.ebi.biostd.persistence.filesystem.api.FilesService
import ac.uk.ebi.biostd.persistence.filesystem.api.NfsFilePersistenceRequest
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.io.FileUtils
import ebi.ac.uk.io.FileUtils.copyOrReplaceFile
import ebi.ac.uk.io.FileUtils.moveFile
import ebi.ac.uk.io.ext.md5
import ebi.ac.uk.io.ext.notExist

class NfsFilesService : FilesService {
    override fun cleanSubmissionFile(file: ExtFile) {
        val nfsFile = file as NfsFile
        FileUtils.deleteFile(nfsFile.file)
    }

    override fun persistSubmissionFile(request: FilePersistenceRequest): ExtFile {
        val (file, subFolder, targetFolder, permissions) = request as NfsFilePersistenceRequest
        val target = targetFolder.resolve(file.relPath)
        val subFile = subFolder.resolve(file.relPath)

        if (target.notExist() && subFile.exists() && subFile.md5() == file.md5)
            moveFile(subFile, target, permissions)
        else if (target.notExist())
            copyOrReplaceFile(file.file, target, permissions)

        return file.copy(fullPath = subFile.absolutePath, file = subFile)
    }
}

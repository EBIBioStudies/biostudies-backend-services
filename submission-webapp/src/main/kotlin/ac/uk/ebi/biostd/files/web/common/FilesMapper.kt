package ac.uk.ebi.biostd.files.web.common

import ac.uk.ebi.biostd.files.model.FilesSpec
import ebi.ac.uk.api.UserFile
import ebi.ac.uk.api.UserFileType
import java.io.File
import java.nio.file.Path

private const val USER_FOLDER_NAME = "User"

class FilesMapper {

    fun asUserFiles(fileSpec: FilesSpec) =
        fileSpec.files.map { UserFile(it.name, getPath(it, fileSpec.source, USER_FOLDER_NAME), it.length(), UserFileType.getType(it)) }

    fun asGroupFiles(groupName: String, fileSpec: FilesSpec) =
        fileSpec.files.map { UserFile(it.name, getPath(it, fileSpec.source, "Groups/$groupName"), it.length(), UserFileType.getType(it)) }

    private fun getPath(file: File, source: Path, replacement: String) =
        file.absolutePath.replace(source.toAbsolutePath().toString(), replacement)
}

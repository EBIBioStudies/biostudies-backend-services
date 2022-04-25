package ebi.ac.uk.io.sources

import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileOrigin
import ebi.ac.uk.model.Attribute
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

class PathFilesSource(
    private val sourcePath: Path,
    override val filesOrigin: ExtFileOrigin
) : FilesSource {
    override fun getExtFile(
        path: String,
        md5: String?,
        attributes: List<Attribute>,
        preferredOrigin: ExtFileOrigin
    ): ExtFile? = findFile(path)?.let { create(path, it, attributes, filesOrigin) }

    override fun getFile(path: String, md5: String?): File? = findFile(path)

    private fun findFile(path: String): File? {
        val filePath = sourcePath.resolve(path)
        return if (Files.exists(filePath)) filePath.toFile() else null
    }
}

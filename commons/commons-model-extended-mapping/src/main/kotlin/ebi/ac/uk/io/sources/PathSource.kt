package ebi.ac.uk.io.sources

import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.model.Attribute
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

class PathSource(override val description: String, private val sourcePath: Path) : FilesSource {
    override fun getExtFile(path: String, attributes: List<Attribute>): ExtFile? =
        findFile(path)?.let { create(path, it, attributes) }

    override fun getFile(path: String): File? = findFile(path)

    private fun findFile(path: String): File? {
        val filePath = sourcePath.resolve(path)
        return if (Files.exists(filePath)) filePath.toFile() else null
    }
}

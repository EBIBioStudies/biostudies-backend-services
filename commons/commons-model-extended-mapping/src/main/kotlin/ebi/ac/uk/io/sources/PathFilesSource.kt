package ebi.ac.uk.io.sources

import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.model.Attribute
import java.nio.file.Files
import java.nio.file.Path

class PathFilesSource(private val sourcePath: Path) : FilesSource {
    override fun getFile(
        path: String,
        md5: String?,
        attributes: List<Attribute>,
        calculateProperties: Boolean
    ): ExtFile? {
        val filePath = sourcePath.resolve(path)
        return if (Files.exists(filePath)) create(path, filePath.toFile(), calculateProperties, attributes) else null
    }
}

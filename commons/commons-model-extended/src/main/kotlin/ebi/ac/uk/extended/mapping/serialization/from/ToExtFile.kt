package ebi.ac.uk.extended.mapping.serialization.from

import com.google.common.hash.Hashing
import com.google.common.io.Files
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.model.File

internal const val TO_EXT_FILE_EXTENSIONS = "ebi.ac.uk.extended.mapping.serialization.from.ToExtFileKt"

fun File.toExtFile(fileSource: FilesSource): ExtFile {
    val file = fileSource.getFile(path)
    return ExtFile(path, md5(file), file, attributes.map { it.toExtAttribute() })
}

private fun md5(file: java.io.File): String = Files.asByteSource(file).hash(Hashing.md5()).toString()

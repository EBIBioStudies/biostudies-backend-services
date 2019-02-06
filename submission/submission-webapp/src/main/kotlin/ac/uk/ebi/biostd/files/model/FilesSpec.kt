package ac.uk.ebi.biostd.files.model

import java.io.File
import java.nio.file.Path

data class FilesSpec(val source: Path, val files: List<File>)

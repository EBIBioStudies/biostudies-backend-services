package ebi.ac.uk.model

import java.io.File

data class FileList(var name: String, val file: File)

val FileList.canonicalName
    get() = name.substringBeforeLast(".")

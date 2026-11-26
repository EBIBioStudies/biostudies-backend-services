package uk.ac.ebi.biostd.model

import java.io.File

data class FileList(var name: String, val file: File)

val FileList.canonicalName
    get() = name.substringBeforeLast(".")

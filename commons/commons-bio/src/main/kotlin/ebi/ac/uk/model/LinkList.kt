package ebi.ac.uk.model

import java.io.File

data class LinkList(
    var name: String,
    val file: File,
)

val LinkList.canonicalName
    get() = name.substringBeforeLast(".")

package ac.uk.ebi.biostd.itest.common

import java.io.File

private val remainingDirectories = setOf("submission", "request-files", "dropbox", "magic", "tmp")

fun File.clean() {
    listFiles()?.forEach {
        if (it.isFile) {
            it.delete()
        } else {
            if (it.name in remainingDirectories) it.cleanDirectory() else it.deleteRecursively()
        }
    }
}

private fun File.cleanDirectory(): File {
    listFiles()?.forEach { it.deleteRecursively() }
    return this
}

package ebi.ac.uk.io

import java.nio.file.Files
import java.nio.file.Path

fun Files.deleteIfExist(path: Path) {
    if (Files.exists(path)) {
        Files.walk(path)
            .sorted(Comparator.reverseOrder())
            .forEach { Files.deleteIfExists(it) }
    }
}

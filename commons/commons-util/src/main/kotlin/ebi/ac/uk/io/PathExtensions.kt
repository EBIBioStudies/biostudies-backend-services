package ebi.ac.uk.io

import java.nio.file.Path

fun Path.exist() = this.toFile().exists()

fun Path.notExist() = this.toFile().notExist()

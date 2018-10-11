package ebi.ac.uk.io

import java.io.File

fun File.notExist() = this.exists().not()
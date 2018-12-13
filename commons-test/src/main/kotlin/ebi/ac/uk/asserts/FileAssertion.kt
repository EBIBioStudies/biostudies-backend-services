package ebi.ac.uk.asserts

import arrow.core.Either
import ebi.ac.uk.model.File
import ebi.ac.uk.model.FilesTable
import org.assertj.core.api.AbstractAssert
import org.assertj.core.api.Assertions

fun assertThat(file: File): FileAssert {
    return FileAssert(file)
}

fun assertThat(file: Either<File, FilesTable>): FileEitherAssert {
    return FileEitherAssert(file)
}

class FileAssert(actual: File) :
    AbstractAssert<FileAssert, File>(actual, FileAssert::class.java)

class FileEitherAssert(actual: Either<File, FilesTable>) :
    AbstractAssert<FileEitherAssert, Either<File, FilesTable>>(actual, FileEitherAssert::class.java) {

    fun isFile(): File {
        Assertions.assertThat(actual.isLeft())
        return actual.getLeft()
    }

    fun isTable(): FilesTable {
        Assertions.assertThat(actual.isRight())
        return actual.getRight()
    }
}

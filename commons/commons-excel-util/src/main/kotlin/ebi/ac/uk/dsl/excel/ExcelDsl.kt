package ebi.ac.uk.dsl.excel

import java.io.File

fun excel(file: File, bookBuilder: ExcelBook.() -> Unit): File {
    val book = ExcelBook()
    book.bookBuilder()
    book.write(file.outputStream())

    return file
}

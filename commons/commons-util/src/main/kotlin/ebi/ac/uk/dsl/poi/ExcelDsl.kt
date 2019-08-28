package ebi.ac.uk.dsl.poi

import java.io.File
import java.io.OutputStream

fun excel(filename: String, bookBuilder: ExcelBook.() -> Unit): File {
    val file = File(filename)
    excelBook(file.outputStream(), bookBuilder)

    return file
}

fun excelBook(out: OutputStream, bookBuilder: ExcelBook.() -> Unit) {
    val excelBook = ExcelBook()
    excelBook.bookBuilder()
    excelBook.write(out)
}

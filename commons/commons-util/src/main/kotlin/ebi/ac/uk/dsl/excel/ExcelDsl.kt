package ebi.ac.uk.dsl.excel

import java.io.File

fun excel(filename: String, bookBuilder: ExcelBook.() -> Unit) = File(filename).also { writeExcelFile(it, bookBuilder) }

private fun writeExcelFile(file: File, bookBuilder: ExcelBook.() -> Unit) =
    ExcelBook().apply {
        bookBuilder()
        write(file.outputStream())
    }

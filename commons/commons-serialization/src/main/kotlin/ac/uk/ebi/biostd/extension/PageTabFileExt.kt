package ac.uk.ebi.biostd.extension

import ac.uk.ebi.biostd.exception.EmptyPageTabFileException
import ac.uk.ebi.biostd.integration.SubFormat
import ac.uk.ebi.biostd.integration.SubFormat.TsvFormat.XlsxTsv
import ebi.ac.uk.io.ext.size
import ebi.ac.uk.util.file.ExcelReader.readContentAsTsv
import java.io.File

internal fun File.readAsPageTab(): String {
    require(size() > 0) { throw EmptyPageTabFileException(name) }

    return if (SubFormat.fromFile(this) == XlsxTsv) readContentAsTsv(this) else readText()
}

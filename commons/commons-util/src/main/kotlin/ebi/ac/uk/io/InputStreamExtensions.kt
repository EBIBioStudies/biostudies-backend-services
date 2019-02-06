package ebi.ac.uk.io

import org.apache.commons.io.IOUtils
import java.io.InputStream
import java.io.StringWriter
import java.nio.charset.StandardCharsets

fun InputStream.asString(): String {
    val writer = StringWriter()
    IOUtils.copy(this, writer, StandardCharsets.UTF_8)
    return writer.toString()
}

package ac.uk.ebi.biostd.itest.common

import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.FileOutputStream

const val PROPERTY_FILE = "application.yml"
const val BASE_PATH_PLACEHOLDER = "{BASE_PATH}"

fun setAppProperty(placeHolder: String, value: String) {
    val resource = Thread.currentThread().contextClassLoader.getResource(PROPERTY_FILE)
    val myFile = File(resource.toURI())
    val content: String = FileUtils.readFileToString(myFile, Charsets.UTF_8)
    IOUtils.write(content.replace(placeHolder, value), FileOutputStream(myFile), Charsets.UTF_8)
}

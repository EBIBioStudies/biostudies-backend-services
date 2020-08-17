package ebi.ac.uk.notifications.util

import org.apache.commons.io.IOUtils
import org.springframework.core.io.ResourceLoader
import java.nio.charset.Charset

class TemplateLoader(private val resourceLoader: ResourceLoader) {
    fun loadTemplate(templateName: String): String {
        val resource = resourceLoader.getResource("classpath:templates/$templateName")
        return IOUtils.toString(resource.inputStream, Charset.defaultCharset())
    }
}

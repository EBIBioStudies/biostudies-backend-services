package ebi.ac.uk.notifications.util

import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.util.collections.firstOrElse
import org.apache.commons.io.IOUtils
import org.springframework.core.io.ResourceLoader
import java.nio.charset.Charset

internal const val DEFAULT_TEMPLATE = "Default"

class TemplateLoader(private val resourceLoader: ResourceLoader) {
    fun loadTemplate(templateName: String): String {
        val template = resourceLoader.getResource("classpath:templates/$templateName")
        return IOUtils.toString(template.inputStream, Charset.defaultCharset())
    }

    fun loadTemplateOrDefault(
        sub: ExtSubmission,
        templateName: String,
    ): String {
        val template = getTemplate(sub, templateName)
        return IOUtils.toString(template.inputStream, Charset.defaultCharset())
    }

    private fun getTemplate(
        sub: ExtSubmission,
        templateName: String,
    ) = sub.collections
        .map { getResource(templateName, it.accNo) }
        .filter { it.exists() }
        .firstOrElse { getResource(templateName, DEFAULT_TEMPLATE) }

    private fun getResource(
        templateName: String,
        collection: String,
    ) = resourceLoader.getResource("classpath:templates/${String.format(templateName, collection)}")
}

package ac.uk.ebi.biostd.notifications.email.util

import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import java.util.Locale

class EmailSender(
        private val emailSender: JavaMailSender,
        private val templateEngine: TemplateEngine) {

    fun send(emailParams: EmailParams) {
        val mail = emailSender.createMimeMessage()
        val helper = MimeMessageHelper(mail, true)
        helper.setTo(emailParams.to)
        helper.setFrom(emailParams.from)
        helper.setSubject(emailParams.subject)
        helper.setText(getText(emailParams.template), true)
        emailSender.send(mail)
    }

    private fun getText(templateParams: TemplateParams) =
            templateEngine.process(templateParams.templateName, templateParams.getContext())
}

data class EmailParams(val from: String, val to: String, val subject: String, val template: TemplateParams)

data class TemplateParams(val templateName: String, val context: Map<String, Any>) {
    fun getContext() = Context(Locale.ENGLISH, context)
}

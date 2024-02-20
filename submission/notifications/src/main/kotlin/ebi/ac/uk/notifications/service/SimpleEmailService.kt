package ebi.ac.uk.notifications.service

import ebi.ac.uk.notifications.model.Email
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper

class SimpleEmailService(private val javaMailSender: JavaMailSender) {
    fun send(email: Email) {
        val mail = javaMailSender.createMimeMessage()
        val helper = MimeMessageHelper(mail, true)
        helper.setTo(email.to)
        helper.setFrom(email.from)
        helper.setSubject(email.subject)
        helper.setText(email.content, true)
        email.bcc?.let { helper.setBcc(it) }
        javaMailSender.send(mail)
    }
}

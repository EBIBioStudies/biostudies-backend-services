package uk.ac.ebi.biostd.client.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file
import uk.ac.ebi.biostd.client.cli.ATTACHED_HELP
import uk.ac.ebi.biostd.client.cli.INPUT_HELP
import uk.ac.ebi.biostd.client.cli.ON_BEHALF_HELP
import uk.ac.ebi.biostd.client.cli.PASSWORD_HELP
import uk.ac.ebi.biostd.client.cli.SERVER_HELP
import uk.ac.ebi.biostd.client.cli.USER_HELP
import uk.ac.ebi.biostd.client.cli.services.SubmissionRequest
import uk.ac.ebi.biostd.client.cli.services.SubmissionService
import java.io.File

private const val FILES_SEPARATOR = ','

class SubmitCommand(private val submissionService: SubmissionService) : CliktCommand(name = "submit") {
    private val server by option("-s", "--server", help = SERVER_HELP).required()
    private val user by option("-u", "--user", help = USER_HELP).required()
    private val password by option("-p", "--password", help = PASSWORD_HELP).required()
    private val onBehalf by option("-b", "--onBehalf", help = ON_BEHALF_HELP)
    private val input by option("-i", "--input", help = INPUT_HELP).file(exists = true).required()
    private val attached by option("-a", "--attached", help = ATTACHED_HELP)

    override fun run() {
        val request = SubmissionRequest(
            server = server,
            user = user,
            password = password,
            onBehalf = onBehalf,
            file = input,
            attached = attached?.split(FILES_SEPARATOR)?.flatMap { getFiles(File(it)) }.orEmpty()
        )

        val response = submissionService.submit(request)
        echo("SUCCESS: Submission with AccNo ${response.accNo} was submitted")
    }

    private fun getFiles(file: File): List<File> =
        if (file.isDirectory) file.walk().filter { it.isFile }.toList() else listOf(file)
}
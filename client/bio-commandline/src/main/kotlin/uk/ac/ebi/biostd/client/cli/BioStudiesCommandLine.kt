package uk.ac.ebi.biostd.client.cli

import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat
import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.PrintMessage
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file
import ebi.ac.uk.base.isNotBlank
import ebi.ac.uk.io.isExcel
import ebi.ac.uk.util.file.ExcelReader
import org.json.JSONObject
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestClientResponseException
import java.io.File

const val FILES_SEPARATOR = ','
const val FILES_NOT_FOUND_ERROR_MSG = "Some of the given files were not found"

class BioStudiesCommandLine(private val excelReader: ExcelReader = ExcelReader()) : CliktCommand(name = "PTSubmit") {
    private val server by option("-s", "--server", help = "BioStudies host url").required()
    private val user by option("-u", "--user", help = "User that will perform the submission").required()
    private val password by option("-p", "--password", help = "The user password").required()
    private val input by option(
        "-i", "--input", help = "Path to the file containing the submission page tab").file(exists = true)
    private val format by option(
        "-f", "--format", help = "Page tab format used to process the given submission").required()
    private val attached by option(
        "-a", "--attached", help = "Comma separated list of paths to the files referenced in the submission")

    @Suppress("TooGenericExceptionCaught")
    override fun run() {
        try {
            val files: MutableList<File> = mutableListOf()

            attached?.let {
                it.split(FILES_SEPARATOR).forEach { path -> addFiles(files, path) }
            }

            val client = getClient(server, user, password)
            val subFormat = SubmissionFormat.valueOf(format.toUpperCase())
            val submission =
                if (input!!.isExcel()) client.submitXlsx(input!!, files).body!!
                else client.submitSingle(input!!.readText(), subFormat, files).body!!

            echo("SUCCESS: Submission with AccNo ${submission.accNo} was submitted")
        } catch (exception: Exception) {
            when (exception) {
                is ResourceAccessException -> throw PrintMessage(exception.cause?.message ?: FILES_NOT_FOUND_ERROR_MSG)
                is RestClientResponseException -> throw PrintMessage(formatException(exception))
                else -> throw exception
            }
        }
    }

    internal fun getClient(host: String, user: String, password: String) =
        SecurityWebClient.create(host).getAuthenticatedClient(user, password)

    // TODO The exceptions should be formatted at bio-webclient level
    private fun formatException(exception: RestClientResponseException) =
        when {
            exception.responseBodyAsString.isNotBlank() -> JSONObject(exception.responseBodyAsString).toString(2)
            exception.message.isNotBlank() -> exception.message!!
            else -> throw exception
        }

    private fun addFiles(files: MutableList<File>, path: String) {
        val file = File(path)

        if (file.isDirectory) file.walk().filter { it.isFile }.forEach { files.add(it) }
        else files.add(file)
    }
}

fun main(args: Array<String>) = BioStudiesCommandLine().main(args)

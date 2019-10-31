package uk.ac.ebi.biostd.client.cli

import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.PrintMessage
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file
import ebi.ac.uk.base.isNotBlank
import org.json.JSONObject
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.ResourceAccessException
import java.io.File
import java.io.FileNotFoundException
import java.net.ConnectException
import java.net.HttpRetryException

private const val FILES_SEPARATOR = ','
internal const val INVALID_SERVER_ERROR_MSG = "Connection Error: The provided server is invalid"
internal const val AUTHENTICATION_ERROR_MSG = "Authentication Error: Invalid email address or password"
internal const val FILES_NOT_FOUND_ERROR_MSG = "Files Not Found Error: Some of the attached files were not found"

class BioStudiesCommandLine : CliktCommand(name = "PTSubmit") {
    private val server by option("-s", "--server", help = "BioStudies host url").required()
    private val user by option("-u", "--user", help = "User that will perform the submission").required()
    private val password by option("-p", "--password", help = "The user password").required()
    private val input by option(
        "-i", "--input", help = "Path to the file containing the submission page tab").file(exists = true)
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
            val submission = client.submitSingle(input!!, files).body!!

            echo("SUCCESS: Submission with AccNo ${submission.accNo} was submitted")
        } catch (exception: Exception) {
            when (exception) {
                is HttpClientErrorException -> throw PrintMessage(formatRestException(exception))
                is ResourceAccessException -> throw PrintMessage(formatConnectionException(exception))
                else -> throw exception
            }
        }
    }

    internal fun getClient(host: String, user: String, password: String) =
        SecurityWebClient.create(host).getAuthenticatedClient(user, password)

    private fun formatConnectionException(exception: ResourceAccessException) =
        when (exception.cause) {
            is ConnectException -> INVALID_SERVER_ERROR_MSG
            is HttpRetryException -> AUTHENTICATION_ERROR_MSG
            is FileNotFoundException -> FILES_NOT_FOUND_ERROR_MSG
            else -> throw exception
        }

    private fun formatRestException(exception: HttpClientErrorException) =
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

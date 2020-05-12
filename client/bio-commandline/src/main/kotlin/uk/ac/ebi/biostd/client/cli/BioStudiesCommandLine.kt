package uk.ac.ebi.biostd.client.cli

import ac.uk.ebi.biostd.client.exception.SecurityWebClientException
import ac.uk.ebi.biostd.client.exception.WebClientException
import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.PrintMessage
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file
import java.io.File
import java.io.StringReader
import java.io.StringWriter
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource

private const val FILES_SEPARATOR = ','

class BioStudiesCommandLine : CliktCommand(name = "PTSubmit") {
    private val server by option("-s", "--server", help = "BioStudies host url").required()
    private val user by option("-u", "--user", help = "User that will perform the submission").required()
    private val password by option("-p", "--password", help = "The user password").required()
    private val onBehalf by option("-b", "--onBehalf", help = "Perform the operation on behalf of this user")
    private val input by option(
        "-i", "--input", help = "Path to the file containing the submission page tab").file(exists = true)
    private val attached by option(
        "-a", "--attached", help = "Comma separated list of paths to the files referenced in the submission")

    @Suppress("TooGenericExceptionCaught")
    override fun run() {
        runCatching {
            val files: MutableList<File> = mutableListOf()

            attached?.let {
                it.split(FILES_SEPARATOR).forEach { path -> addFiles(files, path) }
            }

            val client = getClient(server, user, password, onBehalf)
            val submission = client.submitSingle(input!!, files).body

            echo("SUCCESS: Submission with AccNo ${submission.accNo} was submitted")
        }.onFailure {
            val message = when (it) {
                is WebClientException -> formatErrorMessage(it.message!!)
                is SecurityWebClientException -> it.message!!
                else -> it.message!!
            }

            throw PrintMessage(message)
        }
    }

    internal fun getClient(host: String, user: String, password: String, onBehalf: String?) =
        if (onBehalf.isNullOrBlank()) SecurityWebClient.create(host).getAuthenticatedClient(user, password)
        else SecurityWebClient.create(host).getAuthenticatedClient(user, password, onBehalf)

    private fun addFiles(files: MutableList<File>, path: String) {
        val file = File(path)

        if (file.isDirectory) file.walk().filter { it.isFile }.forEach { files.add(it) }
        else files.add(file)
    }

    private fun formatErrorMessage(message: String): String {
        val xmlOutput = StreamResult(StringWriter())
        val xmlInput = StreamSource(StringReader(message))
        val transformer =
            TransformerFactory
                .newInstance()
                .apply { setAttribute("indent-number", 2) }
                .newTransformer()

        transformer.setOutputProperty(OutputKeys.INDENT, "yes")
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes")
        transformer.transform(xmlInput, xmlOutput)

        return xmlOutput.writer.toString()
    }
}

fun main(args: Array<String>) = BioStudiesCommandLine().main(args)

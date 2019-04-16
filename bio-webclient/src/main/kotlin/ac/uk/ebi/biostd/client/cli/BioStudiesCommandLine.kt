package ac.uk.ebi.biostd.client.cli

import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat
import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Options
import org.json.JSONObject
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestClientResponseException
import java.io.File

const val ATTACHED = "attached"
const val FORMAT = "format"
const val INPUT = "input"
const val PASSWORD = "password"
const val SERVER = "server"
const val USER = "user"
const val CLI_ID = "PT Submit"
const val FILES_SEPARATOR = ','
const val NULL_SUBMISSION_ERROR_MSG = "There was a problem performing the submission"

class BioStudiesCommandLine(
    private val helpFormatter: HelpFormatter = HelpFormatter()
) {
    internal val options = Options().apply {
        addRequiredOption("s", SERVER, true, "BioStudies host url")
        addRequiredOption("u", USER, true, "User that will perform the submission")
        addRequiredOption("p", PASSWORD, true, "The user password")
        addRequiredOption("i", INPUT, true, "Path to the file containing the submission page tab")
        addRequiredOption("f", FORMAT, true, "Page tab format used to process the given submission")
        addOption("a", ATTACHED, true, "Comma separated list of paths to the files referenced in the submission")
    }

    @Suppress("TooGenericExceptionCaught")
    fun submit(args: Array<String>): String {
        var response = ""
        try {
            val files: MutableList<File> = mutableListOf()
            val cli = DefaultParser().parse(options, args)
            val user = cli.getOptionValue(USER)
            val host = cli.getOptionValue(SERVER)
            val input = File(cli.getOptionValue(INPUT))
            val format = cli.getOptionValue(FORMAT)
            val password = cli.getOptionValue(PASSWORD)

            cli.getOptionValue(ATTACHED)?.let {
                it.split(FILES_SEPARATOR).forEach { path -> addFiles(files, path) }
            }

            val client = getClient(host, user, password)
            val submission = client.submitSingle(input.readText(), SubmissionFormat.valueOf(format), files).body

            response = "SUCCESS: Submission with AccNo ${submission!!.accNo} was submitted"
        } catch (exception: Exception) {
            when (exception) {
                is NullPointerException -> printError(NULL_SUBMISSION_ERROR_MSG)
                is ResourceAccessException -> printError(exception.cause?.message)
                is RestClientResponseException -> printJsonError(exception)
                else -> {
                    printError(exception.message)
                    helpFormatter.printHelp(CLI_ID, options)
                }
            }
        }

        return response
    }

    internal fun getClient(host: String, user: String, password: String) =
        SecurityWebClient.create(host).getAuthenticatedClient(user, password)

    internal fun printError(message: String?) = println("ERROR: $message")

    internal fun printJsonError(exception: RestClientResponseException) =
        println("ERROR: ${JSONObject(exception.responseBodyAsString).toString(2)}")

    private fun addFiles(files: MutableList<File>, path: String) {
        val file = File(path)

        if (file.isDirectory) file.walk().filter { it.isFile }.forEach { files.add(it) }
        else files.add(file)
    }
}

fun main(args: Array<String>) {
    println(BioStudiesCommandLine().submit(args))
}

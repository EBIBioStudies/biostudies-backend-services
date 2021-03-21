package uk.ac.ebi.biostd.client.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import uk.ac.ebi.biostd.client.cli.commands.DeleteCommand
import uk.ac.ebi.biostd.client.cli.commands.SubmitCommand
import uk.ac.ebi.biostd.client.cli.services.SubmissionService

class BioStudiesCommandLine : CliktCommand() {
    override fun run() {
        echo("That's BioStudies command line")
    }
}

fun main(args: Array<String>) {
    val submissionService = SubmissionService()
    BioStudiesCommandLine()
        .subcommands(SubmitCommand(submissionService), DeleteCommand(submissionService))
        .main(args)
}

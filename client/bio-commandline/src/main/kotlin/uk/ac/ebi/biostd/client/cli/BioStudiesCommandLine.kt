package uk.ac.ebi.biostd.client.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import uk.ac.ebi.biostd.client.cli.services.SubmissionService

class BioStudiesCommandLine : CliktCommand() {
    override fun run() {
        echo("That's BioStudies command line")
    }
}

fun main(args: Array<String>) = BioStudiesCommandLine()
    .subcommands(Submit(SubmissionService()), Delete(SubmissionService()))
    .main(args)

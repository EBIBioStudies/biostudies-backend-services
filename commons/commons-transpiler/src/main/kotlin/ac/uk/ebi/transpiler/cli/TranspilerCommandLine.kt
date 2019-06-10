package ac.uk.ebi.transpiler.cli

import ac.uk.ebi.biostd.integration.SubFormat
import ac.uk.ebi.transpiler.service.FilesTableTemplateTranspiler
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.PrintMessage
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file

const val FILES_SEPARATOR = ','

class TranspilerCommandLine(
    private val transpiler: FilesTableTemplateTranspiler = FilesTableTemplateTranspiler()
) : CliktCommand(name = "FilesTableGenerator") {
    private val dir by option("-d", "--directory", help = "Path to the directory containing the files").required()
    private val template by option(
        "-t", "--template", help = "Path to the file containing the template").file(exists = true)
    private val base by option(
        "-b", "--base", help = "Base that will be used as prefix for the generated files path").required()
    private val format by option(
        "-f", "--format", help = "Desired format for the generated page tab: TSV, JSON or XML").required()
    private val columns by option(
        "-c", "--columns", help = "Comma separated list of columns that map to the directory structure").required()

    @Suppress("TooGenericExceptionCaught")
    override fun run() {
        try {
            echo(transpiler.transpile(
                template = template!!.readText(),
                baseColumns = columns.split(FILES_SEPARATOR).map { it.trim() }.toList(),
                filesPath = dir,
                basePath = base,
                format = SubFormat.valueOf(format)))
        } catch (exception: Exception) {
            throw PrintMessage(exception.message!!)
        }
    }
}

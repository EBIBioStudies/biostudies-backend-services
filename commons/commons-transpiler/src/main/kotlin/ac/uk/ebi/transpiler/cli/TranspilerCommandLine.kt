package ac.uk.ebi.transpiler.cli

import ac.uk.ebi.biostd.SubFormat
import ac.uk.ebi.transpiler.service.FilesTableTemplateTranspiler
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Options
import java.io.File

const val BASE = "base"
const val COLUMNS = "columns"
const val DIR = "directory"
const val FORMAT = "format"
const val TEMPLATE = "template"
const val CLI_ID = "Files Table Generator"

class TranspilerCommandLine(
    private val helpFormatter: HelpFormatter = HelpFormatter(),
    private val transpiler: FilesTableTemplateTranspiler = FilesTableTemplateTranspiler()
) {
    internal val options = Options().apply {
        addRequiredOption("b", BASE, true, "Base that will be used as prefix for the generated files path")
        addRequiredOption("c", COLUMNS, true, "Comma separated list of columns that map to the directory structure")
        addRequiredOption("d", DIR, true, "Path to the directory containing the files")
        addRequiredOption("f", FORMAT, true, "Desired format for the generated page tab: TSV, JSON or XML")
        addRequiredOption("t", TEMPLATE, true, "Path to the file containing the template")
    }

    fun transpile(args: Array<String>): String {
        var pageTab = ""

        try {
            val cmd = DefaultParser().parse(options, args)
            val base = cmd.getOptionValue(BASE)!!
            val dir = cmd.getOptionValue(DIR)!!
            val format = cmd.getOptionValue(FORMAT)!!
            val columns = cmd.getOptionValue(COLUMNS)!!
            val template = File(cmd.getOptionValue(TEMPLATE)!!)

            pageTab = transpiler.transpile(
                template = template.readText(),
                baseColumns = columns.split(",").map { it.trim() }.toList(),
                filesPath = dir,
                basePath = base,
                format = SubFormat.valueOf(format)
            )
        } catch (exception: Exception) {
            printError(exception.message)
            helpFormatter.printHelp(CLI_ID, options)
        }

        return pageTab
    }

    internal fun printError(message: String?) = println("ERROR: $message")
}

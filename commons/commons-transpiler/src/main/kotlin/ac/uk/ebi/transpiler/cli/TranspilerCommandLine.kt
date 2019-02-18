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

class TranspilerCommandLine {
    fun transpile(args: Array<String>): String {
        var pagetab = ""
        val options = Options().apply {
            addOption("b", BASE, true, "Base that will be used as prefix for the generated files path")
            addOption("c", COLUMNS, true, "Comma separated list of columns that map to the directory structure")
            addOption("d", DIR, true, "Path to the directory containing the files")
            addOption("f", FORMAT, true, "Desired format for the generated page tab: TSV, JSON or XML")
            addOption("t", TEMPLATE, true, "Path to the file containing the template")
        }

        try {
            val cmd = DefaultParser().parse(options, args)
            val base = cmd.getOptionValue(BASE)!!
            val dir = cmd.getOptionValue(DIR)!!
            val format = cmd.getOptionValue(FORMAT)!!
            val columns = cmd.getOptionValue(COLUMNS)!!
            val template = File(cmd.getOptionValue(TEMPLATE)!!)

            pagetab = FilesTableTemplateTranspiler().transpile(
                template = template.readText(),
                baseColumns = columns.split(","),
                filesPath = dir,
                basePath = base,
                format = SubFormat.valueOf(format)
            )
        } catch (exception: Exception) {
            when (exception) {
                is NullPointerException -> println("All the arguments are required")
                else -> println(exception.message)
            }

            HelpFormatter().printHelp("Files Table Generator", options)
        }

        return pagetab
    }
}

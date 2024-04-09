package ac.uk.ebi.transpiler.common

class FilesTableTemplate {
    var header: List<String> = listOf()
    val rows: MutableList<FilesTableTemplateRow> = mutableListOf()

    fun addRecord(
        path: String,
        attributes: List<String>,
    ) = rows.add(FilesTableTemplateRow(path, attributes))
}

class FilesTableTemplateRow(val path: String, val attributes: List<String>)

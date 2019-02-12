package ac.uk.ebi.transpiler.common

class LibraryFile {
    var header: List<String> = listOf()
    val records: MutableList<LibraryFileRecord> = mutableListOf()

    fun addRecord(path: String, attributes: List<String>) =  records.add(LibraryFileRecord(path, attributes))
}

class LibraryFileRecord(private val path: String, private val attributes: List<String>)

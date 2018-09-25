package ac.uk.ebi.biostd.submission

import arrow.core.Either
import ebi.ac.uk.base.EMPTY
import ebi.ac.uk.base.applyIfNotNullOrEmpty
import ebi.ac.uk.base.asIsoDate

data class Submission(
        var accNo: String = EMPTY,
        var rtime: Long = 0L,
        var ctime: Long = 0L,
        var mtime: Long = 0L,
        var relPath: String = EMPTY,
        var secretKey: String = EMPTY,
        var user: User = User(),
        var title: String = EMPTY,
        var rootPath: String? = null,
        var attributes: MutableList<Attribute> = mutableListOf(),
        var accessTags: MutableList<String> = mutableListOf(),
        var section: Section = Section()) {

    fun allAttributes(): List<Attribute> {
        val allAttributes = attributes.toMutableList()
        allAttributes += Attribute(SubFields.TITLE, title)
        allAttributes += Attribute(SubFields.RELEASE_DATE, asIsoDate(rtime).toString())
        rootPath.applyIfNotNullOrEmpty { allAttributes.add(Attribute(SubFields.ROOT_PATH, it)) }
        return allAttributes
    }
}

data class Section(
        var type: String = EMPTY,
        var accNo: String? = null,
        var attributes: MutableList<Attribute> = mutableListOf(),
        var subsections: MutableList<Either<Section, SectionsTable>> = mutableListOf(),
        var links: MutableList<Either<Link, LinksTable>> = mutableListOf(),
        var files: MutableList<Either<File, FilesTable>> = mutableListOf())

data class Term(val name: String, val value: String)

data class Attribute(
        var name: String,
        var value: String,
        var reference: Boolean = false,
        var terms: List<Term> = emptyList()) {

    constructor(name: Any, value: String, reference: Boolean = false, terms: List<Term> = emptyList()) :
            this(name.toString(), value, reference, terms)

    companion object {
        val EMPTY_ATTR: Attribute = Attribute(EMPTY, EMPTY, false, listOf())
    }
}

data class Link(
        var url: String = EMPTY,
        var attributes: MutableList<Attribute> = mutableListOf())

data class File(
        var name: String = EMPTY,
        var size: Int = 0,
        var type: String = FileFields.FILE.toString(),
        var attributes: MutableList<Attribute> = mutableListOf())

data class User(
        var email: String = EMPTY,
        var id: String = EMPTY)

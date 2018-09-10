package ac.uk.ebi.biostd.submission

import arrow.core.Either
import ebi.ac.uk.base.EMPTY

data class Submission(
        var accNo: String = EMPTY,
        var rTime: Long = 0L,
        var title: String = EMPTY,
        var rootPath: String? = null,
        var attributes: MutableList<Attribute> = mutableListOf(),
        var accessTags: MutableList<String> = mutableListOf(),
        var section: Section = Section())

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
        var terms: List<Term>) {
    companion object {
        val EMPTY_ATTR: Attribute = Attribute(EMPTY, EMPTY, false, listOf())
    }
}

data class Link(
        var url: String = EMPTY,
        var attributes: MutableList<Attribute> = mutableListOf())

data class File(
        var name: String = EMPTY,
        var attributes: MutableList<Attribute> = mutableListOf())

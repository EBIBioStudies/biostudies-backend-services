package ac.uk.ebi.biostd.submission

import ac.uk.ebi.biostd.common.FilesTable
import ac.uk.ebi.biostd.common.LinksTable
import ac.uk.ebi.biostd.common.SectionsTable
import arrow.core.Either

internal const val EMPTY = ""

data class Submission(
        var rTime: Long = 0L,
        var accNo: String = EMPTY,
        var title: String = EMPTY,
        var rootPath: String = EMPTY,
        var accessTags: MutableList<String> = mutableListOf(),
        var sections: MutableList<Section> = mutableListOf(),
        var attributes: MutableList<Attribute> = mutableListOf())

data class Section(
        var type: String = EMPTY,
        var accNo: String = EMPTY,
        var attrs: MutableList<Attribute> = mutableListOf(),
        var sections: MutableList<Either<Section, SectionsTable>> = mutableListOf(),
        var links: MutableList<Either<Link, LinksTable>> = mutableListOf(),
        var files: MutableList<Either<File, FilesTable>> = mutableListOf())

data class Term(val name: String, val value: String)

data class Attribute(
        var name: String,
        var value: String,
        var reference: Boolean = false,
        var terms: List<Term>) {
    companion object {
        val EMPTY: Attribute = Attribute("", "", false, listOf())
    }
}

data class Link(
        var url: String = EMPTY,
        var attrs: MutableList<Attribute> = mutableListOf())

data class File(
        var name: String = EMPTY,
        val attrs: MutableList<Attribute> = mutableListOf())

package ac.uk.ebi.biostd.submission

import ac.uk.ebi.biostd.common.Either
import ac.uk.ebi.biostd.common.Table
import ac.uk.ebi.biostd.common.TableElement

internal const val NO_TABLE_INDEX = -1
internal const val EMPTY = ""

data class Submission(
        var rTime: Long = 0L,
        var cTime: Long = 0L,
        var mTime: Long = 0L,
        var accNo: String = EMPTY,
        var title: String = EMPTY,
        var rootPath: String = EMPTY,
        var accessTags: MutableList<String> = mutableListOf(),
        var sections: MutableList<Section> = mutableListOf(),
        var attributes: MutableList<Attribute> = mutableListOf())

data class Section(
        var type: String = EMPTY,
        var accNo: String = EMPTY,
        var ord: Int? = null,
        var tableIndex: Int = NO_TABLE_INDEX,
        var attrs: MutableList<Attribute> = mutableListOf(),
        var sections: MutableList<Either<Section, Table<Section>>> = mutableListOf(),
        var links: MutableList<Either<Link, Table<Link>>> = mutableListOf()) : TableElement {

    override val id: String
        get() = accNo
    override val attributes: List<Attribute>
        get() = attrs
}

data class Attribute(
        var name: String,
        var value: String,
        var reference: Boolean = false,
        var terms: List<Pair<String, String>>,
        var order: Int)

data class Link(
        var url: String = EMPTY,
        var ord: Int = 0,
        var tableIndex: Int = NO_TABLE_INDEX,
        var attrs: MutableList<Attribute> = mutableListOf()) : TableElement {

    override val id: String
        get() = url
    override val attributes: List<Attribute>
        get() = attrs
}

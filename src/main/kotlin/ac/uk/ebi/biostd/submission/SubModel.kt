package ac.uk.ebi.biostd.submission

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
        var ord: Int? = null,
        var tableIndex: Int = NO_TABLE_INDEX,
        var attrs: MutableList<Attribute> = mutableListOf(),
        var sections: MutableList<Section> = mutableListOf(),
        var links: MutableList<Link> = mutableListOf())

data class Attribute(
        var name: String,
        var value: String,
        var reference: Boolean = false,
        var qualifierVal: String? = null,
        var order: Int)

data class Link(
        var url: String = EMPTY,
        var ord: Int = 0,
        var tableIndex: Int = NO_TABLE_INDEX,
        var attributes: MutableList<Attribute> = mutableListOf()
)

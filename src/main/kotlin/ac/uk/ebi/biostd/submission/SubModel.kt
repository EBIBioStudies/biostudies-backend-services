package ac.uk.ebi.biostd.submission

internal const val noTableIndex = -1
internal const val empty = ""

data class Submission(
        var rTime: Long = 0L,
        var cTime: Long = 0L,
        var mTime: Long = 0L,
        var accNo: String = empty,
        var title: String = empty,
        var rootPath: String = empty,
        var accessTags: MutableList<String> = mutableListOf(),
        var sections: MutableList<Section> = mutableListOf(),
        var attributes: MutableList<Attribute> = mutableListOf())

data class Section(
        var type: String = empty,
        var ord: Int? = null,
        var tableIndex: Int = noTableIndex,
        var attributes: MutableList<Attribute> = mutableListOf(),
        var sections: MutableList<Section> = mutableListOf(),
        var links: MutableList<Link> = mutableListOf())

data class Attribute(
        var name: String,
        var value: String,
        var reference: Boolean = false,
        var qualifierVal: String? = null,
        var order: Int)

data class Link(
        var url: String = empty,
        var ord: Int = 0,
        var tableIndex: Int = noTableIndex,
        var attributes: MutableList<Attribute> = mutableListOf()
)
package ac.uk.ebi.biostd.serialization.tsv

import ac.uk.ebi.biostd.submission.Link
import ac.uk.ebi.biostd.submission.Submission
import ac.uk.ebi.biostd.submission.noTableIndex
import com.google.common.collect.HashBasedTable
import com.google.common.collect.Table

typealias DataTable = Table<String, String, String>

class TsvSerializer {

    private val builder: TsvBuilder = TsvBuilder()

    fun serialize(submission: Submission): String {
        builder.addSubAccAndTags(submission.accNo, submission.accessTags)
        builder.addSubTitle(submission.title)
        builder.addSubReleaseDate(submission.rTime)
        builder.addRootPath(submission.rootPath)
        submission.attributes.forEach(builder::addSecAttr)

        submission.sections.forEach { section ->
            builder.addSeparator()
            builder.addSecType(section.type)
            section.attributes.forEach(builder::addSecAttr)

            val (linksTable, listLinks) = processLinks(section.links)

            linksTable?.let {
                builder.addSeparator()
                builder.addTableHeaders(it.columnKeySet())

                for (attr in it.columnKeySet()) {
                    for (link in it.rowKeySet()) {
                        builder.addTableValue(it[link, attr])
                    }
                }
            }

            listLinks.forEach {
                builder.addSeparator()
                builder.addSecLink(it)
                builder.addSecLinkAttributes(it.attributes)
            }
        }

        return builder.toString()
    }

    private fun processLinks(links: List<Link>): Pair<DataTable?, List<Link>> {
        val tableLinksMap = links.groupBy { it.tableIndex != noTableIndex }
        return Pair(asTable(tableLinksMap[true]), tableLinksMap[false].orEmpty())
    }

    private fun asTable(links: List<Link>?): DataTable? {
        return links?.let {
            val data = HashBasedTable.create<String, String, String>()

            it.forEach { link ->
                data.put(link.url, linkTableUrlHeader, link.url)

                link.attributes.forEach { attr ->
                    data.put(link.url, attr.name, attr.value)
                }
            }

            return data
        }
    }
}
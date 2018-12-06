package ac.uk.ebi.biostd.tsv.deserialization.model

sealed class TsvChunk(body: List<String>) {

    val header: TsvChunkLine
    val lines: List<TsvChunkLine>

    init {
        val lines = body.map { TsvChunkLine(it) }
        this.header = lines.first()
        this.lines = lines.drop(1)
    }
}

class LinkChunk(body: List<String>) : TsvChunk(body)
class FileChunk(body: List<String>) : TsvChunk(body)
class LinksTableChunk(body: List<String>) : TsvChunk(body)
class FileTableChunk(body: List<String>) : TsvChunk(body)

sealed class SectionTableChunk(body: List<String>) : TsvChunk(body)
class RootSectionTableChunk(body: List<String>) : SectionTableChunk(body)
class SubSectionTableChunk(body: List<String>, val parent: String) : SectionTableChunk(body)

sealed class SectionChunk(body: List<String>) : TsvChunk(body)
class RootSubSectionChunk(body: List<String>) : SectionChunk(body)
class SubSectionChunk(body: List<String>, val parent: String) : SectionChunk(body)

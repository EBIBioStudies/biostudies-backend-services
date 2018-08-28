package ac.uk.ebi.biostd.serialization.tsv

import ac.uk.ebi.biostd.test.createRNA_Profiling
import org.junit.Test

class ArrayExpressSubTsvParserTest {

    private val testInstance: TsvSerializer = TsvSerializer()

    @Test
    fun parse() {
        val sub = createRNA_Profiling()
        val tsvString = TsvString(testInstance.serialize(sub))

        assertThat(tsvString[0]).contains("Submission", "E-MTAB-6957", "Public")
    }
}

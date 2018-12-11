package ac.uk.ebi.biostd.tsv

import ac.uk.ebi.biostd.test.twoBasicSubmission
import org.junit.jupiter.api.Test

/**
 *
 */
internal class TsvSerializerTest {

    private val testInstance: TsvSerializer = TsvSerializer()

    @Test
    fun deserializeList() {
        testInstance.deserializeList(twoBasicSubmission())
    }
}
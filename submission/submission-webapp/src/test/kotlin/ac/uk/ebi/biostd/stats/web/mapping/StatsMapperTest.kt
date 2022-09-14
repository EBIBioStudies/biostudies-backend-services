package ac.uk.ebi.biostd.stats.web.mapping

import ac.uk.ebi.biostd.persistence.common.model.SubmissionStatType.FILES_SIZE
import ac.uk.ebi.biostd.persistence.doc.model.SingleSubmissionStat
import ac.uk.ebi.biostd.stats.web.model.SubmissionStatDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class StatsMapperTest {
    @Test
    fun `to stat dto`() {
        val stat = SingleSubmissionStat("S-BSST0", 12L, FILES_SIZE)
        val expectedDto = SubmissionStatDto("S-BSST0", 12L, FILES_SIZE.value)

        assertThat(stat.toStatDto()).isEqualToComparingFieldByField(expectedDto)
    }

    @Test
    fun `to stat`() {
        val dto = SubmissionStatDto("S-BSST0", 12L, "FILES_SIZE")
        val expectedStat = SingleSubmissionStat("S-BSST0", 12L, FILES_SIZE)

        assertThat(dto.toStat()).isEqualToComparingFieldByField(expectedStat)
    }

    @Test
    fun `to stat with invalid type`() {
        val dto = SubmissionStatDto("S-BSST0", 12L, "INVALID")
        val exception = assertThrows<IllegalArgumentException> { dto.toStat() }

        assertThat(exception.message).isEqualTo("Unknown SubmissionStatType 'INVALID")
    }
}

package ebi.ac.uk.asserts

import arrow.core.Either
import ebi.ac.uk.model.Section
import ebi.ac.uk.model.SectionsTable
import org.assertj.core.api.AbstractAssert
import org.assertj.core.api.Assertions.assertThat

fun assertThat(section: Section): SectionAssert {
    return SectionAssert(section)
}

fun assertThat(section: Either<Section, SectionsTable>): SectionEitherAssert {
    return SectionEitherAssert(section)
}

class SectionAssert(actual: Section) :
    AbstractAssert<SectionAssert, Section>(actual, SectionAssert::class.java) {

    fun has(accNo: String, type: String) {
        assertThat(actual.accNo).isEqualTo(accNo)
        assertThat(actual.type).isEqualTo(type)
    }
}

class SectionEitherAssert(actual: Either<Section, SectionsTable>) :
    AbstractAssert<SectionEitherAssert, Either<Section, SectionsTable>>(actual, SectionEitherAssert::class.java) {

    fun isSection(): Section {
        assertThat(actual.isLeft())
        return actual.getLeft()
    }

    fun isTable(): SectionsTable {
        assertThat(actual.isRight())
        return actual.getRight()
    }
}

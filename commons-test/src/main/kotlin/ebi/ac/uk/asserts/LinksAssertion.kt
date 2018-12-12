package ebi.ac.uk.asserts

import arrow.core.Either
import ebi.ac.uk.model.Link
import ebi.ac.uk.model.LinksTable
import org.assertj.core.api.AbstractAssert
import org.assertj.core.api.Assertions

fun assertThat(link: Link): LinkAssert {
    return LinkAssert(link)
}

fun assertThat(link: Either<Link, LinksTable>): LinkEitherAssert {
    return LinkEitherAssert(link)
}

class LinkAssert(actual: Link) :
    AbstractAssert<LinkAssert, Link>(actual, LinkAssert::class.java)

class LinkEitherAssert(actual: Either<Link, LinksTable>) :
    AbstractAssert<LinkEitherAssert, Either<Link, LinksTable>>(actual, LinkEitherAssert::class.java) {

    fun isLink(): Link {
        Assertions.assertThat(actual.isLeft())
        return actual.getLeft()
    }

    fun isTable(): LinksTable {
        Assertions.assertThat(actual.isRight())
        return actual.getRight()
    }
}